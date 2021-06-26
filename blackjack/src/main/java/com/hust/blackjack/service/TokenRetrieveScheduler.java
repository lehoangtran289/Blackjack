package com.hust.blackjack.service;

import com.hust.blackjack.model.CreditCard;
import com.hust.blackjack.model.Token;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class TokenRetrieveScheduler {
    private final CreditCardService creditCardService;
    private final long expiredTime;

    public TokenRetrieveScheduler(CreditCardService creditCardService,
                                  @Value("${tokenTime}") long expiredTime) {
        this.creditCardService = creditCardService;
        this.expiredTime = expiredTime;
    }

    @Scheduled(fixedDelay = 600000)
    public void scheduleFixedDelayTask() {
        List<CreditCard> cards = creditCardService.findAll();
        for (CreditCard card : cards) {
            List<Token> tokens = card.getTokens();
            tokens.removeIf(
                    token -> ChronoUnit.MINUTES.between(LocalDateTime.now(), token.getCreatedAt()) >= expiredTime
            );
        }
    }
}
