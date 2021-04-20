package com.hust.blackjack.repository.impl;

import com.hust.blackjack.model.CreditCard;
import com.hust.blackjack.repository.CreditCardRepository;
import com.hust.blackjack.repository.seed.Seed;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CreditCardRepositoryImpl implements CreditCardRepository {
    private final Seed seed;
    private List<CreditCard> creditCards;

    @PostConstruct
    public void init() {
        creditCards = seed.getCreditCards();
    }

    @Override
    public Optional<CreditCard> findByCardNumber(String cardNumber) {
        return creditCards.stream()
                .filter(c -> StringUtils.equals(c.getCardNumber(), cardNumber))
                .findAny();
    }

    @Override
    public List<CreditCard> findAll() {
        return creditCards;
    }
}
