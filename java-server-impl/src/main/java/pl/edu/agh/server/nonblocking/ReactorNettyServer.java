package pl.edu.agh.server.nonblocking;

import io.netty.handler.logging.LogLevel;
import reactor.core.publisher.Flux;
import reactor.netty.DisposableServer;
import reactor.netty.resources.LoopResources;
import reactor.netty.tcp.TcpServer;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

public class ReactorNettyServer {
    public static void main(String[] args) {
        LoopResources loop = LoopResources.create("event-loop", 1, 4, true);

        TcpServer server = TcpServer
                .create()
                .runOn(loop)
                .host("localhost")
                .port(8080)
                .wiretap("simple-reactor-netty-server", LogLevel.DEBUG, AdvancedByteBufFormat.TEXTUAL)
                .handle((inbound, outbound) -> {
                    Flux<String> str = inbound
                            .receive()
                            .asString()
                            .map(String::toUpperCase);

                    return outbound.sendString(str);
                });

        DisposableServer disposableServer = server.bindNow();
        disposableServer.onDispose().block();
    }
}
