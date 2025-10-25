package rs.raf.pds.v5.z2.gRPC.model;

import java.time.Instant;
import java.util.Objects;

public class BookingRecord {
    private final String bookingId;
    private final String userId;
    private final String roomId;
    private final String date; // yyyy-MM-dd
    private volatile String status; // ACTIVE or CANCELED
    private final Instant createdAt = Instant.now();

    public BookingRecord(String bookingId, String userId, String roomId, String date, String status) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.roomId = roomId;
        this.date = date;
        this.status = status;
    }

    public String getBookingId() { return bookingId; }
    public String getUserId() { return userId; }
    public String getRoomId() { return roomId; }
    public String getDate() { return date; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }

    public synchronized void cancel() { if (!"CANCELED".equals(status)) status = "CANCELED"; }

    @Override public boolean equals(Object o) { return this == o || (o instanceof BookingRecord && Objects.equals(bookingId, ((BookingRecord) o).bookingId)); }
    @Override public int hashCode() { return Objects.hash(bookingId); }
}
