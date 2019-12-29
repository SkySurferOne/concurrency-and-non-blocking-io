package pl.edu.agh.server.blocking;

import pl.edu.agh.server.util.Util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NIOBlockingServer {
    public static void main(String[] args) throws IOException {
        System.out.println("Thread pool server...");

        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.bind(new InetSocketAddress("localhost", 8080));

        // out of memory on the queue or waiting connections waiting for handling their operations
        ExecutorService pool = Executors.newFixedThreadPool(400);
        while (true) {
            SocketChannel s = ssc.accept(); // blocking call - never returns null
            pool.submit(() -> Util.process(s));
        }
    }
}
