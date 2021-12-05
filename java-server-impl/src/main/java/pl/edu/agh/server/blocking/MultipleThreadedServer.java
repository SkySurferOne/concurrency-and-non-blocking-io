package pl.edu.agh.server.blocking;

import pl.edu.agh.server.util.Util;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MultipleThreadedServer {
    public static void main(String[] args) throws IOException {
        System.out.println("Multiple threaded server...");

        ServerSocket ss = new ServerSocket(8080);

        while (true) {
            Socket s = ss.accept(); // blocking call - never returns null

            // We will get out of memory exception, also there will be a lot of context switching
            // We can run out of file descriptors also
            new Thread(() -> Util.process(s)).start();
        }
    }
}
