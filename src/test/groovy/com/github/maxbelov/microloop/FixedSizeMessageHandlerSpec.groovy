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
}
