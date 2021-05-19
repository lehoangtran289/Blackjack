package com.hust.blackjack.model;

import com.hust.blackjack.exception.CardException;
import lombok.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
@Builder
@ToString
public class Deck {
    private List<Card> deck;

    public Deck() throws CardException.InvalidCardException {
        deck = new ArrayList<>();
        for (Card.Suit suit : Card.Suit.values()) {
            for (Card.Rank rank : Card.Rank.values()) {
                deck.add(new Card(rank, suit));
            }
        }
        Collections.shuffle(deck);
    }

    public void shuffle() {
        Collections.shuffle(deck);
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
