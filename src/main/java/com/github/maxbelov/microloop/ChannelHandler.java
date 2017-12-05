package com.github.maxbelov.microloop;

import java.nio.channels.SocketChannel;

public class ChannelHandler {
    protected ChannelHandler() {
    }

    public void onActive(SocketChannel socketChannel) {
    }

    public void onData(SocketChannel socketChannel, byte[] dataChunk) {
    }
}
