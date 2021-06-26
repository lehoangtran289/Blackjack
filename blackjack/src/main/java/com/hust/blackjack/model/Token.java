package com.hust.blackjack.model;

import com.hust.blackjack.common.RandomId;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Token {
    private final String username;
    private final String secret;
    private final LocalDateTime createdAt;

    public Token(String username) {
        this.username = username;
        this.createdAt = LocalDateTime.now();
        this.secret = RandomId.generateToken();
    }
}
