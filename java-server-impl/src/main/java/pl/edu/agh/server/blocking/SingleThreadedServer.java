package pl.edu.agh.server.blocking;

import pl.edu.agh.server.util.Util;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SingleThreadedServer {
    public static void main(String[] args) throws IOException {
        System.out.println("Started single threaded server...");

        ServerSocket ss = new ServerSocket(8080);

        // Bottleneck is obvious, we have only one thread so we have only one connection
        while (true) {
            Socket s = ss.accept(); // blocking call - never returns null
            Util.process(s);
        }
    }
}
