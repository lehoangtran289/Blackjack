package com.hust.blackjack.model;

import lombok.*;

import java.nio.channels.SocketChannel;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
public class Player {
    // player's info
    private String playerName;
    private String password;
    private double bank;
    private SocketChannel channel;

    // game state properties
    private String tableId;
    private Hand hand;
    private double bet;
    private int isBlackjack;
    private int isBust;
    private int isStand;
    private Action choice;

    public Player(String playerName) {
        this.playerName = playerName;
        this.bank = 1000;
    }

    public Player(String playerName, String password) {
        this.playerName = playerName;
        this.password = password;
        this.bank = 1000;
    }

    public void refresh() {
        if (hand != null) {
            hand.clear();
        }
        bet = 0.0;
        isBlackjack = 0;
        isBust = 0;
        isStand = 0;
        choice = null;
    }

    public void placeBet(double bet) {
        setBet(bet);
        setBank(getBank() - bet);
    }

    public ResultState checkPlayerFinalState(int dealerHand) {
        int total = getHand().value();
        if (total > Table.MAXIMUM_SCORE) return ResultState.BUST;
        if (total == Table.MAXIMUM_SCORE) return ResultState.BLACKJACK;
        if (dealerHand > 21) {
            return ResultState.WIN;
        }
        if (total < dealerHand) return ResultState.LOSE;
        if (total == dealerHand) return ResultState.PUSH;
        else return ResultState.WIN;
    }

    @SneakyThrows
    @Override
    public String toString() {
        return "Player{" +
                "playerName='" + playerName + '\'' +
                ", bank=" + bank +
                ", channel=" + channel.getRemoteAddress() +
                ", tableId=" + tableId +
                '}';
    }
}
