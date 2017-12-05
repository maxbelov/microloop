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
import java.util.Iterator;
import java.util.function.Supplier;

public class LoopServer {
    private static final int CHANNEL_BUFFER_SIZE = 1024;
    private final InetSocketAddress bindAddress;
    private final Supplier<? extends MessageHandler> handlerSupplier;
    private Selector selector;

    public LoopServer(String host, int port, Supplier<? extends MessageHandler> handlerSupplier) {
        this.bindAddress = new InetSocketAddress(host, port);
        this.handlerSupplier = handlerSupplier;
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

        MessageHandler channelHandler = handlerSupplier.get(); // todo: rewrite to initializer
        ChannelContext channelContext = new ChannelContext(channel, channelHandler);
        channel.register(selector, SelectionKey.OP_READ, channelContext);
        channelContext.channelActive(); // todo: remove from accept thread
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(CHANNEL_BUFFER_SIZE);

        int bytesRead = channel.read(buffer);

        if (bytesRead == -1) {
            Socket socket = channel.socket();
            SocketAddress remoteAddr = socket.getRemoteSocketAddress();
            System.out.println("Connection closed by client: " + remoteAddr);
            channel.close();
            key.cancel();
            return;
        }

        channel.read(buffer);

        byte[] data = Arrays.copyOf(buffer.array(), bytesRead);

        System.out.println(String.format("got %d bytes", data.length));
        ChannelContext channelContext = (ChannelContext) key.attachment();
        channelContext.channelRead(data);
    }
}
