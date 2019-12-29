package pl.edu.agh.server.blocking;

import pl.edu.agh.server.util.Util;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

public class ThreadPoolServer {
    public static void main(String[] args) throws IOException {
        System.out.println("Thread pool server...");

        ServerSocket ss = new ServerSocket(8080);

//        ExecutorService pool = new ThreadPoolExecutor(0, 400,
//                60L, TimeUnit.SECONDS,
//                new SynchronousQueue<Runnable>(),
//                new ThreadPoolExecutor.CallerRunsPolicy());

        // out of memory on the queue or waiting connections waiting for handling their operations
        ExecutorService pool = Executors.newFixedThreadPool(400);
        while (true) {
            Socket s = ss.accept(); // blocking call - never returns null
            pool.submit(() -> Util.process(s));
        }
    }
}
