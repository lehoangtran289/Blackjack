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

    private String tableId;

    // game state
    private Deck deck;
    private Hand dealerHand;
    private List<Player> players;
    private boolean isDealerHasBlackjack;
    private boolean continuePlaying;

    public Table(String tableId) {
        this.tableId = tableId;
        players = new ArrayList<>();
    }

    public void initDeck() {
        try {
            deck = new Deck();
        } catch (CardException.InvalidCardException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "Table{" +
                "players=" + players +
                '}';
    }
}
