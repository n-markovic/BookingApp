package rs.raf.pds.v5.z2.gRPC.model;

public class Hotel {
    private final String id;
    private final String name;
    private final int stars; // 3..5
    private final int distanceMeters;
    private final String city;
    private volatile boolean available;

    public Hotel(String id, String name, int stars, int distanceMeters, String city, boolean available) {
        this.id = id;
        this.name = name;
        this.stars = stars;
        this.distanceMeters = distanceMeters;
        this.city = city;
        this.available = available;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getStars() { return stars; }
    public int getDistanceMeters() { return distanceMeters; }
    public String getCity() { return city; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}
