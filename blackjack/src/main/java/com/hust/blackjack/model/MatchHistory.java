package com.hust.blackjack.model;

import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
@ToString
public class MatchHistory {
    private String playerName;
    private ResultState resultState;
    private double bet;
    private Date date;

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
}
