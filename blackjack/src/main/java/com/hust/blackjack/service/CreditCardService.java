package com.hust.blackjack.service;

import com.hust.blackjack.exception.CreditCardException;
import com.hust.blackjack.exception.PlayerException;
import com.hust.blackjack.model.CreditCard;
import com.hust.blackjack.model.Player;
import com.hust.blackjack.repository.CreditCardRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Log4j2
@Service
public class CreditCardService {
    private final CreditCardRepository creditCardRepository;
    private final PlayerService playerService;

    public CreditCardService(CreditCardRepository creditCardRepository, PlayerService playerService) {
        this.creditCardRepository = creditCardRepository;
        this.playerService = playerService;
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
    public Player manageCreditCard(CreditCard.Action action, String playerName, String cardNumber, double amount)
            throws CreditCardException, PlayerException {
        Player player = playerService.getPlayerByName(playerName);
        CreditCard creditCard = this.getCreditCard(cardNumber);

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
}
