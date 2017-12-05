package com.github.maxbelov.microloop;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {

    private static final String HOSTNAME = "localhost";
    private static final int PORT = 8000;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Starting Loop!");
        ExecutorService executor = Executors.newCachedThreadPool();

        LoopServer loopServer = new LoopServer(HOSTNAME, PORT, () -> new FixedSizeMessageHandler(10));

        executor.submit(() -> {
            try {
                loopServer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        TimeUnit.SECONDS.sleep(2);

        Runnable runnable = () -> {
            try {
                new SocketClientExample().startClient(HOSTNAME, PORT);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        };

        executor.submit(runnable);
        executor.submit(runnable);
        executor.shutdown();

    }
}
