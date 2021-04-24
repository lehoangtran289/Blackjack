package com.hust.blackjack.model.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
public class PlayerGameInfo {
    private String playerName;
    private double moneyEarn;
    private int win;
    private int lose;
    private int push;
    private int bust;
    private int blackjack;
}
