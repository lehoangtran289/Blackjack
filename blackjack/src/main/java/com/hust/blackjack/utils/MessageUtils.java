package com.hust.blackjack.utils;

import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

@Log4j2
public class MessageUtils {

    public static void writeToChannel(SocketChannel channel, String msg) throws IOException {
//        msg += "\n"; // terminal testing purposes
        log.info("Response to channel {}: {}", channel.getRemoteAddress(), msg);
        channel.write(ByteBuffer.wrap(msg.getBytes()));
    }

    public static void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
