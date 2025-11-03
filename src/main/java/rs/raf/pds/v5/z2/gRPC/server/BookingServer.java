package rs.raf.pds.v5.z2.gRPC.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import rs.raf.pds.v5.z2.gRPC.service.BookingServiceImpl;

public class BookingServer {
    private final int port;
    private final String hotelHost;
    private final int hotelPort;
    private Server server;
    // Bank status for BookingServer (simple shared field)
    private double bank = 0.0;

    public BookingServer(int port, String hotelHost, int hotelPort) {
        this.port = port;
        this.hotelHost = hotelHost;
        this.hotelPort = hotelPort;
    }

    private void start() throws Exception {
        server = ServerBuilder.forPort(port)
        .addService(new BookingServiceImpl(hotelHost, hotelPort))
                .build()
                .start();
    System.out.println("gRPC BookingServer pokrenut na portu " + port + " | stanje banke=" + bank);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Detektirano gasenje JVM-a, zatvaram gRPC server...");
            BookingServer.this.stop();
            System.out.println("Server zaustavljen.");
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
        BookingServer s = new BookingServer(port, hotelHost, hotelPort);
        s.start();
        s.blockUntilShutdown();
    }

    public double getBank() { return bank; }
    public void setBank(double bank) { this.bank = bank; }
}
