package com.hust.blackjack.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Action {
    HIT("HIT"),
    STAND("STAND");

    private final String value;
}
