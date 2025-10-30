package rs.raf.pds.v5.z2.gRPC.store;

import rs.raf.pds.v5.z2.gRPC.model.BookingRecord;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class InMemoryBookingStore {
    private final ConcurrentMap<String, BookingRecord> byId = new ConcurrentHashMap<>();
    // Index per hotel to speed overlap checks (hotelId -> list of booking ids)
    private final ConcurrentMap<String, Set<String>> byHotel = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_CANCELED = "CANCELED";
    public static final String STATUS_PAID = "PAID";

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public Result create(String userId, String hotelId, String startDateStr, int durationDays) {
        if (blank(userId) || blank(hotelId) || blank(startDateStr)) return Result.failure("Missing required field(s)");
        if (durationDays < 1) return Result.failure("duration_days must be >= 1");
        LocalDate start;
        try { start = LocalDate.parse(startDateStr, DATE_FMT); } catch (DateTimeParseException e) { return Result.failure("start_date must be yyyy-MM-dd"); }
        LocalDate end = start.plusDays(durationDays); // exclusive

        // Overlap detection: [start,end) overlaps existing ACTIVE booking if ranges intersect
        Set<String> ids = byHotel.getOrDefault(hotelId, Collections.emptySet());
        for (String id : ids) {
            BookingRecord existing = byId.get(id);
            if (existing == null || !STATUS_ACTIVE.equals(existing.getStatus())) continue;
            LocalDate eStart = LocalDate.parse(existing.getStartDate(), DATE_FMT);
            LocalDate eEnd = LocalDate.parse(existing.getEndDate(), DATE_FMT);
            boolean overlap = !(end.compareTo(eStart) <= 0 || start.compareTo(eEnd) >= 0);
            if (overlap) return Result.failure("Hotel already booked for overlapping period");
        }

        String newId = String.valueOf(sequence.incrementAndGet());
        BookingRecord rec = new BookingRecord(newId, userId, hotelId, startDateStr, durationDays, STATUS_ACTIVE, end.format(DATE_FMT));
        byId.put(newId, rec);
        byHotel.computeIfAbsent(hotelId, k -> ConcurrentHashMap.newKeySet()).add(newId);
        return Result.success("Booking created", rec);
    }

    public Result cancel(String bookingId) {
        if (blank(bookingId)) return Result.failure("booking_id required");
        BookingRecord rec = byId.get(bookingId);
        if (rec == null) return Result.failure("Booking not found");
        rec.cancel();
        return Result.success("Booking canceled", rec);
    }

    public Result cancelAuthorized(String bookingId, String userId) {
        if (blank(bookingId) || blank(userId)) return Result.failure("booking_id and user_id required");
        BookingRecord rec = byId.get(bookingId);
        if (rec == null) return Result.failure("Booking not found");
        if (!rec.getUserId().equals(userId)) return Result.failure("Not owner of booking");
        rec.cancel();
        return Result.success("Booking canceled", rec);
    }

    public List<BookingRecord> listByUser(String userId) {
        if (blank(userId)) return Collections.emptyList();
        return byId.values().stream()
                .filter(b -> b.getUserId().equals(userId))
                .sorted(Comparator.comparing(BookingRecord::getCreatedAt))
                .collect(Collectors.toList());
    }

    public List<BookingRecord> listActiveByHotel(String hotelId) {
        Set<String> ids = byHotel.getOrDefault(hotelId, Collections.emptySet());
        return ids.stream()
                .map(byId::get)
                .filter(Objects::nonNull)
                .filter(b -> STATUS_ACTIVE.equals(b.getStatus()))
                .sorted(Comparator.comparing(BookingRecord::getStartDate))
                .collect(Collectors.toList());
    }

    public BookingRecord findById(String bookingId) { return byId.get(bookingId); }

    public Result markPaid(String bookingId, String userId) {
        BookingRecord rec = byId.get(bookingId);
        if (rec == null) return Result.failure("Booking not found");
        if (!rec.getUserId().equals(userId)) return Result.failure("Not owner of booking");
        if (!STATUS_ACTIVE.equals(rec.getStatus())) return Result.failure("Cannot pay: status=" + rec.getStatus());
        rec.paid();
        return Result.success("Payment recorded", rec);
    }

    private boolean blank(String s) { return s == null || s.trim().isEmpty(); }

    public static class Result {
        public final boolean success; public final String message; public final BookingRecord record;
        private Result(boolean success, String message, BookingRecord record) { this.success = success; this.message = message; this.record = record; }
        public static Result success(String m, BookingRecord r) { return new Result(true, m, r); }
        public static Result failure(String m) { return new Result(false, m, null); }
    }
}
