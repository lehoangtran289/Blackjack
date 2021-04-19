package com.hust.blackjack.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class Card {
    private final Rank rank;
    private final Suit suit;

    @AllArgsConstructor
    public enum Rank {
        ACE(1),
        TWO(2),
        THREE(3),
        FOUR(4),
        FIVE(5),
        SIX(6),
        SEVEN(7),
        EIGHT(8),
        NINE(9),
        TEN(10),
        QUEEN(10),
        KING(10),
        JACK(10);

        private final int value;  // value of the rank

        public int value() {
            return value;
        }

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    @AllArgsConstructor
    public enum Suit {
        CLUBS("club"),
        DIAMONDS("diamonds"),
        HEARTS("hearts"),
        SPADES("spades");

        private final String value;

        public String value() {
            return value;
        }

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    public int value() {
        return this.rank.value();
    }

    public Rank rank() {
        return this.rank;
    }

    @Override
    public String toString() {
        return this.rank + "_of_" + this.suit;
    }
}
