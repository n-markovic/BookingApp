package rs.raf.pds.v5.z2.gRPC.service;

import io.grpc.stub.StreamObserver;
import rs.raf.pds.v5.z2.gRPC.*;
import rs.raf.pds.v5.z2.gRPC.model.BookingRecord;
import rs.raf.pds.v5.z2.gRPC.store.InMemoryBookingStore;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class BookingServiceImpl extends BookingServiceGrpc.BookingServiceImplBase {
    private final InMemoryBookingStore store;
    private final HotelServiceGrpc.HotelServiceBlockingStub hotelBlocking;
    // Notification subscriptions: userId -> observer
    private final java.util.concurrent.ConcurrentMap<String, io.grpc.stub.StreamObserver<Notification>> subscriptions = new java.util.concurrent.ConcurrentHashMap<>();
    // Interest registry: hotelId -> set of userIds who asked or booked
    private final java.util.concurrent.ConcurrentMap<String, java.util.Set<String>> hotelInterested = new java.util.concurrent.ConcurrentHashMap<>();
    // Executors for async tasks (reservation/payment) and deadlines
    private final java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newCachedThreadPool();
    private final java.util.concurrent.ScheduledExecutorService scheduler = java.util.concurrent.Executors.newSingleThreadScheduledExecutor();

    public BookingServiceImpl(InMemoryBookingStore store, String hotelHost, int hotelPort) {
        this.store = store;
        ManagedChannel ch = ManagedChannelBuilder.forAddress(hotelHost, hotelPort).usePlaintext().build();
        this.hotelBlocking = HotelServiceGrpc.newBlockingStub(ch);
    }

    // Convenience: create default in-memory store internally
    public BookingServiceImpl(String hotelHost, int hotelPort) {
        this(new InMemoryBookingStore(), hotelHost, hotelPort);
    }

    @Override
    public void makeBooking(BookingRequest request, StreamObserver<BookingResponse> responseObserver) {
    String hotelId = request.getHotelId();
    // Validate hotel exists
    HotelResponse hotelResp = hotelBlocking.getHotel(HotelIdRequest.newBuilder().setIdOrName(hotelId).build());
    if (!hotelResp.getFound()) {
        responseObserver.onNext(BookingResponse.newBuilder()
            .setSuccess(false)
            .setMessage("Nepoznat hotel: " + hotelId)
            .build());
        responseObserver.onCompleted();
        return;
    }
    rs.raf.pds.v5.z2.gRPC.Hotel h = hotelResp.getHotel();
            // First check validation via HotelService GetHotel and snapshot pricePerNight
            double pricePerNight = h.getLastPrice();
            InMemoryBookingStore.Result res = store.create(request.getUserId(), h.getId(), request.getStartDate(), request.getDurationDays(), pricePerNight);
            if (!res.success) {
                responseObserver.onNext(BookingResponse.newBuilder().setSuccess(false).setMessage(res.message).build());
                responseObserver.onCompleted();
                return;
            }
            // Async reserve at hotel
            String bookingId = res.record.getBookingId();
            executor.submit(() -> {
                try {
            ReserveRequest.Builder rreq = ReserveRequest.newBuilder()
                .setHotelId(h.getId())
                .setStartDate(request.getStartDate())
                .setDurationDays(request.getDurationDays());
            String ru = request.getUserId();
            String ruTrim = ru == null ? null : ru.trim();
            String rDisplay = (ruTrim == null || ruTrim.isEmpty()) ? "gost" : ruTrim;
            if (ruTrim != null && !ruTrim.isEmpty()) rreq.setUserId(ruTrim);
            System.out.println("BookingServer: Prosledjujem zahtev za rezervaciju od " + rDisplay + " za hotel=" + h.getId() + " start=" + request.getStartDate() + " trajanje=" + request.getDurationDays());
                    ReserveResponse rr = hotelBlocking.reserve(rreq.build());
                    if (!rr.getSuccess()) {
                        // Rollback locally if hotel refused
                        store.cancel(bookingId);
                    }
                } catch (Exception ex) {
                    store.cancel(bookingId);
                }
            });
            // Schedule auto-cancel if unpaid within 60s
            scheduler.schedule(() -> {
                BookingRecord br = store.findById(bookingId);
                if (br != null && InMemoryBookingStore.STATUS_ACTIVE.equals(br.getStatus())) {
                    store.cancel(bookingId);
                    try {
                        hotelBlocking.release(ReleaseRequest.newBuilder()
                                .setHotelId(h.getId())
                                .setStartDate(request.getStartDate())
                                .setDurationDays(request.getDurationDays())
                                .build());
                    } catch (Exception ignored) {}
                }
            }, 60, java.util.concurrent.TimeUnit.SECONDS);

            responseObserver.onNext(BookingResponse.newBuilder().setSuccess(true).setMessage("Rezervacija kreirana | id=" + bookingId + " | platite u roku od 60s").build());
            // Register interest for this booked hotel for the user
            String uid = request.getUserId();
            if (uid != null && !uid.trim().isEmpty()) {
                hotelInterested.computeIfAbsent(h.getId(), k -> java.util.Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<>())).add(uid.trim());
            }
            responseObserver.onCompleted();
    }

    @Override
    public void cancelBooking(CancelRequest request, StreamObserver<CancelResponse> responseObserver) {
        // Authorization: require user_id and ownership
        InMemoryBookingStore.Result res;
        String user = request.getUserId();
        if (user != null && !user.isEmpty()) {
            res = store.cancelAuthorized(request.getBookingId(), user);
        } else {
            res = store.cancel(request.getBookingId()); // legacy fallback
        }
        responseObserver.onNext(CancelResponse.newBuilder()
                .setSuccess(res.success)
                .setMessage(res.message)
                .build());
        responseObserver.onCompleted();
    }

    // Proxy: forward AskHotels to HotelService so client only connects to BookingService
    @Override
    public void askHotels(AskRequest request, StreamObserver<AskResponse> responseObserver) {
    try {
        String user = request.getUserId();
        // Normalize and treat whitespace-only user IDs as empty
        String userTrim = user == null ? null : user.trim();
    String displayUser = (userTrim == null || userTrim.isEmpty()) ? "gost" : userTrim;
    System.out.println("BookingServer: Prosledjujem upit od " + displayUser + " za grad " + request.getCity() + " (maxDist=" + request.getMaxDistanceM() + ", minStars=" + request.getMinStars() + ")");
        AskRequest.Builder askForHotel = AskRequest.newBuilder()
            .setCity(request.getCity())
            .setMaxDistanceM(request.getMaxDistanceM())
            .setMinStars(request.getMinStars());
        if (userTrim != null && !userTrim.isEmpty()) askForHotel.setUserId(userTrim);
            AskResponse resp = hotelBlocking.askHotels(askForHotel.build());
            // Register interest: if user asked a city, register interest for each returned hotel
            String uid = userTrim;
            if (uid != null && !uid.isEmpty()) {
                for (Hotel h : resp.getHotelsList()) {
                    hotelInterested.computeIfAbsent(h.getId(), k -> java.util.Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<>())).add(uid);
                }
            }
            // Rebuild response and augment hotels with occupancy info from local booking store
            AskResponse.Builder out = AskResponse.newBuilder();
            for (Hotel h : resp.getHotelsList()) {
                rs.raf.pds.v5.z2.gRPC.Hotel.Builder hb = h.toBuilder();
                // Add occupied periods from the booking store (if any)
                for (rs.raf.pds.v5.z2.gRPC.model.BookingRecord rec : store.listActiveByHotel(h.getId())) {
                    hb.addOccupied(OccupancyPeriod.newBuilder().setStartDate(rec.getStartDate()).setEndDate(rec.getEndDate()).build());
                }
                out.addHotels(hb.build());
            }
            responseObserver.onNext(out.build());
            System.out.println("BookingServer: Prosledjujem odgovor na upit korisniku " + displayUser);
        } catch (Exception e) {
            responseObserver.onNext(AskResponse.newBuilder().build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void subscribeNotifications(UserRequest request, StreamObserver<Notification> responseObserver) {
        String uid = request.getUserId();
        if (uid == null || uid.trim().isEmpty()) {
            responseObserver.onCompleted();
            return;
        }
        String u = uid.trim();
        subscriptions.put(u, responseObserver);
    //System.out.println("BookingServer: korisnik se pretplatio na notifikacije: " + u);
        // Note: we intentionally don't complete the observer; it stays open until client disconnects
    }

    @Override
    public void notifyHotelUpdate(HotelUpdate request, StreamObserver<BookingResponse> responseObserver) {
        String hid = request.getHotelId();
        double price = request.getLastPrice();
        // Try to fetch hotel details to produce "City - HotelName" format
        String text;
        try {
            HotelResponse hr = hotelBlocking.getHotel(HotelIdRequest.newBuilder().setIdOrName(hid).build());
            if (hr.getFound()) {
                rs.raf.pds.v5.z2.gRPC.Hotel h = hr.getHotel();
                text = h.getCity() + " - " + h.getName() + " cena promenjena u " + price;
            } else {
                text = "Hotel " + hid + " cena promenjena u " + price;
            }
        } catch (Exception e) {
            text = "Hotel " + hid + " cena promenjena u " + price;
        }
        // Find interested users
        java.util.Set<String> users = hotelInterested.getOrDefault(hid, java.util.Collections.emptySet());
        for (String u : users) {
            io.grpc.stub.StreamObserver<Notification> obs = subscriptions.get(u);
            if (obs != null) {
                try {
                    obs.onNext(Notification.newBuilder().setText(text).setHotelId(hid).build());
                } catch (Exception ignored) {}
            }
        }
        // Print user-friendly hotel display in logs
        String displayLog;
        try {
            HotelResponse hr2 = hotelBlocking.getHotel(HotelIdRequest.newBuilder().setIdOrName(hid).build());
            if (hr2.getFound()) {
                rs.raf.pds.v5.z2.gRPC.Hotel hh = hr2.getHotel();
                displayLog = hh.getCity() + " - " + hh.getName();
            } else displayLog = hid;
        } catch (Exception e) { displayLog = hid; }
    System.out.println("BookingServer: Obavesteno " + users.size() + " korisnika o " + displayLog + " cena=" + price);
        responseObserver.onNext(BookingResponse.newBuilder().setSuccess(true).setMessage("Notified").build());
        responseObserver.onCompleted();
    }

    @Override
    public void getBookings(UserRequest request, StreamObserver<BookingInfo> responseObserver) {
        for (BookingRecord rec : store.listByUser(request.getUserId())) {
            BookingInfo info = BookingInfo.newBuilder()
                    .setBookingId(rec.getBookingId())
                    .setHotelId(rec.getHotelId())
                    .setStartDate(rec.getStartDate())
                    .setDurationDays(rec.getDurationDays())
                    .setEndDate(rec.getEndDate())
                    .setStatus(rec.getStatus())
                    .build();
            responseObserver.onNext(info);
        }
        responseObserver.onCompleted();
    }

        @Override
        public void pay(PaymentRequest request, StreamObserver<PaymentResponse> responseObserver) {
                // Mark existence and authorization first
                InMemoryBookingStore.Result pr = store.findById(request.getBookingId()) != null ? InMemoryBookingStore.Result.success("ok", store.findById(request.getBookingId())) : InMemoryBookingStore.Result.failure("Booking not found");
                if (!pr.success) {
                    responseObserver.onNext(PaymentResponse.newBuilder().setSuccess(false).setMessage(pr.message).build());
                    responseObserver.onCompleted();
                    return;
                }
                BookingRecord rec = pr.record;
                if (!rec.getUserId().equals(request.getUserId())) {
                    responseObserver.onNext(PaymentResponse.newBuilder().setSuccess(false).setMessage("Niste vlasnik rezervacije").build());
                    responseObserver.onCompleted();
                    return;
                }
                if (!InMemoryBookingStore.STATUS_ACTIVE.equals(rec.getStatus())) {
                    responseObserver.onNext(PaymentResponse.newBuilder().setSuccess(false).setMessage("Ne moze se platiti: status=" + rec.getStatus()).build());
                    responseObserver.onCompleted();
                    return;
                }

                // Determine price from hotel lastPrice * duration (simple model)
                // Use price snapshot stored in booking record (pricePerNight and totalPrice)
                double total = rec.getTotalPrice();

                // Check bank and perform transfer: withdraw and keep booking cut locally, then forward hotel share
                rs.raf.pds.v5.z2.gRPC.store.BankService bank = rs.raf.pds.v5.z2.gRPC.store.BankService.getInstance();
                // Log payment request
                System.out.println("BookingServer: Zahtev za naplatu od " + rec.getUserId() + " za rezervaciju=" + rec.getBookingId() + " iznos=" + total);
                double hotelShare = bank.withdrawAndKeepBookingCutReturnHotelShare(rec.getUserId(), total);
                if (hotelShare < 0) {
                    responseObserver.onNext(PaymentResponse.newBuilder().setSuccess(false).setMessage("Nedovoljno sredstava").build());
                    responseObserver.onCompleted();
                    return;
                }
                // Log booking_server balance after withdraw
                double bookingServerBal = bank.getBalance("booking_server");
                System.out.println("BookingServer: Oduzeto " + total + " od " + rec.getUserId() + ", stanje BookingServer-a=" + bookingServerBal);

                // Mark paid locally and notify hotel (include hotelShare amount)
                store.markPaid(rec.getBookingId(), rec.getUserId());
                executor.submit(() -> {
                    try {
                        System.out.println("BookingServer: Prosledjujem udeo hotela=" + hotelShare + " HotelServer-u za rezervaciju=" + rec.getBookingId());
                        hotelBlocking.confirmPayment(ConfirmPaymentRequest.newBuilder()
                                .setHotelId(rec.getHotelId())
                                .setBookingId(rec.getBookingId())
                                .setAmount(hotelShare) // amount to hotel (forwarded)
                                .build());
                    } catch (Exception ignored) {}
                });

            double balAfter = rs.raf.pds.v5.z2.gRPC.store.BankService.getInstance().getBalance(rec.getUserId());
            responseObserver.onNext(PaymentResponse.newBuilder().setSuccess(true).setMessage("Placanje prihvaceno").setBalance(balAfter).build());
                responseObserver.onCompleted();
        }

        @Override
        public void deposit(DepositRequest request, StreamObserver<DepositResponse> responseObserver) {
            if (request == null || request.getUserId() == null || request.getUserId().isEmpty()) {
                responseObserver.onNext(DepositResponse.newBuilder().setSuccess(false).setMessage("user_id required").setBalance(0).build());
                responseObserver.onCompleted();
                return;
            }
            double amount = request.getAmount();
            if (amount <= 0) {
                responseObserver.onNext(DepositResponse.newBuilder().setSuccess(false).setMessage("amount must be > 0").setBalance(0).build());
                responseObserver.onCompleted();
                return;
            }
            rs.raf.pds.v5.z2.gRPC.store.BankService bank = rs.raf.pds.v5.z2.gRPC.store.BankService.getInstance();
            double bal = bank.deposit(request.getUserId(), amount);
            responseObserver.onNext(DepositResponse.newBuilder().setSuccess(true).setMessage("Uplaceno").setBalance(bal).build());
            responseObserver.onCompleted();
        }
}
