package rs.raf.pds.v5.z2.gRPC.store;

import rs.raf.pds.v5.z2.gRPC.model.BookingRecord;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class InMemoryBookingStore {
    private final ConcurrentMap<String, BookingRecord> byId = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, String> roomDateIndex = new ConcurrentHashMap<>(); // room|date -> id

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_CANCELED = "CANCELED";

    public static String roomDateKey(String roomId, String date) { return roomId + "|" + date; }

    public Result create(String userId, String roomId, String date) {
        if (blank(userId) || blank(roomId) || blank(date)) return Result.failure("Nedostaje obavezno polje");
        if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) return Result.failure("Datum mora biti u formatu yyyy-MM-dd");
        String key = roomDateKey(roomId, date);
        String newId = UUID.randomUUID().toString();
        String existing = roomDateIndex.putIfAbsent(key, newId);
        if (existing != null) return Result.failure("Soba je vec rezervisana za dati datum");
        BookingRecord rec = new BookingRecord(newId, userId, roomId, date, STATUS_ACTIVE);
        byId.put(newId, rec);
        return Result.success("Rezervacija kreirana", rec);
    }

    public Result cancel(String bookingId) {
        if (blank(bookingId)) return Result.failure("booking_id required");
        BookingRecord rec = byId.get(bookingId);
        if (rec == null) return Result.failure("Rezervacija nije pronadjena");
        rec.cancel();
        return Result.success("Rezervacija otkazana", rec);
    }

    public List<BookingRecord> listByUser(String userId) {
        if (blank(userId)) return Collections.emptyList();
        return byId.values().stream()
                .filter(b -> b.getUserId().equals(userId))
                .sorted(Comparator.comparing(BookingRecord::getCreatedAt))
                .collect(Collectors.toList());
    }

    private boolean blank(String s) { return s == null || s.trim().isEmpty(); }

    public static class Result {
        public final boolean success; public final String message; public final BookingRecord record;
        private Result(boolean success, String message, BookingRecord record) { this.success = success; this.message = message; this.record = record; }
        public static Result success(String m, BookingRecord r) { return new Result(true, m, r); }
        public static Result failure(String m) { return new Result(false, m, null); }
    }
}
