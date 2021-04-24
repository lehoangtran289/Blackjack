package com.hust.blackjack.model;

import com.hust.blackjack.exception.CardException;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
public class Card {
    private Rank rank;
    private Suit suit;

    public Card(Rank rank, Suit suit) throws CardException.InvalidCardException {
        if (!Rank.isValid(rank.getValue()) || !Suit.isValid(suit.getValue())) {
            throw new CardException.InvalidCardException();
        } else {
            this.rank = rank;
            this.suit = suit;
        }
    }

    @AllArgsConstructor
    @Getter
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

        private final int value;

        public static boolean isValid(int value) {
            return value < 1 || value > 10;
        }

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    @AllArgsConstructor
    @Getter
    public enum Suit {
        CLUBS("club"),
        DIAMONDS("diamonds"),
        HEARTS("hearts"),
        SPADES("spades");

        private final String value;

        private static final List<String> suits = new ArrayList<>();
        static {
            for (Suit suit : Suit.values()) {
                suits.add(suit.getValue());
            }
        }

        public static boolean isValid(String suit) {
            return suits.contains(suit);
        }

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    public int value() {
        return this.rank.getValue();
    }

    public Rank rank() {
        return this.rank;
    }

    @Override
    public String toString() {
        return this.rank + "_of_" + this.suit;
    }
}