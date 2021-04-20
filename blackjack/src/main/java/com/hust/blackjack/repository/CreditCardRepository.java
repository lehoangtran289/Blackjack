package com.hust.blackjack.repository;

import com.hust.blackjack.model.CreditCard;

import java.util.List;
import java.util.Optional;

public interface CreditCardRepository {

    Optional<CreditCard> findByCardNumber(String cardNumber);

    List<CreditCard> findAll();
}
