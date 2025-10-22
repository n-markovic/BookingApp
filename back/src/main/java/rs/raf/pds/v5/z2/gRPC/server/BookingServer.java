package rs.raf.pds.v5.z2.gRPC.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import rs.raf.pds.v5.z2.gRPC.service.BookingServiceImpl;
import rs.raf.pds.v5.z2.gRPC.store.InMemoryBookingStore;

public class BookingServer {
    private final int port;
    private Server server;

    public BookingServer(int port) { this.port = port; }

    private void start() throws Exception {
        InMemoryBookingStore store = new InMemoryBookingStore();
        server = ServerBuilder.forPort(port)
                .addService(new BookingServiceImpl(store))
                .build()
                .start();
        System.out.println("gRPC BookingServer started on port " + port);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("JVM shutdown detected, shutting down gRPC server...");
            BookingServer.this.stop();
            System.out.println("Server shut down.");
        }));
    }

    private void stop() { if (server != null) server.shutdown(); }

    private void blockUntilShutdown() throws InterruptedException { if (server != null) server.awaitTermination(); }

    public static void main(String[] args) throws Exception {
        int port = 8090;
        if (args.length > 0) { try { port = Integer.parseInt(args[0]); } catch (NumberFormatException ignored) {} }
        BookingServer s = new BookingServer(port);
        s.start();
        s.blockUntilShutdown();
    }
}
