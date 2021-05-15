package com.hust.blackjack.service;

import com.hust.blackjack.exception.CreditCardException;
import com.hust.blackjack.exception.LoginException;
import com.hust.blackjack.exception.PlayerException;
import com.hust.blackjack.exception.RequestException;
import com.hust.blackjack.model.CreditCard;
import com.hust.blackjack.model.Player;
import com.hust.blackjack.model.RequestType;
import com.hust.blackjack.model.dto.PlayerGameInfo;
import com.hust.blackjack.model.dto.PlayerRanking;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    public void process(SocketChannel channel, String requestMsg)
            throws RequestException, IOException, LoginException, PlayerException, CreditCardException {
        List<String> request = new ArrayList<>(Arrays.asList(requestMsg.split(" ")));
        if (request.isEmpty()) {
            writeToChannel(channel, "FAIL=Invalid request");
            throw new RequestException("Invalid request, request empty");
        }

        RequestType requestType;
        try {
            requestType = RequestType.from(request.get(0));
            log.info("Receive {} request", requestType.getValue());
        } catch (RequestException ex) {
            writeToChannel(channel, "FAIL=Invalid request");
            throw new RequestException("Invalid request type");
        }

        switch (requestType) {
            case LOGIN: {
                if (!isRequestLengthValid(request)) {
                    writeToChannel(channel, "FAIL=Invalid LOGIN request length");
                    throw new RequestException("Invalid request length");
                }
                if (playerService.isChannelLoggedIn(channel)) {
                    writeToChannel(channel, "LOGINFAIL=Channel already login, logout first");
                    log.error("channel {} already login", channel);
                    throw new LoginException("channel already login");
                }
                String playerName = request.get(1);
                String password = request.get(2);
                try {
                    Player player = playerService.login(playerName, password);
                    player.setChannel(channel);
                    writeToChannel(channel, "LOGINSUCCESS=" + player.getPlayerName() + " " + player.getBank());
                    log.info("Player {} login success at channel {}", player.getPlayerName(), player.getChannel());
                } catch (PlayerException.PlayerNotFoundException e) {
                    writeToChannel(channel, "LOGINFAIL=Username or password incorrect");
                    throw e;
                } catch (LoginException.PlayerAlreadyLoginException e) {
                    writeToChannel(channel, "LOGINFAIL=Player already login");
                    throw e;
                }
                break;
            }
            case LOGOUT: {
                if (!isRequestLengthValid(request)) {
                    writeToChannel(channel, "FAIL=Invalid LOGOUT request length");
                    throw new RequestException("Invalid request length");
                }
                String playerName = request.get(1);
                try {
                    Player player = playerService.logout(channel, playerName);
                    writeToChannel(channel, "LOGOUTSUCCESS");
                    log.info("Player {} at channel {} logout success",
                            player.getPlayerName(), channel
                    );
                } catch (PlayerException.PlayerNotFoundException e) {
                    writeToChannel(channel, "LOGOUTFAIL=Username not found");
                    throw e;
                } catch (LoginException.PlayerNotLoginException e) {
                    writeToChannel(channel, "LOGOUTFAIL=Player haven't login");
                    throw e;
                } catch (LoginException.InvalidChannelToLogoutException e) {
                    writeToChannel(channel, "LOGOUTFAIL=Invalid channel to logout");
                    throw e;
                }
                break;
            }
            case SIGNUP: {
                if (!isRequestLengthValid(request)) {
                    writeToChannel(channel, "FAIL=Invalid SIGNUP request length");
                    throw new RequestException("Invalid request length");
                }
                String playerName = request.get(1);
                String password = request.get(2);
                try {
                    Player newPlayer = playerService.saveNewPlayer(playerName, password);
                    writeToChannel(channel, "SIGNUPSUCCESS");
                    log.info("Sign up success with player {}", newPlayer);
                } catch (PlayerException.PlayerAlreadyExistsException e) {
                    writeToChannel(channel, "SIGNUPFAIL=Username already exists");
                    throw e;
                }
                break;
            }
            case INFO: { // INFO {username} {bank} {money_earn} {Win} {Lose} {Push} {Bust} {Blackjack}
                if (!isRequestLengthValid(request)) {
                    writeToChannel(channel, "FAIL=Invalid INFO request length");
                    throw new RequestException("Invalid request length");
                }
                String playerName = request.get(1);
                try {
                    PlayerGameInfo playerGameInfo = matchHistoryService.getPlayerGameInfoByName(playerName);
                    String msg = requestType.getValue() + "=" +
                            String.join(" ", Arrays.asList(
                                    playerGameInfo.getPlayer().getPlayerName(),
                                    String.valueOf(playerGameInfo.getPlayer().getBank()),
                                    String.valueOf(playerGameInfo.getMoneyEarn()),
                                    String.valueOf(playerGameInfo.getWin()),
                                    String.valueOf(playerGameInfo.getLose()),
                                    String.valueOf(playerGameInfo.getPush()),
                                    String.valueOf(playerGameInfo.getBust()),
                                    String.valueOf(playerGameInfo.getBlackjack())
                            ));
                    writeToChannel(channel, msg);
                    log.info("Game Info of player {}: {}", playerName, playerGameInfo);
                } catch (PlayerException.PlayerNotFoundException e) {
                    writeToChannel(channel, "INFOFAIL=Player not found");
                    throw e;
                }
                break;
            }
            case RANKING: { // RANK={ranking1} {user_name1} {money_earn1}, ...
                if (!isRequestLengthValid(request)) {
                    writeToChannel(channel, "FAIL=Invalid RANKING request length");
                    throw new RequestException("Invalid request length");
                }
                String playerName = request.get(1);
                List<PlayerRanking> rankings = matchHistoryService.getAllPlayerRanking(playerName);
                String msg = "RANK=" + rankings.stream().map(PlayerRanking::toString)
                        .collect(Collectors.joining(","));
                writeToChannel(channel, msg);
                log.info("Get rankings {}", rankings);
                break;
            }
            case ADDMONEY: {
                if (!isRequestLengthValid(request)) {
                    writeToChannel(channel, "FAIL=Invalid ADD request length");
                    throw new RequestException("Invalid request length");
                }
                String playerName = request.get(1);
                String cardNumber = request.get(2);
                double amount = Double.parseDouble(request.get(3));
                try {
                    Player player = creditCardService.manageCreditCard(
                            CreditCard.Action.ADD, playerName, cardNumber, amount
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
                if (!isRequestLengthValid(request)) {
                    writeToChannel(channel, "FAIL=Invalid WDR request length");
                    throw new RequestException("Invalid request length");
                }
                String playerName = request.get(1);
                String cardNumber = request.get(2);
                double amount = Double.parseDouble(request.get(3));
                try {
                    Player player = creditCardService.manageCreditCard(
                            CreditCard.Action.WITHDRAW, playerName, cardNumber, amount
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

    private boolean isRequestLengthValid(List<String> request) throws RequestException {
        switch (RequestType.from(request.get(0))) {
            case RANKING:
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
//        msg += "\n"; // terminal testing purposes
        log.info("Response to channel {}: {}", channel.getRemoteAddress(), msg);
        channel.write(ByteBuffer.wrap(msg.getBytes()));
    }

}
