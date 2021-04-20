package com.hust.blackjack.model;

import lombok.*;

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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CreditCard) {
            return this.cardNumber.equals(((CreditCard) obj).cardNumber);
        }
        return false;
    }
}
