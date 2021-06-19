package com.hust.blackjack.controller;

import com.hust.blackjack.controller.handler.CreditCardController;
import com.hust.blackjack.controller.handler.GameController;
import com.hust.blackjack.controller.handler.LoginController;
import com.hust.blackjack.controller.handler.QueryController;
import com.hust.blackjack.exception.*;
import com.hust.blackjack.model.RequestType;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.List;

import static com.hust.blackjack.utils.MessageUtils.writeToChannel;

@Log4j2
@Service
public class Dispatcher {
    private final CreditCardController cardController;
    private final GameController gameController;
    private final LoginController loginController;
    private final QueryController queryController;

    public Dispatcher(CreditCardController cardController, GameController gameController,
                      LoginController loginController, QueryController queryController) {
        this.cardController = cardController;
        this.gameController = gameController;
        this.loginController = loginController;
        this.queryController = queryController;
    }

    public void dispatch(SocketChannel channel, RequestType requestType, List<String> request)
            throws RequestException, IOException, LoginException, PlayerException, CreditCardException, TableException {
        switch (requestType) {
            case LOGIN:
            case LOGOUT:
            case SIGNUP:
                loginController.processRequest(channel, requestType, request);
                break;

            case HISTORY:
            case INFO:
            case SEARCHINFO:
            case RANKING:
                queryController.processRequest(channel, requestType, request);
                break;

            case ADDMONEY:
            case WITHDRAWMONEY:
                cardController.processRequest(channel, requestType, request);
                break;

            case CREATEROOM:
            case PLAY:
            case BET:
            case BETQUIT:
            case HIT:
            case STAND:
            case QUIT:
            case CONTINUE:
                gameController.processRequest(channel, requestType, request);
                break;

            default:
                writeToChannel(channel, "FAIL=Invalid request");
                throw new RequestException.InvalidRequestTypeException(request.get(0));
        }
    }
}
