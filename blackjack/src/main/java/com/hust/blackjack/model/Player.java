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
    private double balance;
    private SocketChannel channel;

    // game state properties
    private String tableId;
    private Hand hand;
    private double bet;
    private int isBlackjack;
    private int isBust;
    private int isStand;
    private Action choice;
    private int isReady;

    public Player(String playerName) {
        this.playerName = playerName;
        this.balance = 1000;
    }

    public Player(String playerName, String password) {
        this.playerName = playerName;
        this.password = password;
        this.balance = 1000;
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
        isReady = 0;
    }

    public void logout() {
        this.refresh();
        this.tableId = null;
        this.channel = null;
    }

    public void placeBet(double bet) {
        setBet(bet);
        setBalance(getBalance() - bet);
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
        String clientChannel = channel != null ?
                channel.getRemoteAddress().toString() :
                "null";
        return "Player{" +
                "playerName='" + playerName + '\'' +
                ", bank=" + balance +
                ", channel=" + clientChannel +
                ", tableId=" + tableId +
                '}';
    }
}
