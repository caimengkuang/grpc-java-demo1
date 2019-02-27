package cn.yase.grpc;

import cn.yase.grpc.proto.HelloRequest;
import cn.yase.grpc.proto.HelloResponse;
import cn.yase.grpc.proto.HelloServiceGrpc;
import io.grpc.stub.StreamObserver;

/**
 * @author yase
 * @create 2019-02-22
 */
public class HelloServiceImpl extends HelloServiceGrpc.HelloServiceImplBase {

    @Override
    public void sayHello(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {
        System.out.println("收到客户端消息:"+request.getGreeting());

        responseObserver.onNext(HelloResponse.newBuilder().setReply("客户端的朋友收到你的问候了！我很好！").build());

        //结束
        responseObserver.onCompleted();
    }
}
