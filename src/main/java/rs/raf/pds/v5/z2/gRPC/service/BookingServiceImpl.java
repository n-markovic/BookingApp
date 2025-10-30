package rs.raf.pds.v5.z2.gRPC.service;

import io.grpc.stub.StreamObserver;
import rs.raf.pds.v5.z2.gRPC.*;
import rs.raf.pds.v5.z2.gRPC.model.BookingRecord;
import rs.raf.pds.v5.z2.gRPC.store.InMemoryBookingStore;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

public class BookingServiceImpl extends BookingServiceGrpc.BookingServiceImplBase {
    private final InMemoryBookingStore store;
    private final HotelServiceGrpc.HotelServiceBlockingStub hotelBlocking;
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
            .setMessage("Unknown hotel: " + hotelId)
            .build());
        responseObserver.onCompleted();
        return;
    }
    rs.raf.pds.v5.z2.gRPC.Hotel h = hotelResp.getHotel();
    if (!h.getAvailable()) {
        responseObserver.onNext(BookingResponse.newBuilder()
            .setSuccess(false)
            .setMessage("Hotel not available: " + h.getName())
            .build());
        responseObserver.onCompleted();
        return;
    }
            // First check validation via HotelService GetHotel
            InMemoryBookingStore.Result res = store.create(request.getUserId(), h.getId(), request.getStartDate(), request.getDurationDays());
            if (!res.success) {
                responseObserver.onNext(BookingResponse.newBuilder().setSuccess(false).setMessage(res.message).build());
                responseObserver.onCompleted();
                return;
            }
            // Async reserve at hotel
            String bookingId = res.record.getBookingId();
            executor.submit(() -> {
                try {
                    ReserveResponse rr = hotelBlocking.reserve(ReserveRequest.newBuilder()
                            .setHotelId(h.getId())
                            .setStartDate(request.getStartDate())
                            .setDurationDays(request.getDurationDays())
                            .build());
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

            responseObserver.onNext(BookingResponse.newBuilder().setSuccess(true).setMessage("Booking created | id=" + bookingId + " | pay within 60s").build());
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
            AskResponse resp = hotelBlocking.askHotels(request);
            responseObserver.onNext(resp);
        } catch (Exception e) {
            responseObserver.onNext(AskResponse.newBuilder().build());
        } finally {
            responseObserver.onCompleted();
        }
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
            // Mark paid locally first
            InMemoryBookingStore.Result pr = store.markPaid(request.getBookingId(), request.getUserId());
            if (!pr.success) {
                responseObserver.onNext(PaymentResponse.newBuilder().setSuccess(false).setMessage(pr.message).build());
                responseObserver.onCompleted();
                return;
            }
            // Confirm at hotel asynchronously
            executor.submit(() -> {
                try {
                    BookingRecord br = pr.record;
                    hotelBlocking.confirmPayment(ConfirmPaymentRequest.newBuilder()
                            .setHotelId(br.getHotelId())
                            .setBookingId(br.getBookingId())
                            .build());
                } catch (Exception ignored) {}
            });
            responseObserver.onNext(PaymentResponse.newBuilder().setSuccess(true).setMessage("Payment accepted").build());
            responseObserver.onCompleted();
        }
}
