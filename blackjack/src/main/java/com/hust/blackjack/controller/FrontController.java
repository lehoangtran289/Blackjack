package com.hust.blackjack.controller;

import com.hust.blackjack.exception.*;
import com.hust.blackjack.model.Player;
import com.hust.blackjack.model.RequestType;
import com.hust.blackjack.service.PlayerService;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.hust.blackjack.utils.MessageUtils.writeToChannel;

@Log4j2
@Service
public class FrontController {

    private final Dispatcher dispatcher;
    private final PlayerService playerService;

    public FrontController(Dispatcher dispatcher, PlayerService playerService) {
        this.dispatcher = dispatcher;
        this.playerService = playerService;
    }

    /**
     * frontController centralize all incoming requests. Validate & then dispatch to Dispatcher
     */
    public void dispatchRequest(SocketChannel channel, String requestMsg) throws RequestException,
            IOException, LoginException, PlayerException, CreditCardException, TableException {
        List<String> request = new ArrayList<>(Arrays.asList(requestMsg.split(" ")));
        if (request.isEmpty()) {
            writeToChannel(channel, "FAIL=Invalid request");
            throw new RequestException("Invalid request, request empty");
        }

        // get request type
        RequestType requestType;
        try {
            requestType = RequestType.from(request.get(0));
            log.info("Receive {} request", requestType.getValue());
        } catch (RequestException ex) {
            writeToChannel(channel, "FAIL=Invalid request");
            throw new RequestException("Invalid request type " + request.get(0));
        }

        // check request length
        if (!isRequestLengthValid(request)) {
            writeToChannel(channel, "FAIL=Invalid " + requestType.getValue() + " request length");
            throw new RequestException.InvalidRequestLengthException(requestType.getValue() + "=" + request.size());
        }

        // dispatch request
        dispatcher.dispatch(channel, requestType, request);
    }

    private boolean isRequestLengthValid(List<String> request) throws RequestException {
        switch (RequestType.from(request.get(0))) {
            case CHAT:
                return request.size() > 3;
            case SEARCHINFO:
                return request.size() == 2 || request.size() == 1;
            case PLAY:
                return request.size() >= 2;
            case RANKING:
            case LOGOUT:
            case INFO: // INFO {username} {bank} {money_earn} {Win} {Lose} {Push} {Bust} {Blackjack}
            case HISTORY:
                return request.size() == 2;
            case LOGIN:
            case SIGNUP:
            case HIT:
            case STAND:
            case QUIT:
            case CONTINUE:
            case BETQUIT:
            case CREATEROOM:
                return request.size() == 3;
            case ADDMONEY:
            case WITHDRAWMONEY:
            case BET:
                return request.size() == 4;
            default:
                throw new RequestException.InvalidRequestLengthException();
        }
    }

    public void processChannelClose(SocketChannel client) {
        List<Player> players = playerService.getAllPlayers();
        for (Player player : players) {
            if (player.getChannel() == client) {
                player.logout();
                return;
            }
        }
    }
}
