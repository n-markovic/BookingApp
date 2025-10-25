package rs.raf.pds.v5.z2.gRPC.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import rs.raf.pds.v5.z2.gRPC.service.HotelServiceImpl;
import rs.raf.pds.v5.z2.gRPC.store.HotelRegistry;
import rs.raf.pds.v5.z2.gRPC.BookingServiceGrpc;

/** Standalone server exposing only HotelService */
public class HotelServer {
    private final int port;
    private Server server;
    // Bank status for HotelServer (simple shared field)
    private double bank = 0.0;

    public HotelServer(int port) { this.port = port; }

    private void start() throws Exception {
    HotelRegistry hotels = new HotelRegistry();
    // Create a BookingService stub to notify BookingServer about hotel updates (default location)
    String bookingHost = "localhost"; int bookingPort = 8090;
    io.grpc.ManagedChannel bookingCh = io.grpc.ManagedChannelBuilder.forAddress(bookingHost, bookingPort).usePlaintext().build();
    BookingServiceGrpc.BookingServiceBlockingStub bookingStub = BookingServiceGrpc.newBlockingStub(bookingCh);
        // Pass bookingStub to HotelServiceImpl so it can call back on price updates
        HotelServiceImpl impl = new HotelServiceImpl(hotels, null, null, bookingStub);
        server = ServerBuilder.forPort(port)
        .addService(impl)
                .build()
                .start();
        // Schedule periodic random discounts (every 30s)
        java.util.concurrent.ScheduledExecutorService sched = java.util.concurrent.Executors.newSingleThreadScheduledExecutor();
        sched.scheduleAtFixedRate(() -> {
            try {
                java.util.List<rs.raf.pds.v5.z2.gRPC.model.Hotel> all = hotels.listAll();
                if (all.isEmpty()) return;
                int idx = new java.util.Random().nextInt(all.size());
                rs.raf.pds.v5.z2.gRPC.model.Hotel chosen = all.get(idx);
                int pct = new int[]{20,30}[new java.util.Random().nextInt(2)];
                impl.applyDiscount(chosen.getId(), pct);
            } catch (Exception ignored) {}
        }, 15, 30, java.util.concurrent.TimeUnit.SECONDS);
    System.out.println("HotelServer pokrenut na portu " + port + " | stanje banke=" + bank);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Detektirano gasenje JVM-a, zatvaram HotelServer...");
            HotelServer.this.stop();
            System.out.println("HotelServer zaustavljen.");
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

    public double getBank() { return bank; }
    public void setBank(double bank) { this.bank = bank; }
}
