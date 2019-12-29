package pl.edu.agh.server.util;

import java.io.IOException;
import java.net.Socket;

public class TrafficGenerator {
    public static void main(String[] args) throws InterruptedException {
        int connectionNumber = 6000;
        for (int i = 0; i < connectionNumber; i++) {
            try {
                new Socket("localhost", 8080);
                System.out.println(i);
            } catch (IOException e) {
                System.out.println("Could not connect - " + e);
            }
        }

        Thread.sleep(1000000000); // to sustain connections
    }
}
