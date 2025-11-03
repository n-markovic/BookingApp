package rs.raf.pds.v5.z2.gRPC.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import rs.raf.pds.v5.z2.gRPC.*;

import java.util.Iterator;
import java.util.Scanner;
import java.io.IOException;

public class BookingClient implements AutoCloseable {

    private final ManagedChannel bookingChannel;
    private final BookingServiceGrpc.BookingServiceBlockingStub bookingBlocking;
    // Client-side bank balance (local bookkeeping)
    private double bank = 0.0;
    // Marquee (notification bar)
    private Thread marqueeThread;
    private volatile boolean marqueeRunning = false;
    // Thread listening for ESC to stop marquee
    private Thread marqueeKeyThread;

    public BookingClient(String bookingHost, int bookingPort) {
        bookingChannel = ManagedChannelBuilder.forAddress(bookingHost, bookingPort).usePlaintext().build();
        bookingBlocking = BookingServiceGrpc.newBlockingStub(bookingChannel);
    }

    // Start a notification subscription (blockingStream consumer runs in background)
    public void startNotificationSubscription(String userId) {
        if (userId == null || userId.trim().isEmpty()) return;
        // Use async stub for streaming
        BookingServiceGrpc.BookingServiceStub async = BookingServiceGrpc.newStub(bookingChannel);
        async.subscribeNotifications(UserRequest.newBuilder().setUserId(userId).build(), new io.grpc.stub.StreamObserver<Notification>() {
            @Override public void onNext(Notification value) {
                System.out.println("Obavestenje o rezervaciji: " + value.getText());
            }
            @Override public void onError(Throwable t) {}
            @Override public void onCompleted() {}
        });
        // status bar can be started via command: 'status'
    }

    // Start a simple marquee that scrolls hotel summaries from right to left.
    // Format per item: "<Grad> <Hotel> <Udaljenost> <Kategorija> <Current Price> <Min Price> <Max Price>"
    public void startMarquee() {
        if (marqueeRunning) return;
        marqueeRunning = true;
        // start a small key-listener thread that watches for ESC (27) to stop the marquee
        marqueeKeyThread = new Thread(() -> {
            try {
                while (marqueeRunning) {
                    int r = System.in.read(); // blocking read
                    if (r == 69 ) { // ESC
                        marqueeRunning = false;
                        System.out.println("\nStatus bar zaustavljen. Koristite 'status' da ga pokrenete ponovo.");
                        break;
                    }
                    if (r == -1) break;
                }
            } catch (IOException ignored) {}
        }, "marquee-key-listener");
        marqueeKeyThread.setDaemon(true);
        marqueeKeyThread.start();
        marqueeThread = new Thread(() -> {
            try {
                String scroll = "";
                long lastFetch = 0;
                while (marqueeRunning) {
                    long now = System.currentTimeMillis();
                    if (scroll.isEmpty() || now - lastFetch > 30000) { // refresh every 30s
                        try {
                            AskRequest req = AskRequest.newBuilder().setCity("").setMaxDistanceM(Integer.MAX_VALUE).setMinStars(3).build();
                            AskResponse resp = bookingBlocking.askHotels(req);
                            StringBuilder sb = new StringBuilder();
                            for (Hotel h : resp.getHotelsList()) {
                                if (sb.length() > 0) sb.append("   |   ");
                                sb.append(h.getCity()).append(' ').append(h.getName()).append(' ')
                                        .append(h.getDistanceM()).append('m').append(' ')
                                        .append(h.getStars()).append("*")
                                        .append(' ').append(h.getLastPrice())
                                        .append(' ').append(h.getMinPrice())
                                        .append(' ').append(h.getMaxPrice());
                            }
                            if (sb.length() == 0) sb.append("Nema hotela za prikaz");
                            scroll = sb.toString() + "     ";
                            lastFetch = now;
                        } catch (Exception e) {
                            // ignore and retry later
                        }
                    }

                    if (scroll.length() > 0) {
                        for (int i = 0; i < scroll.length() && marqueeRunning; i++) {
                            String s = scroll.substring(i) + scroll.substring(0, i);
                            String out = s.length() > 100 ? s.substring(0, 100) : s;
                            System.out.print('\r' + out);
                            System.out.flush();
                            try { Thread.sleep(150); } catch (InterruptedException ie) { /* ignore */ }
                        }
                    } else {
                        try { Thread.sleep(1000); } catch (InterruptedException ie) { /* ignore */ }
                    }
                }
            } finally {
                System.out.print('\r');
                System.out.flush();
            }
        }, "marquee-thread");
        marqueeThread.setDaemon(true);
        marqueeThread.start();
    }

    public void setBank(double amount) { this.bank = amount; }
    public double getBank() { return bank; }

    public String make(String user, String hotel, String startDate, int durationDays) {
    BookingResponse resp = bookingBlocking.makeBooking(BookingRequest.newBuilder()
                .setUserId(user)
                .setHotelId(hotel)
                .setStartDate(startDate)
                .setDurationDays(durationDays)
                .build());
        return resp.getMessage();
    }

