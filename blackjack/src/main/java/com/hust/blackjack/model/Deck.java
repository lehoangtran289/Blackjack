package com.hust.blackjack.model;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class Deck {
    private List<Card> deck;

    public Deck() {
        for (Card.Suit suit : Card.Suit.values()) {
            for (Card.Rank rank : Card.Rank.values()) {
                deck.add(new Card(rank, suit));
            }
        }
    }

    public Card dealCard() {
        Card card = deck.get(deck.size() - 1);    // last card in the deck
        deck.remove(card);
        return card;
    }

    public int size() {
        return deck.size();
    }
}
