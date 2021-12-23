package pl.edu.agh.server.nonblocking;

import pl.edu.agh.server.util.Util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

public class NonblockingSelectorServer {
    private static Map<SocketChannel, Queue<ByteBuffer>> pendingData = new HashMap<>();

    public static void main(String[] args) throws IOException {
        System.out.println("Nonblocking selector server...");

        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.bind(new InetSocketAddress("localhost", 8080));
        ssc.configureBlocking(false);

        Selector selector = Selector.open();
        ssc.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            selector.select(); // blocking call - blocks until sth happen

            for (Iterator<SelectionKey> itKeys = selector.selectedKeys().iterator(); itKeys.hasNext(); ) {
                SelectionKey key = itKeys.next();
                itKeys.remove();

                if (key.isValid()) {
                    if (key.isAcceptable()) {
                        // someone connected to our ServerSocketChannel
                        accept(key);
                    }
                    if (key.isReadable()) {
                        read(key);
                    }
                    if (key.isWritable()) {
                        write(key);
                    }
                }
            }
        }
    }

    private static void write(SelectionKey key) throws IOException {
        SocketChannel sc = (SocketChannel) key.channel();
        Queue<ByteBuffer> queue = pendingData.get(sc);

        ByteBuffer buf = queue.peek();
        while ((buf = queue.peek()) != null) {
            sc.write(buf);

            if (!buf.hasRemaining()) {
                queue.poll();
            } else {
                return;
            }
        }
        sc.register(key.selector(), SelectionKey.OP_READ);
    }

    // read from a socket channel
    private static void read(SelectionKey key) throws IOException {
        SocketChannel sc = (SocketChannel) key.channel();

        ByteBuffer buf = ByteBuffer.allocateDirect(1024);
        int read = sc.read(buf); // non blocking

        if (read == -1) {
            pendingData.remove(sc);
            return;
        }

        buf.flip(); // buf.limit(buf.position()).position(0)
        for (int i = 0; i < buf.limit(); i++) {
            buf.put(i, (byte) Util.transmogrify(buf.get(i)));
        }

        pendingData.get(sc).add(buf);
        // check when socket channel will be ready for writing
        sc.register(key.selector(), SelectionKey.OP_WRITE);
    }

    // accept new socket
    private static void accept(SelectionKey key) throws IOException {
        ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
        SocketChannel sc = ssc.accept(); // nonblocking, never null
        sc.configureBlocking(false);
        // register read event for this socket
        sc.register(key.selector(), SelectionKey.OP_READ);
        pendingData.put(sc, new ConcurrentLinkedDeque<>());
    }
}
