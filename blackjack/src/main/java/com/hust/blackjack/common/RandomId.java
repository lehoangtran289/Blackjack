package com.hust.blackjack.common;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RandomId {
    public static final int IDLENGTH = 4;

    public static String generate() {
        return RandomStringUtils.random(IDLENGTH, true, true);
    }
}
