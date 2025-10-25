package rs.raf.pds.v5.z2.gRPC.service;

import io.grpc.stub.StreamObserver;
import rs.raf.pds.v5.z2.gRPC.*;
import rs.raf.pds.v5.z2.gRPC.model.BookingRecord;
import rs.raf.pds.v5.z2.gRPC.store.InMemoryBookingStore;

public class BookingServiceImpl extends BookingServiceGrpc.BookingServiceImplBase {
    private final InMemoryBookingStore store;

    public BookingServiceImpl(InMemoryBookingStore store) { this.store = store; }

    @Override
    public void makeBooking(BookingRequest request, StreamObserver<BookingResponse> responseObserver) {
        InMemoryBookingStore.Result res = store.create(request.getUserId(), request.getRoomId(), request.getDate());
        BookingResponse.Builder b = BookingResponse.newBuilder()
                .setSuccess(res.success)
                .setMessage(res.record != null ? res.message + " | id=" + res.record.getBookingId() : res.message);
        responseObserver.onNext(b.build());
        responseObserver.onCompleted();
    }

    @Override
    public void cancelBooking(CancelRequest request, StreamObserver<CancelResponse> responseObserver) {
        InMemoryBookingStore.Result res = store.cancel(request.getBookingId());
        responseObserver.onNext(CancelResponse.newBuilder().setSuccess(res.success).setMessage(res.message).build());
        responseObserver.onCompleted();
    }

    @Override
    public void getBookings(UserRequest request, StreamObserver<BookingInfo> responseObserver) {
        for (BookingRecord rec : store.listByUser(request.getUserId())) {
            BookingInfo info = BookingInfo.newBuilder()
                    .setBookingId(rec.getBookingId())
                    .setRoomId(rec.getRoomId())
                    .setDate(rec.getDate())
                    .setStatus(rec.getStatus())
                    .build();
            responseObserver.onNext(info);
        }
        responseObserver.onCompleted();
    }
}
