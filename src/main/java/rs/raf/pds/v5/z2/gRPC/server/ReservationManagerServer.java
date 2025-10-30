package rs.raf.pds.v5.z2.gRPC.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import rs.raf.pds.v5.z2.gRPC.service.BookingServiceImpl;
import rs.raf.pds.v5.z2.gRPC.store.InMemoryBookingStore;

/**
 * Reservation manager server hosting only BookingService.
 * Requires hotel service host/port to validate hotels remotely.
 */
public class ReservationManagerServer {
    private final int port;
    private final String hotelHost;
    private final int hotelPort;
    private Server server;

    public ReservationManagerServer(int port, String hotelHost, int hotelPort) {
        this.port = port;
        this.hotelHost = hotelHost;
        this.hotelPort = hotelPort;
    }

    private void start() throws Exception {
        InMemoryBookingStore store = new InMemoryBookingStore();
        server = ServerBuilder.forPort(port)
                .addService(new BookingServiceImpl(store, hotelHost, hotelPort))
                .build()
                .start();
        System.out.println("ReservationManagerServer started on port " + port + " (hotel service " + hotelHost + ":" + hotelPort + ")");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutdown detected, stopping ReservationManagerServer...");
            ReservationManagerServer.this.stop();
        }));
    }

    private void stop() { if (server != null) server.shutdown(); }

    private void blockUntilShutdown() throws InterruptedException { if (server != null) server.awaitTermination(); }

    public static void main(String[] args) throws Exception {
        int port = 8090;
        String hotelHost = "localhost";
        int hotelPort = 8100;
        // Args: [port] [hotelHost] [hotelPort]
        if (args.length > 0) { try { port = Integer.parseInt(args[0]); } catch (NumberFormatException ignored) {} }
        if (args.length > 1) { hotelHost = args[1]; }
        if (args.length > 2) { try { hotelPort = Integer.parseInt(args[2]); } catch (NumberFormatException ignored) {} }
        ReservationManagerServer s = new ReservationManagerServer(port, hotelHost, hotelPort);
        s.start();
        s.blockUntilShutdown();
    }
}
