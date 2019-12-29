package pl.edu.agh.server.util;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class SimpleNettyClient {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Simple Netty client running...");
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ClientInitializer());

            Channel channel = bootstrap.connect("localhost", 8080).sync().channel();
            channel.writeAndFlush("Ping\r\n");

            Thread.sleep(1000000);
        } catch (InterruptedException e) {
            System.out.println("Couldn't connect " + e);
        } finally {
            group.shutdownGracefully();
        }
    }

    private static class ClientInitializer extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();

            pipeline.addLast("decoder", new StringDecoder());
            pipeline.addLast("encoder", new StringEncoder());
            pipeline.addLast("handler", new ClientHandler());
        }
    }

    private static class ClientHandler extends SimpleChannelInboundHandler<String> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
            System.out.println(msg);
        }
    }
}
