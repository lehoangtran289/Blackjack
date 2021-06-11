package com.hust.blackjack.model;

import com.hust.blackjack.exception.CardException;
import lombok.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
@Builder
@ToString
public class Deck {
    private List<Card> cardDeck;

    public Deck() throws CardException.InvalidCardException {
        cardDeck = new ArrayList<>();
        for (Card.Suit suit : Card.Suit.values()) {
            for (Card.Rank rank : Card.Rank.values()) {
                cardDeck.add(new Card(rank, suit));
            }
        }
        Collections.shuffle(cardDeck);
    }

    public void shuffle() {
        Collections.shuffle(cardDeck);
    }

    public Card dealCard() {
        Card card = cardDeck.get(cardDeck.size() - 1);    // last card in the deck
        cardDeck.remove(card);
        return card;
    }

    public int size() {
        return cardDeck.size();
    }
}
