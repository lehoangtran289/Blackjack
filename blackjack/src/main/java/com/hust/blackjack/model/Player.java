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
    private static final int MAXIMUM_SCORE = 21;                            // maximum score before bust
    private static final double BLACKJACK_PAYOUT_MULTIPLIER = 1.5;

    // player's info
    private String playerName;
    private String password;
    private double bank;
    private SocketChannel channel;

    // game state properties
    private String tableId;
    private Hand hand;
    private double bet;
    private int hasBlackjack;
    private int hasBust;
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
        hand.clear();
        bet = 0.0;
        hasBlackjack = 0;
        hasBust = 0;
        choice = null;
    }

    public void placeBet(double bet) {
        setBet(bet);
        setBank(getBank() - bet);
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
