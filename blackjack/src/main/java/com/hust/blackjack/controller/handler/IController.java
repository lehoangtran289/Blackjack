package com.hust.blackjack.controller.handler;

import com.hust.blackjack.exception.*;
import com.hust.blackjack.model.RequestType;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.List;

public interface IController {
    void processRequest(SocketChannel channel, RequestType requestType, List<String> request)
            throws IOException, RequestException, LoginException, PlayerException, CreditCardException, TableException;
}
