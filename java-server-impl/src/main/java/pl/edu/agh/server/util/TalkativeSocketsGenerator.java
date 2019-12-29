package pl.edu.agh.server.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;

public class TalkativeSocketsGenerator {
    public static void main(String[] args) {
        System.out.println("TalkativeSocketsGenerator running...");
        int connectionNumber = 6000;

        for (int i = 0; i < connectionNumber; i++) {
            try {
                Socket s = new Socket("localhost", 8080);
                int socketNumber = i;
                new Thread(() -> {
                    talkToServer(socketNumber, s);
                }).start();
            } catch (IOException e) {
                System.out.println("Couldn't connect to server " + e);
            }
        }
    }

    private static void talkToServer(int i, Socket s) {
        try {
            PrintWriter out =
                    new PrintWriter(s.getOutputStream(), true);
            BufferedReader in =
                    new BufferedReader(
                            new InputStreamReader(s.getInputStream()));

            while (true) {
                String message = "Ping from client socket " + i;
                out.println(message);
                System.out.println(in.readLine());

                Thread.sleep(getRandomNumberInRange(0, 1000));
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Connection problem = " + e);
        }
    }

    private static int getRandomNumberInRange(int min, int max) {
        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }

        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }
}
