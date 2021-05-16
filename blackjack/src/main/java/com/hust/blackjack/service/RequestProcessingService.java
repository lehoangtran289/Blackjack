package com.hust.blackjack.service;

import com.hust.blackjack.exception.*;
import com.hust.blackjack.model.*;
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
    private final TableService tableService;

    public RequestProcessingService(CreditCardService creditCardService,
                                    PlayerService playerService,
                                    MatchHistoryService matchHistoryService,
                                    TableService tableService) {
        this.creditCardService = creditCardService;
        this.playerService = playerService;
        this.matchHistoryService = matchHistoryService;
        this.tableService = tableService;
    }

    // TODO: handle each request-type separately
    public void process(SocketChannel channel, String requestMsg)
            throws RequestException, IOException, LoginException, PlayerException, CreditCardException,
            TableException {
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

        // process request
        switch (requestType) {
            case LOGIN: {
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
                    log.info("Player {} login success at channel {}", player.getPlayerName(),
                            player.getChannel());
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
            case SEARCHINFO: {
                String playerName = request.get(1);
                try {
                    List<PlayerGameInfo> playerGameInfos =
                            matchHistoryService.searchPlayerGameInfoByName(playerName);
                    String msg = "SEARCHSUCCESS=" + playerGameInfos.stream()
                            .map(playerGameInfo -> String.join(" ", Arrays.asList(
                                    playerGameInfo.getPlayer().getPlayerName(),
                                    String.valueOf(playerGameInfo.getPlayer().getBank()),
                                    String.valueOf(playerGameInfo.getMoneyEarn()),
                                    String.valueOf(playerGameInfo.getWin()),
                                    String.valueOf(playerGameInfo.getLose()),
                                    String.valueOf(playerGameInfo.getPush()),
                                    String.valueOf(playerGameInfo.getBust()),
                                    String.valueOf(playerGameInfo.getBlackjack())
                                    )
                            ))
                            .collect(Collectors.joining(","));
                    writeToChannel(channel, msg);
                    log.info("Game Info of player {}: {}", playerName, playerGameInfos);
                } catch (PlayerException.PlayerNotFoundException e) {
                    writeToChannel(channel, "SEARCHFAIL");
                    throw e;
                }
                break;
            }
            case INFO: { // INFO {username} {bank} {money_earn} {Win} {Lose} {Push} {Bust} {Blackjack}
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
            case HISTORY: {
                String playerName = request.get(1);
                List<MatchHistory> histories = matchHistoryService.getPlayerHistory(playerName);
                String msg = "HISTORY=" + histories.stream().map(MatchHistory::toString)
                        .collect(Collectors.joining(","));
                writeToChannel(channel, msg);
                break;
            }
            case RANKING: { // RANK={ranking1} {user_name1} {money_earn1}, ...
                String playerName = request.get(1);
                List<PlayerRanking> rankings = matchHistoryService.getAllPlayerRanking(playerName);
                String msg = "RANK=" + rankings.stream().map(PlayerRanking::toString)
                        .collect(Collectors.joining(","));
                writeToChannel(channel, msg);
                log.info("Get rankings {}", rankings);
                break;
            }
            case ADDMONEY: {
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

            case PLAY: {
                String playerName = request.get(1);
                try {
                    Table table = tableService.play(playerName);
                    List<Player> playersInTable = table.getPlayers();

                    // build response string and send to each players in table
                    String msg = "SUCCESS=" + table.getTableId() + " ";
                    StringBuilder players = new StringBuilder();
                    for (int i = 0; i < playersInTable.size() - 1; i++) {
                        players.append(playersInTable.get(i).getPlayerName());
                        if (i != playersInTable.size() - 2) {
                            players.append(" ");
                        }
                    }
                    msg += players.toString();

                    for (Player player : playersInTable) {
                        writeToChannel(player.getChannel(), msg);
                    }
                    log.info("Player {} join table {}. Players {}",
                            playerName, table.getTableId(), table.getPlayers()
                    );
                } catch (TableException.NotEnoughBankBalanceException e) {
                    writeToChannel(channel, "FAIL=Balance not enough");
                    throw e;
                } catch (PlayerException.PlayerNotFoundException e) {
                    writeToChannel(channel, "FAIL=Player not found");
                    throw e;
                } catch (LoginException.PlayerNotLoginException e) {
                    writeToChannel(channel, "FAIL=Player not login");
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
            case SEARCHINFO:
            case INFO:
            case HISTORY:
            case PLAY:
                return request.size() == 2;
            case LOGIN:
            case SIGNUP:
                return request.size() == 3;
            case ADDMONEY:
            case WITHDRAWMONEY:
            case CHAT:
            case BET:
                return request.size() == 4;
            default:
                throw new RequestException.InvalidRequestLengthException();
        }
    }

    public void writeToChannel(SocketChannel channel, String msg) throws IOException {
        msg += "\n"; // terminal testing purposes
        log.info("Response to channel {}: {}", channel.getRemoteAddress(), msg);
        channel.write(ByteBuffer.wrap(msg.getBytes()));
    }

}
