package rs.raf.pds.v5.z2.gRPC.store;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Simple in-memory bank service shared by servers and services.
 * Thread-safe and process-local; for demo only.
 */
public class BankService {
    private static final BankService INSTANCE = new BankService();
    private final ConcurrentMap<String, Double> balances = new ConcurrentHashMap<>();

    private BankService() {
        // initialize server accounts
        balances.putIfAbsent("booking_server", 0.0);
        balances.putIfAbsent("hotel_server", 0.0);
    }

    public static BankService getInstance() { return INSTANCE; }

    public double getBalance(String id) { return balances.getOrDefault(id, 0.0); }

    public synchronized double deposit(String id, double amount) {
        if (amount <= 0) return getBalance(id);
        double prev = getBalance(id);
        double next = prev + amount;
        balances.put(id, next);
        return next;
    }

    /**
     * Try to withdraw amount from user and split to booking_server (10%) and hotel_server (90%).
     * Returns true if successful and applies the transfers.
     */
    public synchronized boolean chargeAndSplit(String userId, double amount) {
        double userBal = getBalance(userId);
        if (userBal < amount) return false;
        // Deduct from user
        balances.put(userId, userBal - amount);
        // Split
        double bookingCut = amount * 0.10;
        double hotelCut = amount - bookingCut;
        balances.put("booking_server", getBalance("booking_server") + bookingCut);
        balances.put("hotel_server", getBalance("hotel_server") + hotelCut);
        return true;
    }

    /**
     * Withdraw the amount from user's balance and credit only the booking_server cut locally.
     * Vraca iznos koji pripada hotelu (>=0) kada je uspesno, ili -1 kada nema dovoljno sredstava.
     *
     * This is intended for multi-process setups where the booking server keeps its cut locally
     * and then forwards the hotel share to the hotel process which will credit its own account.
     */
    public synchronized double withdrawAndKeepBookingCutReturnHotelShare(String userId, double amount) {
        double userBal = getBalance(userId);
        if (userBal < amount) return -1.0;
        // Deduct full amount from user
        balances.put(userId, userBal - amount);
        double bookingCut = amount * 0.10;
        double hotelCut = amount - bookingCut;
        // Credit booking server only; hotel will be credited by its own process via RPC
        balances.put("booking_server", getBalance("booking_server") + bookingCut);
        return hotelCut;
    }
}
