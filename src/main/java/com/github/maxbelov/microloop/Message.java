package com.github.maxbelov.microloop;

import java.util.Arrays;

public class Message {
    private static final int DATA_LENGTH = 4;
    private byte[] data = new byte[DATA_LENGTH];
    private int lastIndex = 0;

    public boolean isFull() {
        return lastIndex >= DATA_LENGTH - 1;
    }

    public byte[] addData(byte[] chunk) { // todo: return only index?
        if (isFull()) {
            throw new RuntimeException("Message is full!");
        }

        int copyLength = Math.min(DATA_LENGTH - lastIndex, chunk.length);
        if (copyLength > 0) {
            System.arraycopy(chunk, 0, data, lastIndex, copyLength);
            lastIndex += copyLength;
            if (copyLength < chunk.length) {
                int remainingLength = chunk.length - copyLength;
                byte[] remaining = Arrays.copyOfRange(chunk, copyLength, chunk.length);
                return remaining;
            }
        }
        return new byte[0];
    }

    @Override
    public String toString() {
        return "Message{" +
                "data=" + new String(data) +
                '}';
    }
}
