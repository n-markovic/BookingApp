package rs.raf.pds.v5.z2.gRPC.store;

import rs.raf.pds.v5.z2.gRPC.model.Hotel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class HotelRegistry {
    private final List<Hotel> hotels = new ArrayList<>();
    private final String defaultCity = "CityA";
    private final String secondCity = "CityB";

    public HotelRegistry() {
        // City A
        hotels.add(new Hotel("A1", "A-Hotel-1", 3, 800, defaultCity, true));
        hotels.add(new Hotel("A2", "A-Hotel-2", 4, 1200, defaultCity, true));
        hotels.add(new Hotel("A3", "A-Hotel-3", 5, 300, defaultCity, true));
        // City B (second city) with three hotels
        hotels.add(new Hotel("B1", "B-Hotel-1", 3, 600, secondCity, true));
        hotels.add(new Hotel("B2", "B-Hotel-2", 4, 1500, secondCity, true));
        hotels.add(new Hotel("B3", "B-Hotel-3", 5, 400, secondCity, true));
    }

    public List<Hotel> listAll() { return Collections.unmodifiableList(hotels); }

    public List<Hotel> query(String city, int maxDistance, int minStars) {
    String targetCity = (city == null || city.isEmpty()) ? defaultCity : city;
        return hotels.stream()
                .filter(h -> h.getCity().equalsIgnoreCase(targetCity))
                .filter(h -> h.getDistanceMeters() <= maxDistance)
                .filter(h -> h.getStars() >= minStars)
                .filter(Hotel::isAvailable)
                .collect(Collectors.toList());
    }
}
