package com.github.maxbelov.microloop;

import java.util.stream.IntStream;

public abstract class FixedSizeMessageHandler implements ChannelHandler {
    private final int messageSize;
    private byte[] data;
    private int lastIndex = 0;

    public FixedSizeMessageHandler(final int messageSize) {
        if (messageSize < 1) {
            throw new IllegalArgumentException("Message size should be strictly positive number!");
        }
        this.messageSize = messageSize;
        data = new byte[messageSize];
    }


    @Override
    public void onData(final byte[] dataChunk) {
        if (dataChunk == null) {
            throw new NullPointerException("Data chunk must not be null!");
        }

        int chunkPosition = 0;

        while (chunkPosition < dataChunk.length) {
            final int remainingLength = dataChunk.length - chunkPosition;
            final int copyLength = Math.min(messageSize - lastIndex, remainingLength);
            System.arraycopy(dataChunk, chunkPosition, data, lastIndex, copyLength);
            lastIndex += copyLength;

            final boolean messageComplete = lastIndex >= messageSize;
            if (messageComplete) {
                handleMessage(data);
                data = new byte[messageSize];
                lastIndex = 0;
            }

            chunkPosition += copyLength;
        }
    }

    protected abstract void handleMessage(byte[] message);

    public static void main(String[] args) {
        FixedSizeMessageHandler h = new FixedSizeMessageHandler(4) {
            @Override
            protected void handleMessage(byte[] message) {
                System.out.printf("Message: %s\n", new String(message));
            }
        };
        byte[] bytes = "12345".getBytes();
        IntStream.range(0, 4).forEach(i -> h.onData(bytes));
    }

}
