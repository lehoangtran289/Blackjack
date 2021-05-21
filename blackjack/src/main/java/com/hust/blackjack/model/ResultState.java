package com.hust.blackjack.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@AllArgsConstructor
@Getter
public enum ResultState implements Serializable {
    WIN("win"),
    LOSE("lose"),
    BUST("bust"),
    PUSH("push"),
    BLACKJACK("blackjack");

    private final String value;
}
