package com.hust.blackjack.model;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class Table {
    private static final int TABLE_SIZE = 4;
    private static final int MAXIMUM_SCORE = 21;
    private static final int DEALER_HIT_THRESHOLD = 17;
    private static final double MAXIMUM_BET = 200;
    private static final double MINIMUM_BET = 10;

    private List<Player> table;
    private boolean isDealerHasBlackjack;
    private boolean continuePlaying;
}
