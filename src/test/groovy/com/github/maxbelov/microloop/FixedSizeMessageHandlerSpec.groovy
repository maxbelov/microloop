package com.github.maxbelov.microloop

import spock.lang.Specification

class FixedSizeMessageHandlerSpec extends Specification {
    static final String dataString = "12345"
    static final int stringsCount = 20
    static final int messageSize = 4

    def "messages read correctly"() {
        setup:
        def messageAccumulator = new StringBuilder()
        def stringAccumulator = new StringBuilder()

        def handler = new FixedSizeMessageHandler(messageSize) {
            @Override
            protected void handleMessage(byte[] message) {
               messageAccumulator << new String(message)
            }
        }

        (1..stringsCount).each {
            stringAccumulator << dataString
            handler.onData(dataString.getBytes())
        }

        expect:
        messageAccumulator as String == stringAccumulator as String
    }

    //todo: add multiple invocations with different parameters
    def "long message is splitted correctly"() {
        setup:
        final dataLength = 100
        final messageSize = 7
        final totalMessagesLength = dataLength.intdiv(messageSize) as int

        def accumulator = []

        def handler = new FixedSizeMessageHandler(messageSize) {
            @Override
            protected void handleMessage(byte[] message) {
                accumulator << message
            }
        }

        def data = (1..dataLength) as byte[]
        handler.onData(data)

        expect:
        accumulator.size() == totalMessagesLength
        accumulator.stream().flatMap { Arrays.stream(it) }.collect() == data[0..<(totalMessagesLength * messageSize)]
    }
}
