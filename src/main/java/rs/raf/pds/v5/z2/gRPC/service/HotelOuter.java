package rs.raf.pds.v5.z2.gRPC.service;

import rs.raf.pds.v5.z2.gRPC.model.Hotel; // domain model

// After protobuf regeneration there will be a generated class rs.raf.pds.v5.z2.gRPC.Hotel (proto).
// To avoid name clash we do not import it and refer with full name.
public final class HotelOuter {
    private HotelOuter() {}

    public static rs.raf.pds.v5.z2.gRPC.Hotel newHotel(Hotel h) {
        return rs.raf.pds.v5.z2.gRPC.Hotel.newBuilder()
                .setId(h.getId())
                .setName(h.getName())
                .setStars(h.getStars())
                .setDistanceM(h.getDistanceMeters())
                .setCity(h.getCity())
                .setAvailable(h.isAvailable())
                .build();
    }
}
