package rs.raf.pds.v5.z2.gRPC.service;

import io.grpc.stub.StreamObserver;
import rs.raf.pds.v5.z2.gRPC.*;
import rs.raf.pds.v5.z2.gRPC.model.BookingRecord;
import rs.raf.pds.v5.z2.gRPC.model.Hotel;
import rs.raf.pds.v5.z2.gRPC.store.HotelRegistry;
import java.util.List;
import java.util.function.Function;

/**
 * Hotel-only gRPC service exposing hotel queries and details.
 * Occupancy periods are built from booking store (active bookings).
 */
public class HotelServiceImpl extends HotelServiceGrpc.HotelServiceImplBase {
    private final HotelRegistry hotels;
    // optional occupancy provider: hotelId -> active bookings
    private final Function<String, List<BookingRecord>> occupancyProvider;

    public HotelServiceImpl(HotelRegistry hotels) {
        this(hotels, null);
    }

    public HotelServiceImpl(HotelRegistry hotels, Function<String, List<BookingRecord>> occupancyProvider) {
        this.hotels = hotels;
        this.occupancyProvider = occupancyProvider;
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
                    .setAvailable(h.isAvailable());
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
                    .setAvailable(found.isAvailable());
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
}