    public String cancel(String bookingId, String userId) {
        CancelRequest.Builder b = CancelRequest.newBuilder().setBookingId(bookingId);
        if (userId != null) b.setUserId(userId);
    CancelResponse resp = bookingBlocking.cancelBooking(b.build());
        return resp.getMessage();
    }

    public void list(String userId) {
    Iterator<BookingInfo> it = bookingBlocking.getBookings(UserRequest.newBuilder().setUserId(userId).build());
        while (it.hasNext()) {
            BookingInfo b = it.next();
            System.out.println("- " + b.getBookingId() + " hotel=" + b.getHotelId() + " start=" + b.getStartDate() + " +" + b.getDurationDays() + "d kraj=" + b.getEndDate() + " status=" + b.getStatus());
        }
    }

    @Override
    public void close() {
    // Stop marquee if running
    try {
        marqueeRunning = false;
        if (marqueeThread != null) marqueeThread.join(200);
        if (marqueeKeyThread != null) marqueeKeyThread.join(200);
    } catch (InterruptedException ignored) {}
    if (bookingChannel != null) bookingChannel.shutdown();
    }

    public static void main(String[] args) {
    String bookingHost = "localhost"; int bookingPort = 8090;
        String sessionUser = null;
        Double startingMoney = null;

        if (args.length == 1) {
            sessionUser = args[0];
        } else if (args.length == 2) {
            // could be: <user> <startingMoney>
            sessionUser = args[0];
            try { startingMoney = Double.parseDouble(args[1]); } catch (NumberFormatException ignored) { startingMoney = null; }
        } else if (args.length == 3) {
            bookingHost = args[0];
            try { bookingPort = Integer.parseInt(args[1]); } catch (NumberFormatException ignored) {}
            sessionUser = args[2];
        } else if (args.length >= 4) {
            bookingHost = args[0];
            try { bookingPort = Integer.parseInt(args[1]); } catch (NumberFormatException ignored) {}
            sessionUser = args[2];
            try { startingMoney = Double.parseDouble(args[3]); } catch (NumberFormatException ignored) { startingMoney = null; }
        }

        try (BookingClient client = new BookingClient(bookingHost, bookingPort); Scanner in = new Scanner(System.in)) {
            System.out.println("BookingClient povezan (booking=" + bookingHost + ":" + bookingPort + ")");
            if (sessionUser != null) {
                System.out.println("Korisnik sesije postavljen na '" + sessionUser + "'. Mozete izostaviti korisnika u komandama 'book' i 'list'.");
            }
            // Determine starting money and deposit to server-side account
            double deposited = 0.0;
            if (startingMoney != null) {
                deposited = startingMoney;
            } else {
                // Prompt user to enter starting money as required
                Double entered = null;
                while (entered == null) {
                    System.out.print("Unesite pocetni iznos novca (npr. 500): ");
                    String line = in.hasNextLine() ? in.nextLine().trim() : "";
                    if (line.isEmpty()) continue;
                    try { entered = Double.parseDouble(line); } catch (NumberFormatException nfe) { System.out.println("Nevazeci broj, pokusajte ponovo."); }
                }
                deposited = entered;
            }

            // Deposit to server-side bank for this user
            DepositResponse dresp = client.bookingBlocking.deposit(DepositRequest.newBuilder().setUserId(sessionUser == null ? "gost" : sessionUser).setAmount(deposited).build());
            if (dresp.getSuccess()) {
                client.setBank(dresp.getBalance());
            } else {
                client.setBank(0.0);
            }
            // Start receiving notifications for this user
            if (sessionUser != null) client.startNotificationSubscription(sessionUser);
            System.out.println(String.format("Pocetna uplata izvrsena: %.2f | stanje na serveru: %.2f", deposited, client.getBank()));
            System.out.println("Komande: \n- book <Hotel> <datum yyyy-MM-dd> <trajanjeBoravka> \n- pay <IDrezervacije> \n- cancel <IDrezervacije> \n- list \n- ask <Grad?> <MaxDistanca[m]> <minKategorija> \n- bank \n- status - rotirajuci status bar sa hotelima \n- quit");
            while (true) {
                System.out.print("> ");
                if (!in.hasNextLine()) break;
                String line = in.nextLine().trim();
                if (line.isEmpty()) continue;
                if (line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit")) break;
                String[] parts = line.split("\\s+");
                try {
                    switch (parts[0]) {
                        case "bank":
                        case "balance": {
                            System.out.println(String.format("Stanje racuna: %.2f", client.getBank()));
                            break;
                        }
                        case "book":
                        case "make": { // alias
                            if (sessionUser != null) {
                                // book <hotel> <startDate> <duration>
                                if (parts.length < 4) { System.out.println("Upotreba: book <Hotel> <datum yyyy-MM-dd> <trajanjeBoravka>"); break; }
                                int dur;
                                try { dur = Integer.parseInt(parts[3]); } catch (NumberFormatException e) { System.out.println("trajanjeBoravka mora biti ceo broj"); break; }
                                String msg = client.make(sessionUser, parts[1], parts[2], dur);
                                System.out.println(msg);
                            } else {
                                // book <user> <hotel> <startDate> <duration>
                                if (parts.length < 5) { System.out.println("Upotreba: book <Korisnik> <Hotel> <yyyy-MM-dd> <trajanjeBoravka>"); break; }
                                int dur;
                                try { dur = Integer.parseInt(parts[4]); } catch (NumberFormatException e) { System.out.println("trajanjeBoravka mora biti ceo broj"); break; }
                                String msg = client.make(parts[1], parts[2], parts[3], dur);
                                System.out.println(msg);
                            }
                            break; }
                        case "cancel": {
                            if (parts.length < 2) { System.out.println("Upotreba: cancel <IDrezervacije>"); break; }
                            String user = sessionUser;
                            if (user == null) {
                                System.out.println("Nema povezanog korisnika sesije; otkazivanje zahteva vlasnistvo. Pokrenite klijent sa korisnickim imenom.");
                            }
                            System.out.println(client.cancel(parts[1], user));
                            break; }
                        case "list":
                            if (sessionUser != null) {
                                client.list(sessionUser);
                            } else {
                                if (parts.length < 2) { System.out.println("Upotreba: list <Korisnik>"); break; }
                                client.list(parts[1]);
                            }
                            break;
                        case "pay": {
                                if (parts.length < 2) { System.out.println("Upotreba: pay <IDrezervacije>"); break; }
                            String user = sessionUser;
                            if (user == null) { System.out.println("Postavite korisnika sesije pokretanjem klijenta sa korisnickim imenom."); break; }
                PaymentResponse resp = client.bookingBlocking.pay(PaymentRequest.newBuilder()
                    .setBookingId(parts[1])
                    .setUserId(user)
                    .build());
                System.out.println(resp.getMessage());
                // Update local view of server-side bank if provided
                try { client.setBank(resp.getBalance()); } catch (Exception ignored) {}
                            break; }
                        case "ask": {
                            // ask <city?> <maxDistanceM> <minStars>
                            String city = ""; int maxDist; int minStars; int offset = 1;
                            if (parts.length == 4) { // city provided
                                city = parts[1]; offset = 2;
                            }
                            if (parts.length - offset < 2) { System.out.println("Upotreba: ask <city?> <maxDistanceMeters> <minStars>"); break; }
                            try {
                                maxDist = Integer.parseInt(parts[offset]);
                                minStars = Integer.parseInt(parts[offset+1]);
                            } catch (NumberFormatException nfe) { System.out.println("Ocekuju se brojevi za rastojanje i zvezdice"); break; }
                            if (minStars < 3) minStars = 3; if (minStars > 5) minStars = 5;
                AskRequest.Builder ab = AskRequest.newBuilder()
                    .setCity(city)
                    .setMaxDistanceM(maxDist)
                    .setMinStars(minStars);
                if (sessionUser != null) ab.setUserId(sessionUser);
                AskRequest req = ab.build();
                            AskResponse resp = client.bookingBlocking.askHotels(req);
                            if (resp.getHotelsCount() == 0) { System.out.println("Nema pronadjenih hotela."); break; }
                            System.out.println("Hoteli:");
                            for (Hotel h : resp.getHotelsList()) {
                                // Print basic hotel info (omit avail flag) and show booked date ranges if any
                                System.out.print("- " + h.getName() + " (" + h.getStars() + "*) grad=" + h.getCity() + " dist=" + h.getDistanceM() + "m");
                                    System.out.print(" cena=" + h.getLastPrice() + " (min=" + h.getMinPrice() + ", max=" + h.getMaxPrice() + ")");
                                    if (h.getOccupiedCount() > 0) {
                                        System.out.print(" rezervisan=[");
                                        for (int i=0;i<h.getOccupiedCount();i++) {
                                            OccupancyPeriod op = h.getOccupied(i);
                                            System.out.print(op.getStartDate()+"->"+op.getEndDate());
                                            if (i < h.getOccupiedCount()-1) System.out.print(", ");
                                        }
                                        System.out.print("]");
                                    } else {
                                        System.out.print(" dostupan");
                                    }
                                System.out.println();
                            }
                            break; }
                        case "status": {
                            // toggle status bar
                            if (client.marqueeRunning) {
                                client.marqueeRunning = false;
                                System.out.println("Status zaustavljen.");
                            } else {
                                client.startMarquee();
                                System.out.println("Status pokrenut. Pritisnite ESC da zaustavite.");
                            }
                            break;
                        }
                        default:
                            System.out.println("Nepoznata komanda");
                    }
                } catch (Exception e) {
                    System.out.println("Greska: " + e.getMessage());
                }
            }
        }
    }
}
