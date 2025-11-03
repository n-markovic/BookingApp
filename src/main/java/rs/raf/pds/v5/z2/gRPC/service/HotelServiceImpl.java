package rs.raf.pds.v5.z2.gRPC.service;

import io.grpc.stub.StreamObserver;
import rs.raf.pds.v5.z2.gRPC.*;
import rs.raf.pds.v5.z2.gRPC.model.BookingRecord;
import rs.raf.pds.v5.z2.gRPC.model.Hotel;
import rs.raf.pds.v5.z2.gRPC.store.HotelRegistry;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.function.Function;
import java.util.function.Consumer;

/**
 * Hotel-only gRPC service exposing hotel queries and details.
 * Occupancy periods are built from booking store (active bookings).
 */
public class HotelServiceImpl extends HotelServiceGrpc.HotelServiceImplBase {
    private final HotelRegistry hotels;
    // optional occupancy provider: hotelId -> active bookings
    private final Function<String, List<BookingRecord>> occupancyProvider;
    // optional update sink for realtime bus
    private final Consumer<Hotel> updateSink;
    // optional BookingService stub to notify BookingServer about hotel updates
    private final BookingServiceGrpc.BookingServiceBlockingStub bookingStub;

    public HotelServiceImpl(HotelRegistry hotels) {
        this(hotels, null, null);
    }

    public HotelServiceImpl(HotelRegistry hotels, Function<String, List<BookingRecord>> occupancyProvider) {
        this(hotels, occupancyProvider, null);
    }

    public HotelServiceImpl(HotelRegistry hotels, Function<String, List<BookingRecord>> occupancyProvider, Consumer<Hotel> updateSink) {
        this(hotels, occupancyProvider, updateSink, null);
    }

    public HotelServiceImpl(HotelRegistry hotels, Function<String, List<BookingRecord>> occupancyProvider, Consumer<Hotel> updateSink, BookingServiceGrpc.BookingServiceBlockingStub bookingStub) {
        this.hotels = hotels;
        this.occupancyProvider = occupancyProvider;
        this.updateSink = updateSink;
        this.bookingStub = bookingStub;
    }

    /**
     * Apply a discount (percent, e.g., 20 for 20%) to a hotel's lastPrice and notify BookingServer.
     */
    public synchronized boolean applyDiscount(String hotelId, int percent) {
        if (percent <= 0 || percent >= 100) return false;
        Hotel h = hotels.listAll().stream()
                .filter(x -> x.getId().equalsIgnoreCase(hotelId))
                .findFirst().orElse(null);
        if (h == null) return false;
        synchronized (h) {
            double old = h.getLastPrice();
            // Randomly decide to increase or decrease by percent
            int sign = Math.random() < 0.5 ? -1 : 1; // -1 => decrease, 1 => increase
            double factor = 1.0 + sign * (percent / 100.0);
            double np = Math.max(15, (double) Math.round(old * factor));
            // Set exact new price using Hotel.setPrice
            h.setPrice(np);
            double after = h.getLastPrice();
            String display = h.getCity() + " - " + h.getName();
            // New message format requested
            System.out.println("Hotel " + display + " je promenio svoju cenu: stara=" + old + " nova=" + after);
            if (updateSink != null) updateSink.accept(h);
            if (bookingStub != null) {
                try {
                    bookingStub.notifyHotelUpdate(HotelUpdate.newBuilder().setHotelId(h.getId()).setLastPrice(h.getLastPrice()).build());
                } catch (Exception ignored) {}
            }
            return true;
        }
    }

