package com.github.maxbelov.microloop;

public interface ChannelHandler {
    void onData(byte[] dataChunk);
}
