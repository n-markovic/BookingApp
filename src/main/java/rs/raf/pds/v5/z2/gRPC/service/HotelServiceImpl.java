package rs.raf.pds.v5.z2.gRPC.service;

import io.grpc.stub.StreamObserver;
import rs.raf.pds.v5.z2.gRPC.*;
import rs.raf.pds.v5.z2.gRPC.model.BookingRecord;
import rs.raf.pds.v5.z2.gRPC.model.Hotel;
import rs.raf.pds.v5.z2.gRPC.store.HotelRegistry;
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

    public HotelServiceImpl(HotelRegistry hotels) {
        this(hotels, null, null);
    }

    public HotelServiceImpl(HotelRegistry hotels, Function<String, List<BookingRecord>> occupancyProvider) {
        this(hotels, occupancyProvider, null);
    }

    public HotelServiceImpl(HotelRegistry hotels, Function<String, List<BookingRecord>> occupancyProvider, Consumer<Hotel> updateSink) {
        this.hotels = hotels;
        this.occupancyProvider = occupancyProvider;
        this.updateSink = updateSink;
    }

    @Override
    public void askHotels(AskRequest request, StreamObserver<AskResponse> responseObserver) {
        int maxDistance = request.getMaxDistanceM();
        int minStars = request.getMinStars();
        if (maxDistance <= 0) maxDistance = Integer.MAX_VALUE;
        if (minStars < 3) minStars = 3;
        AskResponse.Builder builder = AskResponse.newBuilder();
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
            responseObserver.onNext(ReserveResponse.newBuilder().setSuccess(false).setMessage("Unknown hotel").build());
            responseObserver.onCompleted();
            return;
        }
        synchronized (h) {
            if (!h.isAvailable()) {
                responseObserver.onNext(ReserveResponse.newBuilder().setSuccess(false).setMessage("Not available").build());
            } else {
                h.updatePriceOnAvailability(false); // mark reserved
                responseObserver.onNext(ReserveResponse.newBuilder().setSuccess(true).setMessage("Reserved").build());
                if (updateSink != null) updateSink.accept(h);
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
            responseObserver.onNext(ReleaseResponse.newBuilder().setSuccess(false).setMessage("Unknown hotel").build());
            responseObserver.onCompleted();
            return;
        }
        synchronized (h) {
            h.updatePriceOnAvailability(true); // mark released
            responseObserver.onNext(ReleaseResponse.newBuilder().setSuccess(true).setMessage("Released").build());
            if (updateSink != null) updateSink.accept(h);
        }
        responseObserver.onCompleted();
    }

    @Override
    public void confirmPayment(ConfirmPaymentRequest request, StreamObserver<ConfirmPaymentResponse> responseObserver) {
        Hotel h = hotels.listAll().stream()
                .filter(x -> x.getId().equalsIgnoreCase(request.getHotelId()))
                .findFirst().orElse(null);
        if (h == null) {
            responseObserver.onNext(ConfirmPaymentResponse.newBuilder().setSuccess(false).setMessage("Unknown hotel").build());
            responseObserver.onCompleted();
            return;
        }
        // In single-room model, confirmPayment keeps it unavailable until checkout; since we don't simulate dates here, keep it reserved
        responseObserver.onNext(ConfirmPaymentResponse.newBuilder().setSuccess(true).setMessage("Payment confirmed").build());
        if (updateSink != null) updateSink.accept(h);
        responseObserver.onCompleted();
    }
}
