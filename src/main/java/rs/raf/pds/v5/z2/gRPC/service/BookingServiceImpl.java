package rs.raf.pds.v5.z2.gRPC.service;

import io.grpc.stub.StreamObserver;
import rs.raf.pds.v5.z2.gRPC.*;
import rs.raf.pds.v5.z2.gRPC.model.BookingRecord;
import rs.raf.pds.v5.z2.gRPC.store.InMemoryBookingStore;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class BookingServiceImpl extends BookingServiceGrpc.BookingServiceImplBase {
    private final InMemoryBookingStore store;
    private final HotelServiceGrpc.HotelServiceBlockingStub hotelBlocking;

    public BookingServiceImpl(InMemoryBookingStore store, String hotelHost, int hotelPort) {
        this.store = store;
        ManagedChannel ch = ManagedChannelBuilder.forAddress(hotelHost, hotelPort).usePlaintext().build();
        this.hotelBlocking = HotelServiceGrpc.newBlockingStub(ch);
    }

    // Convenience: create default in-memory store internally
    public BookingServiceImpl(String hotelHost, int hotelPort) {
        this(new InMemoryBookingStore(), hotelHost, hotelPort);
    }

    @Override
    public void makeBooking(BookingRequest request, StreamObserver<BookingResponse> responseObserver) {
    String hotelId = request.getHotelId();
    // Validate hotel exists
    HotelResponse hotelResp = hotelBlocking.getHotel(HotelIdRequest.newBuilder().setIdOrName(hotelId).build());
    if (!hotelResp.getFound()) {
        responseObserver.onNext(BookingResponse.newBuilder()
            .setSuccess(false)
            .setMessage("Unknown hotel: " + hotelId)
            .build());
        responseObserver.onCompleted();
        return;
    }
    rs.raf.pds.v5.z2.gRPC.Hotel h = hotelResp.getHotel();
    if (!h.getAvailable()) {
        responseObserver.onNext(BookingResponse.newBuilder()
            .setSuccess(false)
            .setMessage("Hotel not available: " + h.getName())
            .build());
        responseObserver.onCompleted();
        return;
    }
    InMemoryBookingStore.Result res = store.create(request.getUserId(), h.getId(), request.getStartDate(), request.getDurationDays());
    BookingResponse.Builder b = BookingResponse.newBuilder()
        .setSuccess(res.success)
        .setMessage(res.record != null ? res.message + " | id=" + res.record.getBookingId() : res.message);
    responseObserver.onNext(b.build());
    responseObserver.onCompleted();
    }

    @Override
    public void cancelBooking(CancelRequest request, StreamObserver<CancelResponse> responseObserver) {
        // Authorization: require user_id and ownership
        InMemoryBookingStore.Result res;
        String user = request.getUserId();
        if (user != null && !user.isEmpty()) {
            res = store.cancelAuthorized(request.getBookingId(), user);
        } else {
            res = store.cancel(request.getBookingId()); // legacy fallback
        }
        responseObserver.onNext(CancelResponse.newBuilder()
                .setSuccess(res.success)
                .setMessage(res.message)
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void getBookings(UserRequest request, StreamObserver<BookingInfo> responseObserver) {
        for (BookingRecord rec : store.listByUser(request.getUserId())) {
            BookingInfo info = BookingInfo.newBuilder()
                    .setBookingId(rec.getBookingId())
                    .setHotelId(rec.getHotelId())
                    .setStartDate(rec.getStartDate())
                    .setDurationDays(rec.getDurationDays())
                    .setEndDate(rec.getEndDate())
                    .setStatus(rec.getStatus())
                    .build();
            responseObserver.onNext(info);
        }
        responseObserver.onCompleted();
    }

}
