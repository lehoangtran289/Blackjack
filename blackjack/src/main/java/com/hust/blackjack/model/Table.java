package com.hust.blackjack.model;

import com.hust.blackjack.exception.CardException;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class Table {
    public static final int TABLE_SIZE = 3;
    public static final int MAXIMUM_SCORE = 21;
    public static final int DEALER_HIT_THRESHOLD = 17;
    public static final double MAXIMUM_BET = 200;
    public static final double MINIMUM_BET = 10;
    public static final double BLACKJACK_RATE = 1.5;

    private String tableId;

    // game state
    private Deck deck;
    private Hand dealerHand;
    private List<Player> players;
    private String playerTurn;      // name of current turn's player
    private int isPlaying;

    public Table(String tableId) {
        this.tableId = tableId;
        players = new ArrayList<>();
    }

    public void refreshAndInitDeck() {
        try {
            deck = new Deck();
        } catch (CardException.InvalidCardException e) {
            e.printStackTrace();
        }
        if (dealerHand != null) {
            dealerHand = null;
        }
        if (playerTurn != null) {
            playerTurn = null;
        }
        isPlaying = 1;
    }

    public void resetTable() {
        try {
            deck = new Deck();
        } catch (CardException.InvalidCardException e) {
            e.printStackTrace();
        }
        players = new ArrayList<>();
        if (dealerHand != null) {
            dealerHand = null;
        }
        if (playerTurn != null) {
            playerTurn = null;
        }
        isPlaying = 0;
    }

    public Player getNextTurn() {
        for (Player p : this.getPlayers()) {
            if (p.getIsStand() == 0) {
                return p;
            }
        }
        return null;
    }

    public boolean isAllReady() {
        return players.stream().allMatch(p -> p.getIsReady() == 1);
    }

    public boolean isAllBet() {
        return this.getPlayers().stream().allMatch(p -> p.getBet() != 0);
    }

    @Override
    public String toString() {
        return "Table{" +
                "players=" + players +
                '}';
    }
}
