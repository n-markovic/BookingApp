package rs.raf.pds.v5.z2.gRPC;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.54.0)",
    comments = "Source: booking_service.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class BookingServiceGrpc {

  private BookingServiceGrpc() {}

  public static final String SERVICE_NAME = "BookingService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<rs.raf.pds.v5.z2.gRPC.BookingRequest,
      rs.raf.pds.v5.z2.gRPC.BookingResponse> getMakeBookingMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "MakeBooking",
      requestType = rs.raf.pds.v5.z2.gRPC.BookingRequest.class,
      responseType = rs.raf.pds.v5.z2.gRPC.BookingResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<rs.raf.pds.v5.z2.gRPC.BookingRequest,
      rs.raf.pds.v5.z2.gRPC.BookingResponse> getMakeBookingMethod() {
    io.grpc.MethodDescriptor<rs.raf.pds.v5.z2.gRPC.BookingRequest, rs.raf.pds.v5.z2.gRPC.BookingResponse> getMakeBookingMethod;
    if ((getMakeBookingMethod = BookingServiceGrpc.getMakeBookingMethod) == null) {
      synchronized (BookingServiceGrpc.class) {
        if ((getMakeBookingMethod = BookingServiceGrpc.getMakeBookingMethod) == null) {
          BookingServiceGrpc.getMakeBookingMethod = getMakeBookingMethod =
              io.grpc.MethodDescriptor.<rs.raf.pds.v5.z2.gRPC.BookingRequest, rs.raf.pds.v5.z2.gRPC.BookingResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "MakeBooking"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  rs.raf.pds.v5.z2.gRPC.BookingRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  rs.raf.pds.v5.z2.gRPC.BookingResponse.getDefaultInstance()))
              .setSchemaDescriptor(new BookingServiceMethodDescriptorSupplier("MakeBooking"))
              .build();
        }
      }
    }
    return getMakeBookingMethod;
  }

  private static volatile io.grpc.MethodDescriptor<rs.raf.pds.v5.z2.gRPC.CancelRequest,
      rs.raf.pds.v5.z2.gRPC.CancelResponse> getCancelBookingMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "CancelBooking",
      requestType = rs.raf.pds.v5.z2.gRPC.CancelRequest.class,
      responseType = rs.raf.pds.v5.z2.gRPC.CancelResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<rs.raf.pds.v5.z2.gRPC.CancelRequest,
      rs.raf.pds.v5.z2.gRPC.CancelResponse> getCancelBookingMethod() {
    io.grpc.MethodDescriptor<rs.raf.pds.v5.z2.gRPC.CancelRequest, rs.raf.pds.v5.z2.gRPC.CancelResponse> getCancelBookingMethod;
    if ((getCancelBookingMethod = BookingServiceGrpc.getCancelBookingMethod) == null) {
      synchronized (BookingServiceGrpc.class) {
        if ((getCancelBookingMethod = BookingServiceGrpc.getCancelBookingMethod) == null) {
          BookingServiceGrpc.getCancelBookingMethod = getCancelBookingMethod =
              io.grpc.MethodDescriptor.<rs.raf.pds.v5.z2.gRPC.CancelRequest, rs.raf.pds.v5.z2.gRPC.CancelResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "CancelBooking"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  rs.raf.pds.v5.z2.gRPC.CancelRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  rs.raf.pds.v5.z2.gRPC.CancelResponse.getDefaultInstance()))
              .setSchemaDescriptor(new BookingServiceMethodDescriptorSupplier("CancelBooking"))
              .build();
        }
      }
    }
    return getCancelBookingMethod;
  }

  private static volatile io.grpc.MethodDescriptor<rs.raf.pds.v5.z2.gRPC.UserRequest,
      rs.raf.pds.v5.z2.gRPC.BookingInfo> getGetBookingsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetBookings",
      requestType = rs.raf.pds.v5.z2.gRPC.UserRequest.class,
      responseType = rs.raf.pds.v5.z2.gRPC.BookingInfo.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<rs.raf.pds.v5.z2.gRPC.UserRequest,
      rs.raf.pds.v5.z2.gRPC.BookingInfo> getGetBookingsMethod() {
    io.grpc.MethodDescriptor<rs.raf.pds.v5.z2.gRPC.UserRequest, rs.raf.pds.v5.z2.gRPC.BookingInfo> getGetBookingsMethod;
    if ((getGetBookingsMethod = BookingServiceGrpc.getGetBookingsMethod) == null) {
      synchronized (BookingServiceGrpc.class) {
        if ((getGetBookingsMethod = BookingServiceGrpc.getGetBookingsMethod) == null) {
          BookingServiceGrpc.getGetBookingsMethod = getGetBookingsMethod =
              io.grpc.MethodDescriptor.<rs.raf.pds.v5.z2.gRPC.UserRequest, rs.raf.pds.v5.z2.gRPC.BookingInfo>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetBookings"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  rs.raf.pds.v5.z2.gRPC.UserRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  rs.raf.pds.v5.z2.gRPC.BookingInfo.getDefaultInstance()))
              .setSchemaDescriptor(new BookingServiceMethodDescriptorSupplier("GetBookings"))
              .build();
        }
      }
    }
    return getGetBookingsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<rs.raf.pds.v5.z2.gRPC.PaymentRequest,
      rs.raf.pds.v5.z2.gRPC.PaymentResponse> getPayMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Pay",
      requestType = rs.raf.pds.v5.z2.gRPC.PaymentRequest.class,
      responseType = rs.raf.pds.v5.z2.gRPC.PaymentResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<rs.raf.pds.v5.z2.gRPC.PaymentRequest,
      rs.raf.pds.v5.z2.gRPC.PaymentResponse> getPayMethod() {
    io.grpc.MethodDescriptor<rs.raf.pds.v5.z2.gRPC.PaymentRequest, rs.raf.pds.v5.z2.gRPC.PaymentResponse> getPayMethod;
    if ((getPayMethod = BookingServiceGrpc.getPayMethod) == null) {
      synchronized (BookingServiceGrpc.class) {
        if ((getPayMethod = BookingServiceGrpc.getPayMethod) == null) {
          BookingServiceGrpc.getPayMethod = getPayMethod =
              io.grpc.MethodDescriptor.<rs.raf.pds.v5.z2.gRPC.PaymentRequest, rs.raf.pds.v5.z2.gRPC.PaymentResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Pay"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  rs.raf.pds.v5.z2.gRPC.PaymentRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  rs.raf.pds.v5.z2.gRPC.PaymentResponse.getDefaultInstance()))
              .setSchemaDescriptor(new BookingServiceMethodDescriptorSupplier("Pay"))
              .build();
        }
      }
    }
    return getPayMethod;
  }

  private static volatile io.grpc.MethodDescriptor<rs.raf.pds.v5.z2.gRPC.AskRequest,
      rs.raf.pds.v5.z2.gRPC.AskResponse> getAskHotelsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "AskHotels",
      requestType = rs.raf.pds.v5.z2.gRPC.AskRequest.class,
      responseType = rs.raf.pds.v5.z2.gRPC.AskResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<rs.raf.pds.v5.z2.gRPC.AskRequest,
      rs.raf.pds.v5.z2.gRPC.AskResponse> getAskHotelsMethod() {
    io.grpc.MethodDescriptor<rs.raf.pds.v5.z2.gRPC.AskRequest, rs.raf.pds.v5.z2.gRPC.AskResponse> getAskHotelsMethod;
    if ((getAskHotelsMethod = BookingServiceGrpc.getAskHotelsMethod) == null) {
      synchronized (BookingServiceGrpc.class) {
        if ((getAskHotelsMethod = BookingServiceGrpc.getAskHotelsMethod) == null) {
          BookingServiceGrpc.getAskHotelsMethod = getAskHotelsMethod =
              io.grpc.MethodDescriptor.<rs.raf.pds.v5.z2.gRPC.AskRequest, rs.raf.pds.v5.z2.gRPC.AskResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "AskHotels"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  rs.raf.pds.v5.z2.gRPC.AskRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  rs.raf.pds.v5.z2.gRPC.AskResponse.getDefaultInstance()))
              .setSchemaDescriptor(new BookingServiceMethodDescriptorSupplier("AskHotels"))
              .build();
        }
      }
    }
    return getAskHotelsMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static BookingServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<BookingServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<BookingServiceStub>() {
        @java.lang.Override
        public BookingServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new BookingServiceStub(channel, callOptions);
        }
      };
    return BookingServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static BookingServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<BookingServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<BookingServiceBlockingStub>() {
        @java.lang.Override
        public BookingServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new BookingServiceBlockingStub(channel, callOptions);
        }
      };
    return BookingServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static BookingServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<BookingServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<BookingServiceFutureStub>() {
        @java.lang.Override
        public BookingServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new BookingServiceFutureStub(channel, callOptions);
        }
      };
    return BookingServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     */
    default void makeBooking(rs.raf.pds.v5.z2.gRPC.BookingRequest request,
        io.grpc.stub.StreamObserver<rs.raf.pds.v5.z2.gRPC.BookingResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getMakeBookingMethod(), responseObserver);
    }

    /**
     */
    default void cancelBooking(rs.raf.pds.v5.z2.gRPC.CancelRequest request,
        io.grpc.stub.StreamObserver<rs.raf.pds.v5.z2.gRPC.CancelResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getCancelBookingMethod(), responseObserver);
    }

    /**
     */
    default void getBookings(rs.raf.pds.v5.z2.gRPC.UserRequest request,
        io.grpc.stub.StreamObserver<rs.raf.pds.v5.z2.gRPC.BookingInfo> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetBookingsMethod(), responseObserver);
    }

    /**
     * <pre>
     * Client payment to confirm booking before deadline
     * </pre>
     */
    default void pay(rs.raf.pds.v5.z2.gRPC.PaymentRequest request,
        io.grpc.stub.StreamObserver<rs.raf.pds.v5.z2.gRPC.PaymentResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getPayMethod(), responseObserver);
    }

    /**
     * <pre>
     * Proxy query: Client asks BookingService, which forwards to HotelService
     * </pre>
     */
    default void askHotels(rs.raf.pds.v5.z2.gRPC.AskRequest request,
        io.grpc.stub.StreamObserver<rs.raf.pds.v5.z2.gRPC.AskResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getAskHotelsMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service BookingService.
   */
  public static abstract class BookingServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return BookingServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service BookingService.
   */
  public static final class BookingServiceStub
      extends io.grpc.stub.AbstractAsyncStub<BookingServiceStub> {
    private BookingServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected BookingServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new BookingServiceStub(channel, callOptions);
    }

    /**
     */
    public void makeBooking(rs.raf.pds.v5.z2.gRPC.BookingRequest request,
        io.grpc.stub.StreamObserver<rs.raf.pds.v5.z2.gRPC.BookingResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getMakeBookingMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void cancelBooking(rs.raf.pds.v5.z2.gRPC.CancelRequest request,
        io.grpc.stub.StreamObserver<rs.raf.pds.v5.z2.gRPC.CancelResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getCancelBookingMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getBookings(rs.raf.pds.v5.z2.gRPC.UserRequest request,
        io.grpc.stub.StreamObserver<rs.raf.pds.v5.z2.gRPC.BookingInfo> responseObserver) {
      io.grpc.stub.ClientCalls.asyncServerStreamingCall(
          getChannel().newCall(getGetBookingsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Client payment to confirm booking before deadline
     * </pre>
     */
    public void pay(rs.raf.pds.v5.z2.gRPC.PaymentRequest request,
        io.grpc.stub.StreamObserver<rs.raf.pds.v5.z2.gRPC.PaymentResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getPayMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Proxy query: Client asks BookingService, which forwards to HotelService
     * </pre>
     */
    public void askHotels(rs.raf.pds.v5.z2.gRPC.AskRequest request,
        io.grpc.stub.StreamObserver<rs.raf.pds.v5.z2.gRPC.AskResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getAskHotelsMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service BookingService.
   */
  public static final class BookingServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<BookingServiceBlockingStub> {
    private BookingServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected BookingServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new BookingServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public rs.raf.pds.v5.z2.gRPC.BookingResponse makeBooking(rs.raf.pds.v5.z2.gRPC.BookingRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getMakeBookingMethod(), getCallOptions(), request);
    }

    /**
     */
    public rs.raf.pds.v5.z2.gRPC.CancelResponse cancelBooking(rs.raf.pds.v5.z2.gRPC.CancelRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getCancelBookingMethod(), getCallOptions(), request);
    }

    /**
     */
    public java.util.Iterator<rs.raf.pds.v5.z2.gRPC.BookingInfo> getBookings(
        rs.raf.pds.v5.z2.gRPC.UserRequest request) {
      return io.grpc.stub.ClientCalls.blockingServerStreamingCall(
          getChannel(), getGetBookingsMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Client payment to confirm booking before deadline
     * </pre>
     */
    public rs.raf.pds.v5.z2.gRPC.PaymentResponse pay(rs.raf.pds.v5.z2.gRPC.PaymentRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getPayMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Proxy query: Client asks BookingService, which forwards to HotelService
     * </pre>
     */
    public rs.raf.pds.v5.z2.gRPC.AskResponse askHotels(rs.raf.pds.v5.z2.gRPC.AskRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getAskHotelsMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service BookingService.
   */
  public static final class BookingServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<BookingServiceFutureStub> {
    private BookingServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected BookingServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new BookingServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<rs.raf.pds.v5.z2.gRPC.BookingResponse> makeBooking(
        rs.raf.pds.v5.z2.gRPC.BookingRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getMakeBookingMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<rs.raf.pds.v5.z2.gRPC.CancelResponse> cancelBooking(
        rs.raf.pds.v5.z2.gRPC.CancelRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getCancelBookingMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Client payment to confirm booking before deadline
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<rs.raf.pds.v5.z2.gRPC.PaymentResponse> pay(
        rs.raf.pds.v5.z2.gRPC.PaymentRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getPayMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Proxy query: Client asks BookingService, which forwards to HotelService
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<rs.raf.pds.v5.z2.gRPC.AskResponse> askHotels(
        rs.raf.pds.v5.z2.gRPC.AskRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getAskHotelsMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_MAKE_BOOKING = 0;
  private static final int METHODID_CANCEL_BOOKING = 1;
  private static final int METHODID_GET_BOOKINGS = 2;
  private static final int METHODID_PAY = 3;
  private static final int METHODID_ASK_HOTELS = 4;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_MAKE_BOOKING:
          serviceImpl.makeBooking((rs.raf.pds.v5.z2.gRPC.BookingRequest) request,
              (io.grpc.stub.StreamObserver<rs.raf.pds.v5.z2.gRPC.BookingResponse>) responseObserver);
          break;
        case METHODID_CANCEL_BOOKING:
          serviceImpl.cancelBooking((rs.raf.pds.v5.z2.gRPC.CancelRequest) request,
              (io.grpc.stub.StreamObserver<rs.raf.pds.v5.z2.gRPC.CancelResponse>) responseObserver);
          break;
        case METHODID_GET_BOOKINGS:
          serviceImpl.getBookings((rs.raf.pds.v5.z2.gRPC.UserRequest) request,
              (io.grpc.stub.StreamObserver<rs.raf.pds.v5.z2.gRPC.BookingInfo>) responseObserver);
          break;
        case METHODID_PAY:
          serviceImpl.pay((rs.raf.pds.v5.z2.gRPC.PaymentRequest) request,
              (io.grpc.stub.StreamObserver<rs.raf.pds.v5.z2.gRPC.PaymentResponse>) responseObserver);
          break;
        case METHODID_ASK_HOTELS:
          serviceImpl.askHotels((rs.raf.pds.v5.z2.gRPC.AskRequest) request,
              (io.grpc.stub.StreamObserver<rs.raf.pds.v5.z2.gRPC.AskResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getMakeBookingMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              rs.raf.pds.v5.z2.gRPC.BookingRequest,
              rs.raf.pds.v5.z2.gRPC.BookingResponse>(
                service, METHODID_MAKE_BOOKING)))
        .addMethod(
          getCancelBookingMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              rs.raf.pds.v5.z2.gRPC.CancelRequest,
              rs.raf.pds.v5.z2.gRPC.CancelResponse>(
                service, METHODID_CANCEL_BOOKING)))
        .addMethod(
          getGetBookingsMethod(),
          io.grpc.stub.ServerCalls.asyncServerStreamingCall(
            new MethodHandlers<
              rs.raf.pds.v5.z2.gRPC.UserRequest,
              rs.raf.pds.v5.z2.gRPC.BookingInfo>(
                service, METHODID_GET_BOOKINGS)))
        .addMethod(
          getPayMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              rs.raf.pds.v5.z2.gRPC.PaymentRequest,
              rs.raf.pds.v5.z2.gRPC.PaymentResponse>(
                service, METHODID_PAY)))
        .addMethod(
          getAskHotelsMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              rs.raf.pds.v5.z2.gRPC.AskRequest,
              rs.raf.pds.v5.z2.gRPC.AskResponse>(
                service, METHODID_ASK_HOTELS)))
        .build();
  }

  private static abstract class BookingServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    BookingServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return rs.raf.pds.v5.z2.gRPC.BookingServiceOuterClass.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("BookingService");
    }
  }

  private static final class BookingServiceFileDescriptorSupplier
      extends BookingServiceBaseDescriptorSupplier {
    BookingServiceFileDescriptorSupplier() {}
  }

  private static final class BookingServiceMethodDescriptorSupplier
      extends BookingServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    BookingServiceMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (BookingServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new BookingServiceFileDescriptorSupplier())
              .addMethod(getMakeBookingMethod())
              .addMethod(getCancelBookingMethod())
              .addMethod(getGetBookingsMethod())
              .addMethod(getPayMethod())
              .addMethod(getAskHotelsMethod())
              .build();
        }
      }
    }
    return result;
  }
}
