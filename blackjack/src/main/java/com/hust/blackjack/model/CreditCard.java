package com.hust.blackjack.model;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class CreditCard {
    private String cardNumber;
    private String cardOwner;
    private double balance;
    private String email;

    //
    private List<Token> tokens = new ArrayList<>();

    @Getter
    @AllArgsConstructor
    public enum Action {
        ADD("ADD"),
        WITHDRAW("WDR");

        private final String value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CreditCard) {
            return this.cardNumber.equals(((CreditCard) obj).cardNumber);
        }
        return false;
    }
}
