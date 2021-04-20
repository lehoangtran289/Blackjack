package com.hust.blackjack.service;

import com.hust.blackjack.exception.RequestException;
import com.hust.blackjack.model.Player;
import com.hust.blackjack.repository.CreditCardRepository;
import com.hust.blackjack.repository.MatchHistoryRepository;
import com.hust.blackjack.repository.PlayerRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Log4j2
@Service
public class RequestProcessingService {
    private final CreditCardRepository creditCardRepository;
    private final PlayerRepository playerRepository;
    private final MatchHistoryRepository matchHistoryRepository;

    public RequestProcessingService(CreditCardRepository creditCardRepository,
                                    PlayerRepository playerRepository,
                                    MatchHistoryRepository matchHistoryRepository) {
        this.creditCardRepository = creditCardRepository;
        this.playerRepository = playerRepository;
        this.matchHistoryRepository = matchHistoryRepository;
    }

    //TODO: standardize Response to client :D
    public void process(SocketChannel channel, String requestMsg) throws RequestException, IOException {
        List<String> request = new ArrayList<>(Arrays.asList(requestMsg.split(" ")));
        if (request.isEmpty())
            throw new RequestException("Invalid request");

        switch (request.get(0)) {
            case "LOGIN": {
                if (!isRequestLengthValid(request)) {
                    throw new RequestException("Invalid request length");
                }
                String playerName = request.get(1);
                String password = request.get(2);
                Optional<Player> optionalPlayer = playerRepository.getPlayerByNameAndPassword(playerName, password);
                if (optionalPlayer.isEmpty()) {
                    writeToChannel(channel, "LOGIN FAIL\n");
                    log.error("Invalid playerName password");
                    break;
                }
                Player player = optionalPlayer.get();
                if (player.getChannel() != null) {
                    writeToChannel(channel, "PLAYER ALREADY LOGIN\n");
                    log.error("player already login");
                    break;
                }
                writeToChannel(channel, "LOGIN SUCCESS\n");
                player.setChannel(channel);
                break;
            }
            case "LOGOUT": {
                if (!isRequestLengthValid(request)) {
                    writeToChannel(channel, "INVALID REQUEST LENGTH\n");
                    throw new RequestException("Invalid request length");
                }
                String playerName = request.get(1);
                Optional<Player> optionalPlayer = playerRepository.getPlayerByName(playerName);
                if (optionalPlayer.isEmpty()) {
                    writeToChannel(channel, "INVALID PLAYERNAME\n");
                    log.error("Invalid playerName");
                    break;
                }
                Player player = optionalPlayer.get();
                if (player.getChannel() == null) {
                    writeToChannel(channel, "PLAYER HAVEN'T LOGIN\n");
                    log.error("player haven't login");
                    break;
                }
                if (player.getChannel() != channel) {
                    writeToChannel(channel, "INVALID CHANNEL\n");
                    log.error("invalid channel to logout");
                    break;
                }
                writeToChannel(channel, "LOGOUT SUCCESS\n");
                player.setChannel(null);
                break;
            }
            case "SIGNUP": {
                if (!isRequestLengthValid(request)) {
                    break;
                }
                String playerName = request.get(1);
                String password = request.get(2);
                if (playerRepository.isPlayerExists(playerName)) {
                    writeToChannel(channel, "PLAYERNAME EXISTS\n");
                    log.error("playerName {} exists", playerName);
                    break;
                }
                Player newPlayer = new Player(playerName, password);
                playerRepository.save(newPlayer);
                writeToChannel(channel, "SIGNUPSUCCESS\n");
                break;
            }
            default:
                throw new RequestException.InvalidRequestTypeException(request.get(0));
        }
    }

    private boolean isRequestLengthValid(List<String> request) throws RequestException.InvalidRequestLengthException {
        switch (request.get(0)) {
            case "LOGIN":
                return request.size() == 3;
            case "LOGOUT":
                return request.size() == 2;
            case "SIGNUP":
                return request.size() == 3;
            default:
                throw new RequestException.InvalidRequestLengthException();
        }
    }

    public void writeToChannel(SocketChannel channel, String msg) throws IOException {
        channel.write(ByteBuffer.wrap(msg.getBytes()));
    }

}
