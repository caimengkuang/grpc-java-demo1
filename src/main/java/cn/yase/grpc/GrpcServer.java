package cn.yase.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

/**
 * @author yase
 * @create 2019-02-22
 */
public class GrpcServer {


    /** GRPC 服务端 */
    private Server server;

    public static void main(String[] args) throws IOException, InterruptedException {
        GrpcServer grpcService = new GrpcServer();

        grpcService.start();
        System.out.println("GRPC 服务端启动成功");

        //GRPC 服务端需要手动阻塞线程
        grpcService.waitTermination();


    }

    private void start() throws IOException {
        //绑定接口、启动服务
        this.server = ServerBuilder.forPort(8899)
                .addService(new HelloServiceImpl())
                .build()
                .start();

        System.out.println("server start!");

        //这里是为了防止jvm关闭了，但是tcp还没有关闭的情况
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            System.out.println("关闭jvm");
            GrpcServer.this.stop();
        }));
    }

    private void stop() {
        if (this.server != null) {
            this.server.shutdown();
        }
    }

    private void waitTermination() throws InterruptedException {

        if (this.server != null) {
            server.awaitTermination();
        }
    }
}
