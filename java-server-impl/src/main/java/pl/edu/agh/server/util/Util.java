package pl.edu.agh.server.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Util {
    public static int transmogrify(int data) {
        if (Character.isLetter(data)) return data ^ ' ';
        return data;
    }

    public static void process(Socket s) {
        System.out.println("Connection from " + s);

        try (
                InputStream in = s.getInputStream();
                OutputStream out = s.getOutputStream();
        ) {
            int data;
            while ((data = in.read()) != -1) {
                data = transmogrify(data);
                out.write(data);
            }
        } catch (IOException e) {
            System.err.println("Connection problem = " + e);
        }
    }

    public static void process(SocketChannel sc) {
        System.out.println("Connection from " + sc);

        try {
            ByteBuffer buf = ByteBuffer.allocateDirect(1024);
            while (sc.read(buf) != -1) {
                buf.flip(); // buf.limit(buf.position()).position(0)
                for (int i = 0; i < buf.limit(); i++) {
                    buf.put(i, (byte) transmogrify(buf.get(i)));
                }
                sc.write(buf);
                buf.clear();
            }
        } catch (IOException e) {
            System.err.println("Connection problem = " + e);
        }
    }
}
