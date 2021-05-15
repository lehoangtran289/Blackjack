package com.hust.blackjack.model.dto;

import com.hust.blackjack.model.Player;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
public class PlayerGameInfo {
    private Player player;
    private double moneyEarn;
    private int win;
    private int lose;
    private int push;
    private int bust;
    private int blackjack;

    @Override
    public String toString() {
        return "PlayerGameInfo{" +
                "player=" + player.getPlayerName() +
                ", moneyEarn=" + moneyEarn +
                ", win=" + win +
                ", lose=" + lose +
                ", push=" + push +
                ", bust=" + bust +
                ", blackjack=" + blackjack +
                '}';
    }
}
