package cn.yase.grpc;

import cn.yase.grpc.proto.HelloRequest;
import cn.yase.grpc.proto.HelloResponse;
import cn.yase.grpc.proto.HelloServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

/**
 * @author yase
 * @create 2019-02-24
 */
public class GrpcClient {

    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8899)
                .usePlaintext()
                .build();

        //获取client stub
        HelloServiceGrpc.HelloServiceBlockingStub blockingStub = HelloServiceGrpc.newBlockingStub(channel);

        //stub调用服务端上 sayHello 方法 （一元RPC）
        HelloResponse helloResponse = blockingStub.sayHello(HelloRequest.newBuilder().setGreeting("服务端的朋友你好！").build());

        System.out.println(helloResponse.getReply());

    }
}
