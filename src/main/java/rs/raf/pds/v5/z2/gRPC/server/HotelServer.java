package rs.raf.pds.v5.z2.gRPC.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import rs.raf.pds.v5.z2.gRPC.service.HotelServiceImpl;
import rs.raf.pds.v5.z2.gRPC.store.HotelRegistry;

/** Standalone server exposing only HotelService */
public class HotelServer {
    private final int port;
    private Server server;

    public HotelServer(int port) { this.port = port; }

    private void start() throws Exception {
    HotelRegistry hotels = new HotelRegistry();
    // Occupancy can be omitted here to keep Hotel service independent
        server = ServerBuilder.forPort(port)
        .addService(new HotelServiceImpl(hotels))
                .build()
                .start();
        System.out.println("HotelServer started on port " + port);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("JVM shutdown detected, shutting down HotelServer...");
            HotelServer.this.stop();
            System.out.println("HotelServer stopped.");
        }));
    }

    private void stop() { if (server != null) server.shutdown(); }

    private void blockUntilShutdown() throws InterruptedException { if (server != null) server.awaitTermination(); }

    public static void main(String[] args) throws Exception {
        int port = 8100; // distinct default port
        if (args.length > 0) { try { port = Integer.parseInt(args[0]); } catch (NumberFormatException ignored) {} }
        HotelServer s = new HotelServer(port);
        s.start();
        s.blockUntilShutdown();
    }
}
