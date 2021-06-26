package com.hust.blackjack.controller.handler;

import com.hust.blackjack.exception.CreditCardException;
import com.hust.blackjack.exception.PlayerException;
import com.hust.blackjack.exception.RequestException;
import com.hust.blackjack.model.CreditCard;
import com.hust.blackjack.model.Player;
import com.hust.blackjack.model.RequestType;
import com.hust.blackjack.model.Token;
import com.hust.blackjack.service.CreditCardService;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.List;

import static com.hust.blackjack.utils.MessageUtils.writeToChannel;

@Log4j2
@Service
public class CreditCardController implements IController {

    private final CreditCardService creditCardService;

    public CreditCardController(CreditCardService creditCardService) {
        this.creditCardService = creditCardService;
    }

    @Override
    public void processRequest(SocketChannel channel, RequestType requestType, List<String> request)
            throws RequestException, IOException, PlayerException, CreditCardException {
        switch (requestType) {
            case CARDREQUEST: {
                String cardId = request.get(1);
                String username = request.get(2);
                try {
                    Token token = creditCardService.sendToken(cardId, username);
                    writeToChannel(channel, "RQOK");
                    log.info("New token sent, token = {} ", token.getSecret());
                } catch (PlayerException.PlayerNotFoundException e) {
                    writeToChannel(channel, "RQFAIL=Player not found");
                    throw e;
                } catch (CreditCardException.CreditCardNotFoundException e) {
                    writeToChannel(channel, "RQFAIL=credit card not found");
                    throw e;
                }
                break;
            }
            case ADDMONEY: {
                String playerName = request.get(1);
                String cardNumber = request.get(2);
                String secret = request.get(3);
                double amount = Double.parseDouble(request.get(4));
                try {
                    Player player = creditCardService.manageCreditCard(
                            CreditCard.Action.ADD, playerName, cardNumber, amount, secret
                    );
                    writeToChannel(channel, "ADDSUCCESS=" + player.getPlayerName() + " " + player.getBank());
                    log.info("Add money from card {} to player {} success. New balance: {} ",
                            cardNumber, playerName, player.getBank()
                    );
                } catch (PlayerException.PlayerNotFoundException e) {
                    writeToChannel(channel, "ADDFAIL=Player not found");
                    throw e;
                } catch (CreditCardException.CreditCardNotFoundException e) {
                    writeToChannel(channel, "ADDFAIL=credit card not found");
                    throw e;
                } catch (CreditCardException.NotEnoughBalanceException e) {
                    writeToChannel(channel, "ADDFAIL=Credit card balance not enough");
                    throw e;
                }
                break;
            }
            case WITHDRAWMONEY: {
                String playerName = request.get(1);
                String cardNumber = request.get(2);
                String secret = request.get(3);
                double amount = Double.parseDouble(request.get(3));
                try {
                    Player player = creditCardService.manageCreditCard(
                            CreditCard.Action.WITHDRAW, playerName, cardNumber, amount, secret
                    );
                    writeToChannel(channel, "WDRSUCCESS=" + player.getPlayerName() + " " + player.getBank());
                    log.info("Withdrawn money from player {} to card {} success. New balance: {} ",
                            playerName, cardNumber, player.getBank()
                    );
                } catch (PlayerException.PlayerNotFoundException e) {
                    writeToChannel(channel, "WDRFAIL=Player not found");
                    throw e;
                } catch (CreditCardException.CreditCardNotFoundException e) {
                    writeToChannel(channel, "WDRFAIL=credit card not found");
                    throw e;
                } catch (PlayerException.NotEnoughBankBalanceException e) {
                    writeToChannel(channel, "WDRFAIL=Player bank balance not enough");
                    throw e;
                }
                break;
            }
            default:
                writeToChannel(channel, "FAIL=Invalid request");
                throw new RequestException.InvalidRequestTypeException(request.get(0));
        }
    }
}
