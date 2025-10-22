package rs.raf.pds.v5.z2.gRPC.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import rs.raf.pds.v5.z2.gRPC.*;

import java.util.Iterator;
import java.util.Scanner;

public class BookingClient implements AutoCloseable {

    private final ManagedChannel bookingChannel;
    private final ManagedChannel hotelChannel;
    private final BookingServiceGrpc.BookingServiceBlockingStub bookingBlocking;
    private final HotelServiceGrpc.HotelServiceBlockingStub hotelBlocking;

    public BookingClient(String bookingHost, int bookingPort, String hotelHost, int hotelPort) {
        bookingChannel = ManagedChannelBuilder.forAddress(bookingHost, bookingPort).usePlaintext().build();
        hotelChannel = (bookingHost.equals(hotelHost) && bookingPort == hotelPort)
                ? bookingChannel
                : ManagedChannelBuilder.forAddress(hotelHost, hotelPort).usePlaintext().build();
        bookingBlocking = BookingServiceGrpc.newBlockingStub(bookingChannel);
        hotelBlocking = HotelServiceGrpc.newBlockingStub(hotelChannel);
    }

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
            System.out.println("- " + b.getBookingId() + " hotel=" + b.getHotelId() + " start=" + b.getStartDate() + " +" + b.getDurationDays() + "d end=" + b.getEndDate() + " status=" + b.getStatus());
        }
    }

    @Override
    public void close() {
    if (bookingChannel != null) bookingChannel.shutdown();
    if (hotelChannel != null && hotelChannel != bookingChannel) hotelChannel.shutdown();
    }

    public static void main(String[] args) {
        // Argument patterns supported now:
        // 1) java -cp ... BookingClient <username>
        // 2) java -cp ... BookingClient <host> <port> <username>
        // 3) java -cp ... BookingClient <host> <username>   (port defaults)
    String bookingHost = "localhost"; int bookingPort = 8090;
    String hotelHost = "localhost"; int hotelPort = 8100;
        String sessionUser = null;

        // Arg patterns (flexible simplified):
        // 1) <user>
        // 2) <bookingHost> <bookingPort> <user>
        // 3) <bookingHost> <bookingPort> <hotelHost> <hotelPort> <user>
        if (args.length == 1) {
            sessionUser = args[0];
        } else if (args.length == 3) {
            bookingHost = args[0];
            try { bookingPort = Integer.parseInt(args[1]); } catch (NumberFormatException ignored) {}
            sessionUser = args[2];
        } else if (args.length >= 5) {
            bookingHost = args[0];
            try { bookingPort = Integer.parseInt(args[1]); } catch (NumberFormatException ignored) {}
            hotelHost = args[2];
            try { hotelPort = Integer.parseInt(args[3]); } catch (NumberFormatException ignored) {}
            sessionUser = args[4];
        }

        try (BookingClient client = new BookingClient(bookingHost, bookingPort, hotelHost, hotelPort); Scanner in = new Scanner(System.in)) {
            System.out.println("BookingClient connected (booking=" + bookingHost + ":" + bookingPort + ", hotel=" + hotelHost + ":" + hotelPort + ")");
            if (sessionUser != null) {
                System.out.println("Session user set to '" + sessionUser + "'. You can omit the user in 'book' and 'list' commands.");
            }
            System.out.println("Commands: book <hotel> <yyyy-MM-dd> <durationDays> | cancel <bookingId> | list | ask <city?> <maxDistanceM> <minStars> | quit");
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
                        case "make": { // alias
                            if (sessionUser != null) {
                                // book <hotel> <startDate> <duration>
                                if (parts.length < 4) { System.out.println("Usage: book <hotel> <yyyy-MM-dd> <durationDays>"); break; }
                                int dur;
                                try { dur = Integer.parseInt(parts[3]); } catch (NumberFormatException e) { System.out.println("durationDays must be integer"); break; }
                                String msg = client.make(sessionUser, parts[1], parts[2], dur);
                                System.out.println(msg);
                            } else {
                                // book <user> <hotel> <startDate> <duration>
                                if (parts.length < 5) { System.out.println("Usage: book <user> <hotel> <yyyy-MM-dd> <durationDays>"); break; }
                                int dur;
                                try { dur = Integer.parseInt(parts[4]); } catch (NumberFormatException e) { System.out.println("durationDays must be integer"); break; }
                                String msg = client.make(parts[1], parts[2], parts[3], dur);
                                System.out.println(msg);
                            }
                            break; }
                        case "cancel": {
                            if (parts.length < 2) { System.out.println("Usage: cancel <bookingId>"); break; }
                            String user = sessionUser;
                            if (user == null) {
                                System.out.println("No session user bound; cancellation requires ownership. Provide username first when starting client.");
                            }
                            System.out.println(client.cancel(parts[1], user));
                            break; }
                        case "list":
                            if (sessionUser != null) {
                                client.list(sessionUser);
                            } else {
                                if (parts.length < 2) { System.out.println("Usage: list <user>"); break; }
                                client.list(parts[1]);
                            }
                            break;
                        case "ask": {
                            // ask <city?> <maxDistanceM> <minStars>
                            String city = ""; int maxDist; int minStars; int offset = 1;
                            if (parts.length == 4) { // city provided
                                city = parts[1]; offset = 2;
                            }
                            if (parts.length - offset < 2) { System.out.println("Usage: ask <city?> <maxDistanceMeters> <minStars>"); break; }
                            try {
                                maxDist = Integer.parseInt(parts[offset]);
                                minStars = Integer.parseInt(parts[offset+1]);
                            } catch (NumberFormatException nfe) { System.out.println("Numbers expected for distance and stars"); break; }
                            if (minStars < 3) minStars = 3; if (minStars > 5) minStars = 5;
                            AskRequest req = AskRequest.newBuilder()
                                    .setCity(city)
                                    .setMaxDistanceM(maxDist)
                                    .setMinStars(minStars)
                                    .build();
                            AskResponse resp = client.hotelBlocking.askHotels(req);
                            if (resp.getHotelsCount() == 0) { System.out.println("No hotels found."); break; }
                            System.out.println("Hotels:");
                            for (Hotel h : resp.getHotelsList()) {
                                System.out.print("- " + h.getName() + " (" + h.getStars() + "*) city=" + h.getCity() + " dist=" + h.getDistanceM() + "m avail=" + h.getAvailable());
                                if (h.getOccupiedCount() > 0) {
                                    System.out.print(" occupied=[");
                                    for (int i=0;i<h.getOccupiedCount();i++) {
                                        OccupancyPeriod op = h.getOccupied(i);
                                        System.out.print(op.getStartDate()+"->"+op.getEndDate());
                                        if (i < h.getOccupiedCount()-1) System.out.print(", ");
                                    }
                                    System.out.print("]");
                                }
                                System.out.println();
                            }
                            break; }
                        default:
                            System.out.println("Unknown command");
                    }
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        }
    }
}
