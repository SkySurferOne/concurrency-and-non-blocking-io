package pl.edu.agh.server.nonblocking;

import pl.edu.agh.server.util.Util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

public class NonblockingSingleThreadedPollingServer {
    private static Collection<SocketChannel> sockets = Collections.newSetFromMap(
            new HashMap<SocketChannel, Boolean>()
    );

    public static void main(String[] args) throws IOException {
        System.out.println("Nonblocking single threaded polling server...");

        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.bind(new InetSocketAddress("localhost", 8080));
        ssc.configureBlocking(false);

        while (true) {
            SocketChannel s = ssc.accept(); // nonblocking call - usually null

            if (s != null) {
                System.out.println("Connection from " + s);
                s.configureBlocking(false);
                sockets.add(s);
            }

            // Checking sockets is really CPU consuming
            // Could throw too many open files in system when open too many connections
            for (Iterator<SocketChannel> it = sockets.iterator(); it.hasNext(); ) {
                SocketChannel socket = it.next();
                try {
                    ByteBuffer buf = ByteBuffer.allocateDirect(1024);
                    int read = socket.read(buf);

                    if (read == -1) {
                        it.remove();
                    } else if (read != 0) {
                        buf.flip(); // buf.limit(buf.position()).position(0)
                        for (int i = 0; i < buf.limit(); i++) {
                            buf.put(i, (byte) Util.transmogrify(buf.get(i)));
                        }
                        socket.write(buf);
                        // buf.clear();
                    }
                } catch (IOException e) {
                    System.err.println("Connection problem = " + e);
                    it.remove();
                }
            }
        }
    }
}
