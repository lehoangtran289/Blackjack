package com.hust.blackjack.service;

import com.hust.blackjack.exception.CreditCardException;
import com.hust.blackjack.exception.PlayerException;
import com.hust.blackjack.model.CreditCard;
import com.hust.blackjack.model.Player;
import com.hust.blackjack.model.Token;
import com.hust.blackjack.repository.CreditCardRepository;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Log4j2
@Service
public class CreditCardService {
    private final CreditCardRepository creditCardRepository;
    private final PlayerService playerService;
    private final EmailService emailService;

    public CreditCardService(CreditCardRepository creditCardRepository, PlayerService playerService,
                             EmailService emailService) {
        this.creditCardRepository = creditCardRepository;
        this.playerService = playerService;
        this.emailService = emailService;
    }

    public CreditCard getCreditCard(String creditCardId) throws CreditCardException {
        Optional<CreditCard> optionalCreditCard = creditCardRepository.findByCardNumber(creditCardId);
        if (optionalCreditCard.isEmpty()) {
            log.error("credit card {} not found", creditCardId);
            throw new CreditCardException.CreditCardNotFoundException();
        }
        return optionalCreditCard.get();
    }

    @Transactional
    public Player manageCreditCard(CreditCard.Action action, String playerName, String cardNumber, double amount, String secret)
            throws CreditCardException, PlayerException {
        Player player = playerService.getPlayerByName(playerName);
        CreditCard creditCard = this.getCreditCard(cardNumber);

        if (!isValidToken(creditCard, playerName, secret)) {
            log.error("Token not valid");
            throw new CreditCardException.InvalidToken();
        }

        // handle request
        if (action == CreditCard.Action.ADD) {
            if (creditCard.getBalance() < amount) {
                log.error("Credit card {} balance not enough, balance = {}", creditCard, creditCard.getBalance());
                throw new CreditCardException.NotEnoughBalanceException();
            }
            creditCard.setBalance(creditCard.getBalance() - amount);
            player.setBank(player.getBank() + amount);
        }
        else if (action == CreditCard.Action.WITHDRAW) {
            if (player.getBank() < amount) {
                log.error("Player {} bank balance not enough, balance = {}", playerName, player.getBank());
                throw new PlayerException.NotEnoughBankBalanceException();
            }
            player.setBank(player.getBank() - amount);
            creditCard.setBalance(creditCard.getBalance() + amount);
        }
        return player;
    }

    private boolean isValidToken(CreditCard card, String username, String secret) {
        List<Token> tokens = card.getTokens();
        for (Token token: tokens) {
            if (StringUtils.equals(token.getSecret(), secret) &&
                    StringUtils.equals(token.getUsername(), username) &&
                    ChronoUnit.MINUTES.between(LocalDateTime.now(), token.getCreatedAt()) < 5
            ) {
                card.getTokens().remove(token);
                return true;
            }
        }
        return false;
    }

    public Token sendToken(String cardNumber, String username) throws PlayerException, CreditCardException {
        Player player = playerService.getPlayerByName(username);
        CreditCard creditCard = this.getCreditCard(cardNumber);

        // generate token
        Token token = new Token(player.getPlayerName());
        if (creditCard.getTokens() == null) {
            creditCard.setTokens(new ArrayList<>());
        }
        creditCard.getTokens().add(token);

        //
        String emailMessage = "Your token is: " + token.getSecret();
        emailService.sendEmail(creditCard.getEmail(), "Token Confirmation", emailMessage);

        return token;
    }

    public List<CreditCard> findAll() {
        return creditCardRepository.findAll();
    }
}
