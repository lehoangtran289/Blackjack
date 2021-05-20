package com.hust.blackjack.model;

import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class Hand {
    private List<Card> cards;

    public Hand() {
        this.cards = new ArrayList<>();
    }

    public Card getCard(int index) {
        return cards.get(index);
    }

    public void addCard(Card newCard) {
        cards.add(newCard);
    }

    public int value() {
        return cards.stream()
                .map(Card::value)
                .reduce(0, Integer::sum);
    }

    public int size() {
        return cards.size();
    }

    public void clear() {
        cards.clear();
    }

    public boolean isBlackJack() {
        return this.totalSum() == Table.MAXIMUM_SCORE;
    }

    public boolean isBust() {
        return this.totalSum() > Table.MAXIMUM_SCORE;
    }

    public int totalSum() {
        int sum = 0;
        for (Card c : cards) {
            if (c.getRank() == Card.Rank.ACE && sum <= 11) {
                sum += 10;
                continue;
            }
            if (c.getRank() == Card.Rank.JACK ||
                    c.getRank() == Card.Rank.QUEEN ||
                    c.getRank() == Card.Rank.KING) {
                sum += 10;
                continue;
            }
            sum += c.getRank().getValue();
        }
        return sum;
    }

    @Override
    public String toString() {
        return "Hand{" +
                "cards=" + cards +
                '}';
    }
}
