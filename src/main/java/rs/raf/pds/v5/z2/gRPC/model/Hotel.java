package rs.raf.pds.v5.z2.gRPC.model;

public class Hotel {
    private final String id;
    private final String name;
    private final int stars; // 3..5
    private final int distanceMeters;
    private final String city;
    private volatile boolean available; // single-room model: available or not
    private volatile double lastPrice;
    private volatile double minPrice;
    private volatile double maxPrice;

    public Hotel(String id, String name, int stars, int distanceMeters, String city, boolean available) {
        this.id = id;
        this.name = name;
        this.stars = stars;
        this.distanceMeters = distanceMeters;
        this.city = city;
        this.available = available;
        // initialize simple price model based on stars and distance
        this.lastPrice = basePrice();
        this.minPrice = lastPrice;
        this.maxPrice = lastPrice;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getStars() { return stars; }
    public int getDistanceMeters() { return distanceMeters; }
    public String getCity() { return city; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public double getLastPrice() { return lastPrice; }
    public double getMinPrice() { return minPrice; }
    public double getMaxPrice() { return maxPrice; }

    // Simple pricing: base = 50 + 20*(stars-3) - distanceKm*5
    private double basePrice() {
        double distanceKm = distanceMeters / 1000.0;
        double price = 50 + 20 * (stars - 3) - 5 * distanceKm;
        return Math.max(20, Math.round(price * 100.0) / 100.0);
    }

    public synchronized void updatePriceOnAvailability(boolean nowAvailable) {
        this.available = nowAvailable;
        // if available turned false (reserved), increase price slightly; if becomes available, decrease slightly
        double factor = nowAvailable ? 0.98 : 1.05;
        lastPrice = Math.max(15, Math.round((lastPrice * factor) * 100.0) / 100.0);
        if (lastPrice < minPrice) minPrice = lastPrice;
        if (lastPrice > maxPrice) maxPrice = lastPrice;
    }
}
