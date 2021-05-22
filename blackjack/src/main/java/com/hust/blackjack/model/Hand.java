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

    public int size() {
        return cards.size();
    }

    public void clear() {
        cards.clear();
    }

    public boolean isBlackJack() {
        return this.value() == Table.MAXIMUM_SCORE;
    }

    public boolean isBust() {
        return this.value() > Table.MAXIMUM_SCORE;
    }

    public int value() {
        int sum = 0;
        List<Card> aces = new ArrayList<>();
        for (Card c : cards) {
            if (c.getRank() == Card.Rank.ACE) {
                aces.add(c);
            } else {
                if (c.getRank() == Card.Rank.JACK ||
                        c.getRank() == Card.Rank.QUEEN ||
                        c.getRank() == Card.Rank.KING) {
                    sum += 10;
                } else {
                    sum += c.getRank().getValue();
                }
            }
        }
        for (Card ignored : aces) {
            sum += sum <= 11 ? 11 : 1;
        }
        return sum;
    }

//    public static void main(String[] args) throws CardException.InvalidCardException {
//        List<Card> cards = Arrays.asList(
//                new Card(Card.Rank.EIGHT, Card.Suit.DIAMONDS),
//                new Card(Card.Rank.ACE, Card.Suit.CLUBS),
//                new Card(Card.Rank.KING, Card.Suit.DIAMONDS)
//        );
//        Hand hand = new Hand();
//        hand.setCards(cards);
//        System.out.println(hand.totalSum());
//    }

    @Override
    public String toString() {
        return "Hand{" +
                "cards=" + cards +
                '}';
    }
}
