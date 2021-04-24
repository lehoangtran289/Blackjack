package com.hust.blackjack.service;

import com.hust.blackjack.exception.CreditCardException;
import com.hust.blackjack.exception.PlayerException;
import com.hust.blackjack.exception.RequestException;
import com.hust.blackjack.model.CreditCard;
import com.hust.blackjack.model.Player;
import com.hust.blackjack.model.RequestType;
import com.hust.blackjack.model.dto.PlayerRanking;
import com.hust.blackjack.model.dto.PlayerGameInfo;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Log4j2
@Service
public class RequestProcessingService {
    private final CreditCardService creditCardService;
    private final PlayerService playerService;
    private final MatchHistoryService matchHistoryService;

    public RequestProcessingService(CreditCardService creditCardService,
                                    PlayerService playerService,
                                    MatchHistoryService matchHistoryService) {
        this.creditCardService = creditCardService;
        this.playerService = playerService;
        this.matchHistoryService = matchHistoryService;
    }

    public void process(SocketChannel channel, String requestMsg) throws RequestException, IOException {
        List<String> request = new ArrayList<>(Arrays.asList(requestMsg.split(" ")));
        if (request.isEmpty()) {
            writeToChannel(channel, "FAIL - Invalid request");
            throw new RequestException("Invalid request, request empty");
        }

        RequestType requestType;
        try {
            requestType = RequestType.from(request.get(0));
            log.info("Receive {} request", requestType.getValue());
        } catch (RequestException ex) {
            writeToChannel(channel, "FAIL - Invalid request");
            throw new RequestException("Invalid request type");
        }

        // TODO: refactor repo - service
        switch (requestType) {
            case LOGIN: {
                if (!isRequestLengthValid(request)) {
                    writeToChannel(channel, "FAIL - Invalid LOGIN request length");
                    throw new RequestException("Invalid request length");
                }
                if (playerService.isChannelLoggedIn(channel)) {
                    writeToChannel(channel, "LOGINFAIL - Channel already login, logout first");
                    log.error("channel already login");
                    break;
                }
                String playerName = request.get(1);
                String password = request.get(2);
                Optional<Player> optionalPlayer = playerService.getPlayerByNameAndPassword(playerName, password);
                if (optionalPlayer.isEmpty()) {
                    writeToChannel(channel, "LOGINFAIL - Username or password incorrect");
                    log.error("Invalid playerName password");
                    break;
                }
                Player player = optionalPlayer.get();
                if (player.getChannel() != null) {
                    writeToChannel(channel, "LOGINFAIL - Player already login");
                    log.error("player already login");
                    break;
                }
                writeToChannel(channel, "LOGINSUCCESS " + player.getPlayerName() + " " + player.getBank());
                log.info("Player {} login success at channel {}", player.getPlayerName(), player.getChannel());
                player.setChannel(channel);
                break;
            }
            case LOGOUT: {
                if (!isRequestLengthValid(request)) {
                    writeToChannel(channel, "FAIL - Invalid LOGOUT request length");
                    throw new RequestException("Invalid request length");
                }
                String playerName = request.get(1);
                Optional<Player> optionalPlayer = playerService.getPlayerByName(playerName);
                if (optionalPlayer.isEmpty()) {
                    writeToChannel(channel, "LOGOUTFAIL- Username not found");
                    log.error("Invalid playerName {}", playerName);
                    break;
                }
                Player player = optionalPlayer.get();
                if (player.getChannel() == null) {
                    writeToChannel(channel, "LOGOUTFAIL - Player haven't login");
                    log.error("player {} haven't login", player.getPlayerName());
                    break;
                }
                if (player.getChannel() != channel) {
                    writeToChannel(channel, "LOGOUTFAIL - Invalid channel to logout");
                    log.error("invalid channel to logout");
                    break;
                }
                writeToChannel(channel, "LOGOUTSUCCESS");
                log.info("Player {} at channel {} logout success", player.getPlayerName(), player.getChannel());
                player.setChannel(null);
                break;
            }
            case SIGNUP: {
                if (!isRequestLengthValid(request)) {
                    writeToChannel(channel, "FAIL - Invalid SIGNUP request length");
                    throw new RequestException("Invalid request length");
                }
                String playerName = request.get(1);
                String password = request.get(2);
                if (playerService.isPlayerExists(playerName)) {
                    writeToChannel(channel, "SIGNUPFAIL - Username already exists");
                    log.error("playerName {} exists", playerName);
                    break;
                }
                Player newPlayer = new Player(playerName, password);
                playerService.saveNewPlayer(newPlayer);
                writeToChannel(channel, "SIGNUPSUCCESS");
                log.info("Sign up success with player {}", newPlayer);
                break;
            }
            case INFO: { // INFO {username} {bank} {money_earn} {Win} {Lose} {Push} {Bust} {Blackjack}
                if (!isRequestLengthValid(request)) {
                    writeToChannel(channel, "FAIL - Invalid INFO request length");
                    throw new RequestException("Invalid request length");
                }
                String playerName = request.get(1);
                Optional<Player> optionalPlayer = playerService.getPlayerByName(playerName);
                if (optionalPlayer.isEmpty()) {
                    writeToChannel(channel, "INFOFAIL - Player not found");
                    log.error("Invalid playerName {}", playerName);
                    break;
                }
                Player player = optionalPlayer.get();
                PlayerGameInfo playerGameInfo = matchHistoryService.getPlayerGameInfoByName(playerName);
                String msg = String.join(" ", Arrays.asList(
                        requestType.getValue(),
                        playerGameInfo.getPlayerName(),
                        String.valueOf(player.getBank()),
                        String.valueOf(playerGameInfo.getMoneyEarn()),
                        String.valueOf(playerGameInfo.getWin()),
                        String.valueOf(playerGameInfo.getLose()),
                        String.valueOf(playerGameInfo.getPush()),
                        String.valueOf(playerGameInfo.getBust()),
                        String.valueOf(playerGameInfo.getBlackjack())
                ));
                writeToChannel(channel, msg);
                log.info("Game Info of player {}: {}", playerName, playerGameInfo);
                break;
            }
            case RANKING: { // RANK [List of {ranking} {user_name} {money_gain/lose}]
                if (!isRequestLengthValid(request)) {
                    writeToChannel(channel, "FAIL - Invalid RANKING request length");
                    throw new RequestException("Invalid request length");
                }
                List<PlayerRanking> rankings = matchHistoryService.getAllPlayerRanking();
                String msg = "RANK [" +
                        rankings.stream().map(PlayerRanking::toString)
                                .collect(Collectors.joining(", ")) +
                        "]";
                writeToChannel(channel, msg);
                log.info("Get rankings {}", rankings);
                break;
            }
            case ADDMONEY: {
                if (!isRequestLengthValid(request)) {
                    writeToChannel(channel, "FAIL - Invalid ADD request length");
                    throw new RequestException("Invalid request length");
                }
                String playerName = request.get(1);
                String cardNumber = request.get(2);
                double amount = Double.parseDouble(request.get(3));

                try {
                    Player player = creditCardService.addMoney(playerName, cardNumber, amount);
                    writeToChannel(channel, "ADDSUCCESS" + player.getPlayerName() + " " + player.getBank());
                    log.info("Add money from card {} to player {} success. New balance: {} ",
                            cardNumber, playerName, player.getBank());
                } catch (PlayerException.PlayerNotFoundException e) {
                    writeToChannel(channel, "ADDFAIL - Player not found");
                    throw e;
                } catch (CreditCardException.CreditCardNotFoundException e) {
                    writeToChannel(channel, "ADDFAIL - credit card not found");
                    throw e;
                } catch (CreditCardException.NotEnoughBalanceException e) {
                    writeToChannel(channel, "ADDFAIL - Credit card balance not enough");
                    throw e;
                }
                break;
            }
            case WITHDRAWMONEY: {
                if (!isRequestLengthValid(request)) {
                    writeToChannel(channel, "FAIL - Invalid WDR request length");
                    throw new RequestException("Invalid request length");
                }
                String playerName = request.get(1);
                String creditCardId = request.get(2);
                double amount = Double.parseDouble(request.get(3));
                //TODO
                break;
            }
            default:
                writeToChannel(channel, "FAIL - Invalid request");
                throw new RequestException.InvalidRequestTypeException(request.get(0));
        }
    }

    private boolean isRequestLengthValid(List<String> request) throws RequestException {
        switch (RequestType.from(request.get(0))) {
            case RANKING:
                return request.size() == 1;
            case LOGOUT:
            case INFO:
                return request.size() == 2;
            case LOGIN:
            case SIGNUP:
                return request.size() == 3;
            case ADDMONEY:
            case WITHDRAWMONEY:
                return request.size() == 4;
            default:
                throw new RequestException.InvalidRequestLengthException();
        }
    }

    public void writeToChannel(SocketChannel channel, String msg) throws IOException {
        msg += "\n";
        channel.write(ByteBuffer.wrap(msg.getBytes()));
    }

}
