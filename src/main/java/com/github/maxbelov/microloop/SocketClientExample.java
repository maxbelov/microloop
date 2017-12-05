package com.github.maxbelov.microloop;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class SocketClientExample {

    public void startClient(String hostname, int port)
            throws IOException, InterruptedException {

        InetSocketAddress hostAddress = new InetSocketAddress(hostname, port);
        SocketChannel client = SocketChannel.open(hostAddress);

        System.out.println("Client... started");

        String threadName = Thread.currentThread().getName();

        // Send messages to server
        String [] messages = new String []
                {threadName + ": test1",threadName + ": test2",threadName + ": test3"};

        for (int i = 0; i < messages.length; i++) {
            byte [] message = messages[i].getBytes();
            ByteBuffer buffer = ByteBuffer.wrap(message);
            client.write(buffer);
            System.out.println("Sent to server: " + messages[i]);
            readResponse(client);
            Thread.sleep(5000);
        }
        client.close();
    }

    private void readResponse(SocketChannel channel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(10);
        while (buffer.position() < buffer.capacity()) {
            int bytesRead = channel.read(buffer);
            if (bytesRead == -1) {
                throw new RuntimeException("Unexpected connection interrupt!");
            }
        }
        System.out.printf("Client received: %s\n", new String(buffer.array()));

    }
}
