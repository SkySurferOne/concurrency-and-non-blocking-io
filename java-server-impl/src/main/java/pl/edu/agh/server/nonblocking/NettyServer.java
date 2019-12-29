package pl.edu.agh.server.nonblocking;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class NettyServer {
    public static void main(String[] args) {
        System.out.println("Netty server running...");
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(group)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ServerInitializer());

            bootstrap.bind(8080).sync().channel().closeFuture().sync();
        } catch (InterruptedException e) {
            System.out.println("Couldn't start a server " + e);
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
            pipeline.addLast("handler", new ServerHandler());
        }

    }

    private static class ServerHandler extends SimpleChannelInboundHandler<String> {
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
            System.out.println("Channel connected " + ctx.channel());
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
            Channel incoming = ctx.channel();
            String processed = msg.toUpperCase();
            incoming.writeAndFlush(processed);
        }
    }
}
