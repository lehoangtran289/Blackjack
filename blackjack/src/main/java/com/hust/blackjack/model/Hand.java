package com.hust.blackjack.model;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class Hand {
    private List<Card> hand;

    public Hand() {
        this.hand = new ArrayList<>();
    }

    public Card getCard(int index) {
        return hand.get(index);
    }

    public void addCard(Card newCard) {
        hand.add(newCard);
    }

    public int value() {
        return hand.stream()
                .map(Card::value)
                .reduce(0, Integer::sum);
    }

    public int size() {
        return hand.size();
    }

    public void clear() {
        hand.clear();
    }
}
