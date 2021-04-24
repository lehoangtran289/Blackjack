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
}
