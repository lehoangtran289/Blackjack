package com.hust.blackjack.common;

import org.apache.commons.lang3.RandomStringUtils;

public class RandomId {
    public static int IDLENGTH = 4;

    public static String generate() {
        return RandomStringUtils.random(IDLENGTH, true, true);
    }

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            System.out.println(generate());
        }
    }
}
