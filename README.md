第一个DEMO：使用java实现grpc客户端、服务端互相通信
====
# 实现功能
实现客户端发送 "服务端的朋友你好!"  服务端打印出客户端的发送信息，并返回给客户端消息 "客户端的朋友收到你的问候了！我很好"
# 项目开始
## 1.创建spring boot项目pom.xml引入
```
    </dependencies>
        <!--GRPC-->
        <dependency>
            <groupId>io.grpc</groupId>
            <artifactId>grpc-netty-shaded</artifactId>
            <version>1.18.0</version>
        </dependency>
        <dependency>
            <groupId>io.grpc</groupId>
            <artifactId>grpc-protobuf</artifactId>
            <version>1.18.0</version>
        </dependency>
        <dependency>
            <groupId>io.grpc</groupId>
            <artifactId>grpc-stub</artifactId>
            <version>1.18.0</version>
        </dependency>
        <!--GRPC-->
    </dependencies>
    
    <!--GRPC自动生成代码插件-->
    <build>
        <extensions>
            <extension>
                <groupId>kr.motd.maven</groupId>
                <artifactId>os-maven-plugin</artifactId>
                <version>1.5.0.Final</version>
            </extension>
        </extensions>
        <plugins>
            <plugin>
                <groupId>org.xolstice.maven.plugins</groupId>
                <artifactId>protobuf-maven-plugin</artifactId>
                <version>0.5.1</version>
                <configuration>
                    <protocArtifact>com.google.protobuf:protoc:3.5.1-1:exe:${os.detected.classifier}</protocArtifact>
                    <pluginId>grpc-java</pluginId>
                    <pluginArtifact>io.grpc:protoc-gen-grpc-java:1.18.0:exe:${os.detected.classifier}</pluginArtifact>
                </configuration>
                <executions>
                    <execution>
                        <goals>
                            <goal>compile</goal>
                            <goal>compile-custom</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
```

## 2.定义.proto文件
在src/main目录下创建文件夹proto(基于protobuf的代码生成插件默认去src/main/proto下查找.proto文件)并定义一个 .proto 文件。以下的 hello.proto文件，给出了GRPC的1种服务定义:
```
//使用proto3
syntax = "proto3";

//包名，防止不同项目之间的命名冲突
package hello;

//用于生成的java包，如果没有明确指定，将会使用 package的值也就是在本文中的 hello
option java_package = "cn.yase.grpc.proto";
//生成的java类名，如果不指定将会以 .proto文件名进行处理(如：foo_bar.proto 将会生成 FooBar.java)
option java_outer_classname = "MonitorProto";

option java_multiple_files = true;

//服务定义
service HelloService {
    //一元RPC：客户端发送一个请求到服务端，服务端返回一个响应给客户端
    rpc SayHello (HelloRequest) returns (HelloResponse) {}
}

message HelloRequest {
    string greeting = 1;
}

message HelloResponse {
    string reply = 1;
}
```

## 3.GRPC生成代码 
在idea左下角点击 Terminal 选项，出现控制台，输入命令:
```
mvn clean install
```
可以看见生成了 target文件其中 GRPC生成的代码在如下位置:
<br>![image](https://github.com/caimengkuang/grpc-java-demo1/blob/master/20190227194036.png)<br>
从上图与.proto文件相比较，可以知道:cn.yase.grpc.proto 就是 hello.proto 中 java_package的属性值（取这个值的原因是为了后面将生成的代码放到src/main/java/cn/yase/proto 目录下)

在src/main/java将grpc生成的代码移动到 src/main/cn/yase/proto目录下

## 4.编写服务端代码
### 4.1 服务端实现类
```
public class HelloServiceImpl extends HelloServiceGrpc.HelloServiceImplBase {

    @Override
    public void sayHello(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {
        System.out.println("收到客户端消息:"+request.getGreeting());

        responseObserver.onNext(HelloResponse.newBuilder().setReply("客户端的朋友收到你的问候了！我很好！").build());

        //结束
        responseObserver.onCompleted();
    }
}
```
### 4.2 GRPC服务启动类
```
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
```

### 5.编写客户端代码
```
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

```

### 6.先启动服务端代码，再启动客户端。至此OK了！
