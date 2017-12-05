package com.github.maxbelov.microloop;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ChannelContext {
    private final SocketChannel channel;
    private final MessageHandler messageHandler;

    ChannelContext(SocketChannel channel, MessageHandler messageHandler) {
        this.channel = channel;
        this.messageHandler = messageHandler;
    }

    public void write(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        while (buffer.hasRemaining()) {
            try {
                channel.write(buffer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    void channelActive() {
        messageHandler.onActive(this);
    }

    void channelRead(byte[] dataChunk) {
        messageHandler.onData(this, dataChunk);
    }
}
