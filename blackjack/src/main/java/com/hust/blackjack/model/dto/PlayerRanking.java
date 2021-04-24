package com.hust.blackjack.model.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
public class PlayerRanking {
    private String playerName;
    private int playerRank;
    private double moneyEarn;

    @Override
    public String toString() {
        return playerRank + " " + playerName + " " + moneyEarn;
    }
}