    @Override
    public void askHotels(AskRequest request, StreamObserver<AskResponse> responseObserver) {
        int maxDistance = request.getMaxDistanceM();
        int minStars = request.getMinStars();
        if (maxDistance <= 0) maxDistance = Integer.MAX_VALUE;
        if (minStars < 3) minStars = 3;
        AskResponse.Builder builder = AskResponse.newBuilder();
    String user = request.getUserId();
    System.out.println("HotelServer: Primljen upit od " + (user==null||user.isEmpty()?"gosta":user) + " za " + request.getCity() + " (maxDist=" + maxDistance + ", minStars=" + minStars + ")");
    for (Hotel h : hotels.query(request.getCity(), maxDistance, minStars)) {
        rs.raf.pds.v5.z2.gRPC.Hotel.Builder hb = rs.raf.pds.v5.z2.gRPC.Hotel.newBuilder()
                    .setId(h.getId())
                    .setName(h.getName())
                    .setStars(h.getStars())
                    .setDistanceM(h.getDistanceMeters())
                    .setCity(h.getCity())
            .setAvailable(h.isAvailable())
            .setLastPrice(h.getLastPrice())
            .setMinPrice(h.getMinPrice())
            .setMaxPrice(h.getMaxPrice());
            if (occupancyProvider != null) {
                for (BookingRecord rec : occupancyProvider.apply(h.getId())) {
                    hb.addOccupied(OccupancyPeriod.newBuilder()
                            .setStartDate(rec.getStartDate())
                            .setEndDate(rec.getEndDate())
                            .build());
                }
            }
            builder.addHotels(hb.build());
        }
        responseObserver.onNext(builder.build());
    System.out.println("HotelServer: Odgovor za " + (user==null?"gosta":user) + " na zahtev ask");
        responseObserver.onCompleted();
    }

    @Override
    public void getHotel(HotelIdRequest request, StreamObserver<HotelResponse> responseObserver) {
        String key = request.getIdOrName();
        Hotel found = hotels.listAll().stream()
                .filter(h -> h.getId().equalsIgnoreCase(key) || h.getName().equalsIgnoreCase(key))
                .findFirst().orElse(null);
        HotelResponse.Builder rb = HotelResponse.newBuilder();
        if (found != null) {
        rs.raf.pds.v5.z2.gRPC.Hotel.Builder hb = rs.raf.pds.v5.z2.gRPC.Hotel.newBuilder()
                    .setId(found.getId())
                    .setName(found.getName())
                    .setStars(found.getStars())
                    .setDistanceM(found.getDistanceMeters())
                    .setCity(found.getCity())
            .setAvailable(found.isAvailable())
            .setLastPrice(found.getLastPrice())
            .setMinPrice(found.getMinPrice())
            .setMaxPrice(found.getMaxPrice());
            if (occupancyProvider != null) {
                for (BookingRecord rec : occupancyProvider.apply(found.getId())) {
                    hb.addOccupied(OccupancyPeriod.newBuilder()
                            .setStartDate(rec.getStartDate())
                            .setEndDate(rec.getEndDate())
                            .build());
                }
            }
            rb.setFound(true).setHotel(hb.build());
        } else {
            rb.setFound(false);
        }
        responseObserver.onNext(rb.build());
        responseObserver.onCompleted();
    }

    @Override
    public void reserve(ReserveRequest request, StreamObserver<ReserveResponse> responseObserver) {
        Hotel h = hotels.listAll().stream()
                .filter(x -> x.getId().equalsIgnoreCase(request.getHotelId()))
                .findFirst().orElse(null);
        if (h == null) {
            responseObserver.onNext(ReserveResponse.newBuilder().setSuccess(false).setMessage("Nepoznat hotel").build());
            responseObserver.onCompleted();
            return;
        }
        // Parse requested period
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate reqStart;
        LocalDate reqEnd;
        try {
            reqStart = LocalDate.parse(request.getStartDate(), fmt);
            reqEnd = reqStart.plusDays(request.getDurationDays());
        } catch (DateTimeParseException e) {
            responseObserver.onNext(ReserveResponse.newBuilder().setSuccess(false).setMessage("Invalid date").build());
            responseObserver.onCompleted();
            return;
        }

        synchronized (h) {
            boolean overlap = false;
            if (occupancyProvider != null) {
                for (BookingRecord rec : occupancyProvider.apply(h.getId())) {
                    try {
                        LocalDate eStart = LocalDate.parse(rec.getStartDate(), fmt);
                        LocalDate eEnd = LocalDate.parse(rec.getEndDate(), fmt);
                        // overlap if ranges intersect: !(reqEnd <= eStart || reqStart >= eEnd)
                        if (!(reqEnd.compareTo(eStart) <= 0 || reqStart.compareTo(eEnd) >= 0)) { overlap = true; break; }
                    } catch (DateTimeParseException ignored) {}
                }
            } else {
                // fallback to single-room availability flag
                if (!h.isAvailable()) overlap = true;
            }
            if (overlap) {
                responseObserver.onNext(ReserveResponse.newBuilder().setSuccess(false).setMessage("Nije dostupno za trazene datume").build());
            } else {
                // do not change price on provisional reserve; price change occurs on confirmPayment
                responseObserver.onNext(ReserveResponse.newBuilder().setSuccess(true).setMessage("Rezervisano").build());
                if (updateSink != null) updateSink.accept(h);
                // Log reservation with optional user
                String ru = request.getUserId();
                String ruTrim = ru == null ? null : ru.trim();
                String rDisplay = (ruTrim == null || ruTrim.isEmpty()) ? "gost" : ruTrim;
                System.out.println("HotelServer: Napravljena rezervacija za hotel=" + h.getId() + " od " + rDisplay + " start=" + request.getStartDate() + " trajanje=" + request.getDurationDays());
            }
        }
        responseObserver.onCompleted();
    }

