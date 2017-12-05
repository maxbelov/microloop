package com.github.maxbelov.microloop;

public class FixedSizeMessageHandler extends MessageHandler {
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
    public void onData(ChannelContext ctx, Object message) {
        byte[] dataChunk = (byte[]) message;
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
                handleMessage(ctx, data);
                data = new byte[messageSize];
                lastIndex = 0;
            }

            chunkPosition += copyLength;
        }
    }

    private void handleMessage(ChannelContext ctx, byte[] message) {
        System.out.printf("Server received: %s\n", new String(message));
        ctx.write(message);
    }
}
