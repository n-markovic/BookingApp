package rs.raf.pds.v5.z2.gRPC.model;

import java.time.Instant;
import java.util.Objects;

public class BookingRecord {
    private final String bookingId;
    private final String userId;
    private final String hotelId;        // renamed from roomId
    private final String startDate;      // yyyy-MM-dd
    private final int durationDays;      // >=1
    private final String endDate;        // computed exclusive end date (start + duration)
    private volatile String status;      // ACTIVE, CANCELED, PAID
    private final double pricePerNight;
    private final double totalPrice;
    private final Instant createdAt = Instant.now();

    public BookingRecord(String bookingId, String userId, String hotelId, String startDate, int durationDays, String status, String endDate, double pricePerNight) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.hotelId = hotelId;
        this.startDate = startDate;
        this.durationDays = durationDays;
        this.status = status;
        this.endDate = endDate;
        this.pricePerNight = pricePerNight;
        this.totalPrice = Math.round(pricePerNight * durationDays * 100.0) / 100.0;
    }

    public String getBookingId() { return bookingId; }
    public String getUserId() { return userId; }
    public String getHotelId() { return hotelId; }
    public String getStartDate() { return startDate; }
    public int getDurationDays() { return durationDays; }
    public String getEndDate() { return endDate; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }

    public double getPricePerNight() { return pricePerNight; }
    public double getTotalPrice() { return totalPrice; }

    public synchronized void cancel() { if (!"CANCELED".equals(status)) status = "CANCELED"; }
    public synchronized void paid() { status = "PAID"; }

    @Override public boolean equals(Object o) { return this == o || (o instanceof BookingRecord && Objects.equals(bookingId, ((BookingRecord) o).bookingId)); }
    @Override public int hashCode() { return Objects.hash(bookingId); }
}
