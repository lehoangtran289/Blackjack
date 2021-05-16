package com.hust.blackjack.model;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class Table {
    public static final int TABLE_SIZE = 4;
    public static final int MAXIMUM_SCORE = 21;
    public static final int DEALER_HIT_THRESHOLD = 17;
    public static final double MAXIMUM_BET = 200;
    public static final double MINIMUM_BET = 10;

    private int tableId;
    private List<Player> players;
    private boolean isDealerHasBlackjack;
    private boolean continuePlaying;

    public Table(int tableId) {
        this.tableId = tableId;
        players = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "Table{" +
                "players=" + players +
                '}';
    }
}
