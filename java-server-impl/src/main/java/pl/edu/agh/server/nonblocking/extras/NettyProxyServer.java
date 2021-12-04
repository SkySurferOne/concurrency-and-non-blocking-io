package pl.edu.agh.server.nonblocking.extras;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.net.InetSocketAddress;

public class NettyProxyServer {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(NettyProxyServer.class);

    public static void main(String[] args) {
        logger.info("Netty server running...");
        EventLoopGroup group = new NioEventLoopGroup(4);

        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(group)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ServerInitializer());

            bootstrap.bind(9000).sync().channel().closeFuture().sync();
        } catch (InterruptedException e) {
            logger.error("Couldn't start a server " + e);
        } finally {
            group.shutdownGracefully();
        }
    }

    private static class ServerInitializer extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();

            pipeline.addLast("decoder", new StringDecoder());
            pipeline.addLast("encoder", new StringEncoder());
            pipeline.addLast("logging", new LoggingHandler(LogLevel.DEBUG));
            pipeline.addLast("handler", new ServerHandler());
        }

    }

    private static class ServerHandler extends SimpleChannelInboundHandler<String> {
        private final InternalLogger logger = InternalLoggerFactory.getInstance(getClass());

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
            logger.debug("Channel connected " + ctx.channel());
        }

        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
            super.channelUnregistered(ctx);
            logger.debug("Channel unregistered " + ctx.channel());
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
            Channel incoming = ctx.channel();

            Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class)
                    .handler(new ClientInitializer(incoming, msg))
                    //.group(ctx.channel().eventLoop()); // here we are using the same event loop hence the same thread
                    .group(ctx.channel().eventLoop().parent()); // here we are using event loop group
            ChannelFuture connectFuture = bootstrap.connect(new InetSocketAddress("localhost", 8080));

            connectFuture.addListener((ChannelFutureListener) channelFuture -> {
                if (channelFuture.isSuccess()) {
                    logger.debug("Connection completed");
                } else {
                    logger.debug("Connection attempt failed");
                    channelFuture.cause().printStackTrace();
                }
            });
        }
    }

    // =============================================================================
    // client handlers
    // =============================================================================

    private static class ClientHandler extends SimpleChannelInboundHandler<String> {
        private final InternalLogger logger = InternalLoggerFactory.getInstance(getClass());

        private final Channel originalChannel;
        private final String message;

        private ClientHandler(Channel originalChannel, String message) {
            this.originalChannel = originalChannel;
            this.message = message;
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            ctx.writeAndFlush(message);
        }

        @Override
        public void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
            logger.debug("Received data: " + msg);
            originalChannel.writeAndFlush("Response from other server: " + msg);
            ctx.close();
        }
    }

    private static class ClientInitializer extends ChannelInitializer<SocketChannel> {
        private final Channel originalChannel;
        private final String message;

        private ClientInitializer(Channel originalChannel, String message) {
            this.originalChannel = originalChannel;
            this.message = message;
        }

        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();

            pipeline.addLast("client-encoder", new StringEncoder());
            pipeline.addLast("client-decoder", new StringDecoder());
            pipeline.addLast("client-logging", new LoggingHandler(LogLevel.DEBUG));
            pipeline.addLast("client-handler", new ClientHandler(originalChannel, message));
        }

    }
}
