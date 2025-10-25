package rs.raf.pds.v5.z2.gRPC.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import rs.raf.pds.v5.z2.gRPC.*;

import java.util.Iterator;
import java.util.Scanner;

public class BookingClient implements AutoCloseable {

    private final ManagedChannel channel;
    private final BookingServiceGrpc.BookingServiceBlockingStub blocking;

    public BookingClient(String host, int port) {
        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        blocking = BookingServiceGrpc.newBlockingStub(channel);
    }

    public String make(String user, String room, String date) {
        BookingResponse resp = blocking.makeBooking(BookingRequest.newBuilder()
                .setUserId(user)
                .setRoomId(room)
                .setDate(date)
                .build());
        return resp.getMessage();
    }

    public String cancel(String bookingId) {
        CancelResponse resp = blocking.cancelBooking(CancelRequest.newBuilder().setBookingId(bookingId).build());
        return resp.getMessage();
    }

    public void list(String userId) {
        Iterator<BookingInfo> it = blocking.getBookings(UserRequest.newBuilder().setUserId(userId).build());
        while (it.hasNext()) {
            BookingInfo b = it.next();
            System.out.println("- " + b.getBookingId() + " soba=" + b.getRoomId() + " datum=" + b.getDate() + " status=" + b.getStatus());
        }
    }

    @Override
    public void close() {
        if (channel != null) channel.shutdown();
    }

    public static void main(String[] args) {
        // Argument patterns supported now:
        // 1) java -cp ... BookingClient <username>
        // 2) java -cp ... BookingClient <host> <port> <username>
        // 3) java -cp ... BookingClient <host> <username>   (port defaults)
        String host = "localhost";
        int port = 8090;
        String sessionUser = null;

        if (args.length == 1) {
            sessionUser = args[0];
        } else if (args.length == 2) {
            // Could be host + user or user + something else; decide by trying to parse second as int
            try {
                port = Integer.parseInt(args[1]);
                host = args[0];
            } catch (NumberFormatException e) {
                host = "localhost"; // keep default host
                sessionUser = args[0]; // fallback treat first as user, ignore second ambiguous
            }
            if (sessionUser == null) sessionUser = args[1];
        } else if (args.length >= 3) {
            host = args[0];
            try { port = Integer.parseInt(args[1]); } catch (NumberFormatException ignored) {}
            sessionUser = args[2];
        }

        try (BookingClient client = new BookingClient(host, port); Scanner in = new Scanner(System.in)) {
            System.out.println("BookingClient povezan sa " + host + ":" + port);
            if (sessionUser != null) {
                System.out.println("Korisnik sesije postavljen na '" + sessionUser + "'. Mozete izostaviti korisnika u komandama 'book' i 'list'.");
            }
            System.out.println("Komande: book <Hotel> <datum yyyy-MM-dd> | cancel <IDrezervacije> | list | quit");
            while (true) {
                System.out.print("> ");
                if (!in.hasNextLine()) break;
                String line = in.nextLine().trim();
                if (line.isEmpty()) continue;
                if (line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit")) break;
                String[] parts = line.split("\\s+");
                try {
                    switch (parts[0]) {
                        case "book":
                        case "make": { // keep 'make' as alias
                            if (sessionUser != null) {
                                // Expected: book <room> <date>
                                if (parts.length < 3) { System.out.println("Upotreba: book <soba> <yyyy-MM-dd>"); break; }
                                String msg = client.make(sessionUser, parts[1], parts[2]);
                                System.out.println(msg);
                            } else {
                                if (parts.length < 4) { System.out.println("Upotreba: book <user> <soba> <yyyy-MM-dd>"); break; }
                                String msg = client.make(parts[1], parts[2], parts[3]);
                                System.out.println(msg);
                            }
                            break; }
                        case "cancel":
                            if (parts.length < 2) { System.out.println("Upotreba: cancel <bookingId>"); break; }
                            System.out.println(client.cancel(parts[1]));
                            break;
                        case "list":
                            if (sessionUser != null) {
                                client.list(sessionUser);
                            } else {
                                if (parts.length < 2) { System.out.println("Upotreba: list <user>"); break; }
                                client.list(parts[1]);
                            }
                            break;
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
