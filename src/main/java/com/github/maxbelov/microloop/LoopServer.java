package com.github.maxbelov.microloop;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class LoopServer {
    private final InetSocketAddress bindAddress;
    private Selector selector;
    private Map<SocketChannel, Message> messageData = new HashMap<>();
    private int counter;

    public LoopServer(String host, int port) {
        this.bindAddress = new InetSocketAddress(host, port);
    }

    public void start() throws IOException {
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.socket().bind(bindAddress);
        selector = Selector.open();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("server started");

        while (true) {
            selector.select();
            Iterator<SelectionKey> selectedKeysIterator = selector.selectedKeys().iterator();
            while (selectedKeysIterator.hasNext()) {
                SelectionKey key = selectedKeysIterator.next();
                selectedKeysIterator.remove();
                if (key.isValid()) {
                    if (key.isAcceptable()) {
                        accept(key);
                    } else if (key.isReadable()) {
                        read(key);
                    }
                }
            }
        }
    }

    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel channel = serverChannel.accept();
        channel.configureBlocking(false);
        SocketAddress socket = channel.getRemoteAddress();
        System.out.println("Connected to " + socket);

        channel.register(selector, SelectionKey.OP_READ);
    }


    private void read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        int bytesRead = channel.read(buffer);

        if (bytesRead == -1) {
            Socket socket = channel.socket();
            SocketAddress remoteAddr = socket.getRemoteSocketAddress();
            System.out.println("Connection closed by client: " + remoteAddr);
            channel.close();
            key.cancel();
            dispatchMessage(messageData.remove(channel)); //todo: can be not full
            return;
        }

        channel.read(buffer);
        byte[] data = Arrays.copyOf(buffer.array(), bytesRead); // why to copy?
        dispatchData(channel, data);
        System.out.println("Get data " + new String(data));
    }

    private void dispatchData(SocketChannel channel, byte[] data) {
        if (!messageData.containsKey(channel)) {
            messageData.put(channel, new Message());
        }
        Message message = messageData.get(channel);
        byte[] remainingData = data;
        do {
            remainingData = message.addData(remainingData);
            if (message.isFull()) {
                messageData.remove(channel);
                dispatchMessage(message); // todo: call is synchronous
                message = new Message();
                messageData.put(channel, message);
            }
        } while (remainingData.length > 0);
    }

    private void dispatchMessage(Message message) {
        System.out.println("Got message " + ++counter +": " + message);
    }
}
