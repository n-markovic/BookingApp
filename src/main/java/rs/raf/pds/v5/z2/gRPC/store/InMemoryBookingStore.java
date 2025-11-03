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

    // pricePerNight passed by caller (snapshot at booking time)
    public Result create(String userId, String hotelId, String startDateStr, int durationDays, double pricePerNight) {
        if (blank(userId) || blank(hotelId) || blank(startDateStr)) return Result.failure("Nedostaje obavezno polje");
        if (durationDays < 1) return Result.failure("Trajanje mora biti >= 1");
        LocalDate start;
        try { start = LocalDate.parse(startDateStr, DATE_FMT); } catch (DateTimeParseException e) { return Result.failure("Datum mora biti u formatu yyyy-MM-dd"); }
        LocalDate end = start.plusDays(durationDays); // exclusive

        // Overlap detection: [start,end) overlaps existing ACTIVE booking if ranges intersect
        Set<String> ids = byHotel.getOrDefault(hotelId, Collections.emptySet());
        for (String id : ids) {
            BookingRecord existing = byId.get(id);
            if (existing == null || !(STATUS_ACTIVE.equals(existing.getStatus()) || STATUS_PAID.equals(existing.getStatus()))) continue;
            LocalDate eStart = LocalDate.parse(existing.getStartDate(), DATE_FMT);
            LocalDate eEnd = LocalDate.parse(existing.getEndDate(), DATE_FMT);
            boolean overlap = !(end.compareTo(eStart) <= 0 || start.compareTo(eEnd) >= 0);
            if (overlap) return Result.failure("Hotel je vec rezervisan za dati period");
        }

        String newId = String.valueOf(sequence.incrementAndGet());
        BookingRecord rec = new BookingRecord(newId, userId, hotelId, startDateStr, durationDays, STATUS_ACTIVE, end.format(DATE_FMT), pricePerNight);
        byId.put(newId, rec);
        byHotel.computeIfAbsent(hotelId, k -> ConcurrentHashMap.newKeySet()).add(newId);
    return Result.success("Rezervacija kreirana", rec);
    }

    public Result cancel(String bookingId) {
        if (blank(bookingId)) return Result.failure("booking_id required");
        BookingRecord rec = byId.get(bookingId);
        if (rec == null) return Result.failure("Rezervacija nije pronadjena");
        rec.cancel();
        return Result.success("Rezervacija otkazana", rec);
    }

    public Result cancelAuthorized(String bookingId, String userId) {
        if (blank(bookingId) || blank(userId)) return Result.failure("booking_id i user_id neophodni");
        BookingRecord rec = byId.get(bookingId);
        if (rec == null) return Result.failure("Rezervacija nije pronadjena");
    if (!rec.getUserId().equals(userId)) return Result.failure("Niste vlasnik rezervacije");
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

    public List<BookingRecord> listActiveByHotel(String hotelId) {
        Set<String> ids = byHotel.getOrDefault(hotelId, Collections.emptySet());
        return ids.stream()
                .map(byId::get)
                .filter(Objects::nonNull)
                .filter(b -> STATUS_ACTIVE.equals(b.getStatus()) || STATUS_PAID.equals(b.getStatus()))
                .sorted(Comparator.comparing(BookingRecord::getStartDate))
                .collect(Collectors.toList());
    }

    public BookingRecord findById(String bookingId) { return byId.get(bookingId); }

    public Result markPaid(String bookingId, String userId) {
        BookingRecord rec = byId.get(bookingId);
        if (rec == null) return Result.failure("Rezervacija nije pronadjena");
    if (!rec.getUserId().equals(userId)) return Result.failure("Niste vlasnik rezervacije");
    if (!STATUS_ACTIVE.equals(rec.getStatus())) return Result.failure("Ne moze se platiti: status=" + rec.getStatus());
        rec.paid();
        return Result.success("Uplata zabeležena", rec);
    }

    private boolean blank(String s) { return s == null || s.trim().isEmpty(); }

    public static class Result {
        public final boolean success; public final String message; public final BookingRecord record;
        private Result(boolean success, String message, BookingRecord record) { this.success = success; this.message = message; this.record = record; }
        public static Result success(String m, BookingRecord r) { return new Result(true, m, r); }
        public static Result failure(String m) { return new Result(false, m, null); }
    }
}
