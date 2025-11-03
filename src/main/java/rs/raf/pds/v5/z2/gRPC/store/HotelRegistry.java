package rs.raf.pds.v5.z2.gRPC.store;

import rs.raf.pds.v5.z2.gRPC.model.Hotel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class HotelRegistry {
    private final List<Hotel> hotels = new ArrayList<>();
    private final String defaultCity = "Beograd";
    private final String city2 = "Zlatibor";
    private final String city3 = "Kragujevac";

    public HotelRegistry() {
        // Beograd
        hotels.add(new Hotel("BE-MOS", "Moskva", 5, 500, defaultCity, true));
        hotels.add(new Hotel("BE-BAL", "Balkan", 4, 1200, defaultCity, true));
        hotels.add(new Hotel("BE-SAV", "Savamala", 3, 800, defaultCity, true));
        // Zlatibor
        hotels.add(new Hotel("ZT-LUX", "Lux", 5, 3000, city2, true));
        hotels.add(new Hotel("ZT-KON", "Konaciste", 4, 3200, city2, true));
        hotels.add(new Hotel("ZT-PAL", "Palisad", 3, 2800, city2, true));
        // Kragujevac
        hotels.add(new Hotel("KR-ZEN", "Zeneva", 5, 6000, city3, true));
        hotels.add(new Hotel("KR-GAR", "Garni", 4, 5500, city3, true));
        hotels.add(new Hotel("KR-RUB", "Rubikon", 3, 5200, city3, true));
    }

    public List<Hotel> listAll() { return Collections.unmodifiableList(hotels); }

    public List<Hotel> query(String city, int maxDistance, int minStars) {
    String targetCity = (city == null || city.isEmpty()) ? defaultCity : city;
        return hotels.stream()
                .filter(h -> h.getCity().equalsIgnoreCase(targetCity))
                .filter(h -> h.getDistanceMeters() <= maxDistance)
                .filter(h -> h.getStars() >= minStars)
                .collect(Collectors.toList());
    }
}