    @Override
    public void release(ReleaseRequest request, StreamObserver<ReleaseResponse> responseObserver) {
        Hotel h = hotels.listAll().stream()
                .filter(x -> x.getId().equalsIgnoreCase(request.getHotelId()))
                .findFirst().orElse(null);
        if (h == null) {
            responseObserver.onNext(ReleaseResponse.newBuilder().setSuccess(false).setMessage("Nepoznat hotel").build());
            responseObserver.onCompleted();
            return;
        }
        synchronized (h) {
            // record release for pricing only
            h.recordReservation(false);
            responseObserver.onNext(ReleaseResponse.newBuilder().setSuccess(true).setMessage("Released").build());
            if (updateSink != null) updateSink.accept(h);
            if (bookingStub != null) {
                try {
                    bookingStub.notifyHotelUpdate(HotelUpdate.newBuilder().setHotelId(h.getId()).setLastPrice(h.getLastPrice()).build());
                } catch (Exception ignored) {}
            }
        }
        responseObserver.onCompleted();
    }

    @Override
    public void confirmPayment(ConfirmPaymentRequest request, StreamObserver<ConfirmPaymentResponse> responseObserver) {
        Hotel h = hotels.listAll().stream()
                .filter(x -> x.getId().equalsIgnoreCase(request.getHotelId()))
                .findFirst().orElse(null);
        if (h == null) {
            responseObserver.onNext(ConfirmPaymentResponse.newBuilder().setSuccess(false).setMessage("Nepoznat hotel").build());
            responseObserver.onCompleted();
            return;
        }
        // Credit hotel account with the provided amount (if present)
        double amount = 0.0;
        try { amount = request.getAmount(); } catch (Exception ignored) {}
        if (amount > 0.0) {
            System.out.println("HotelServer: Primljen predlog za naplatu za rezervaciju=" + request.getBookingId() + " iznos=" + amount);
            rs.raf.pds.v5.z2.gRPC.store.BankService.getInstance().deposit("hotel_server", amount);
            double hotelBal = rs.raf.pds.v5.z2.gRPC.store.BankService.getInstance().getBalance("hotel_server");
            System.out.println("HotelServer: stanje hotela nakon uplate=" + hotelBal);
            // Apply price change now that payment is confirmed
            h.recordReservation(true);
            if (updateSink != null) updateSink.accept(h);
            // Notify BookingServer about the price change if stub available
            if (bookingStub != null) {
                try {
                    bookingStub.notifyHotelUpdate(HotelUpdate.newBuilder().setHotelId(h.getId()).setLastPrice(h.getLastPrice()).build());
                } catch (Exception ignored) {}
            }
        }
        // In single-room model, confirmPayment keeps it unavailable until checkout
        responseObserver.onNext(ConfirmPaymentResponse.newBuilder().setSuccess(true).setMessage("Payment confirmed").build());
        if (updateSink != null) updateSink.accept(h);
        responseObserver.onCompleted();
    }
}
