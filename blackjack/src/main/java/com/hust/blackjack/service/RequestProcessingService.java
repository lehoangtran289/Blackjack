package com.hust.blackjack.service;

import com.hust.blackjack.common.tuple.Tuple2;
import com.hust.blackjack.exception.*;
import com.hust.blackjack.model.*;
import com.hust.blackjack.model.dto.PlayerGameInfo;
import com.hust.blackjack.model.dto.PlayerRanking;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
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

    // TODO: handle each request-type separately (Front Controller Pattern)
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
            // loginController
            case LOGIN: {
                if (playerService.isChannelLoggedIn(channel)) {
                    writeToChannel(channel, "LOGINFAIL=Channel already login, logout first");
                    log.error("channel {} already login", channel);
                    throw new LoginException("channel already login");
                }
                String playerName = request.get(1);
                String password = request.get(2);
                try {
                    Player player = playerService.login(playerName, password, channel);
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

            // searchController
            case SEARCHINFO: {
                if (request.size() == 1) {
                    log.error("searchinfo playername empty");
                    writeToChannel(channel, "SEARCHFAIL");
                    throw new PlayerException.PlayerNotFoundException();
                }
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

            // creditCardController
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

            // gameController
            case PLAY: {
                String playerName = request.get(1);
                String tableId = request.size() >= 3 ?
                        request.stream().skip(2).collect(Collectors.joining(" ")) :
                        "";

                try {
                    Table table = StringUtils.isEmpty(tableId) ?
                            tableService.play(playerName) :                 // play random
                            tableService.playroom(playerName, tableId);     // play enter room

                    List<Player> playersInTable = table.getPlayers();
                    // build response string and send to each players in table
                    String msg = "SUCCESS=" + table.getTableId() + " " +
                            playersInTable.stream()
                                    .map(Player::getPlayerName)
                                    .collect(Collectors.joining(" "));
                    for (Player player : playersInTable) {
                        writeToChannel(player.getChannel(), msg);
                    }
                    sleep(1001);

                    if (playersInTable.size() == Table.TABLE_SIZE && table.isAllReady()) {
                        Table t = tableService.start(table);
                        for (Player player : t.getPlayers()) {
                            writeToChannel(player.getChannel(), "START=" + t.getTableId());
                        }
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
                } catch (TableException.PlayerInAnotherTableException e) {
                    writeToChannel(channel, "FAIL=Player in another table");
                    throw e;
                } catch (TableException.TableNotFoundException e) {
                    writeToChannel(channel, "FAIL=Table not found");
                    throw e;
                }
                break;
            }
            case CHAT: {
                String tableId = request.get(1);
                String playerName = request.get(2);
                String msgRcv = request.stream()
                        .skip(3)
                        .collect(Collectors.joining(" "));
                String msgSend = "CHAT=" + playerName + " " + msgRcv;
                try {
                    Table table = tableService.getTableById(tableId);
                    for (Player player : table.getPlayers()) {
                        writeToChannel(player.getChannel(), msgSend);
                    }
                } catch (TableException.TableNotFoundException ex) {
                    log.error("Table {} Not found", tableId);
                    throw ex;
                }
                break;
            }
            case BET: {
                String tableId = request.get(1);
                String playerName = request.get(2);
                double bet = Double.parseDouble(request.get(3));

                try {
                    Player player = tableService.getBet(tableId, playerName, bet);
                    Table table = tableService.getTableById(tableId);

                    // send BET response to players in room
                    String msgSend = "BET=" + tableId + " " + playerName + " " + player.getBet();
                    for (Player p : table.getPlayers()) {
                        writeToChannel(p.getChannel(), msgSend);
                    }

                    // if all players bet -> DEAL + TURN
                    if (table.isAllBet()) {
                        Tuple2<Hand, List<Player>> tuple = tableService.dealCards(table);
                        Hand dealerHand = tuple.getA0();
                        List<Player> players = tuple.getA1();

                        // build response message
                        StringBuilder msgBuilder = new StringBuilder("DEAL=");
                        List<Card> dealerCards = dealerHand.getCards();
                        for (int i = 0; i < dealerCards.size(); i++) {
                            Card card = dealerCards.get(i);   // dealer's hand
                            msgBuilder.append(card.rank().getValue())
                                    .append(" ")
                                    .append(card.getSuit().getIntVal());
                            if (i == dealerHand.size() - 1) {
                                msgBuilder.append(",");
                            } else {
                                msgBuilder.append(" ");
                            }
                        }

                        for (Player p : players) {  // players' hand
                            msgBuilder.append(p.getPlayerName()).append(" ");
                            List<Card> playerCards = p.getHand().getCards();
                            for (int i = 0; i < playerCards.size(); ++i) {
                                Card card = playerCards.get(i);
                                msgBuilder.append(card.rank().getValue())
                                        .append(" ")
                                        .append(card.getSuit().getIntVal());
                                if (i == playerCards.size() - 1) {
                                    msgBuilder.append(",");
                                } else {
                                    msgBuilder.append(" ");
                                }
                            }
                        }
                        String dealMsg = msgBuilder.substring(0, msgBuilder.toString().length() - 1); // rm
                        // last ','
                        log.info("DEAL msg: {}", dealMsg);

                        // send DEAL
                        sleep(1000);
                        for (Player p : table.getPlayers()) {
                            writeToChannel(p.getChannel(), dealMsg);
                        }

                        // send TURN
                        sleep(1000);
                        Player firstPlayer = players.get(0);
                        table.setPlayerTurn(firstPlayer.getPlayerName());
                        String turnMsg =
                                "TURN=" + firstPlayer.getPlayerName() + " " + firstPlayer.getIsBlackjack();
                        log.info("TURN msg: {}", turnMsg);
                        for (Player p : table.getPlayers()) {
                            writeToChannel(p.getChannel(), turnMsg);
                        }
                    }
                } catch (TableException.TableNotFoundException ex) {
                    log.error("Table {} Not found", tableId);
                    throw ex;
                } catch (TableException.NotEnoughBankBalanceException ex) {
                    writeToChannel(channel, "BETFAIL=Invalid bet from player " + playerName);
                    throw ex;
                }
                break;
            }
            case BETQUIT: {
                String tableId = request.get(1);
                String playerName = request.get(2);
                try {
                    Table table = tableService.removePlayerInBetPhase(tableId, playerName);
                    // write to requested channel
                    writeToChannel(channel, "QUIT");

                    String msg = "QUIT=" + table.getTableId() + " " + playerName;
                    // write to players in current table
                    for (Player player : table.getPlayers()) {
                        writeToChannel(player.getChannel(), msg);
                    }

                    // if all players bet -> DEAL + TURN
                    if (table.isAllBet()) {
                        Tuple2<Hand, List<Player>> tuple = tableService.dealCards(table);
                        Hand dealerHand = tuple.getA0();
                        List<Player> players = tuple.getA1();

                        // build response message
                        StringBuilder msgBuilder = new StringBuilder("DEAL=");
                        List<Card> dealerCards = dealerHand.getCards();
                        for (int i = 0; i < dealerCards.size(); i++) {
                            Card card = dealerCards.get(i);   // dealer's hand
                            msgBuilder.append(card.rank().getValue())
                                    .append(" ")
                                    .append(card.getSuit().getIntVal());
                            if (i == dealerHand.size() - 1) {
                                msgBuilder.append(",");
                            } else {
                                msgBuilder.append(" ");
                            }
                        }

                        for (Player p : players) {  // players' hand
                            msgBuilder.append(p.getPlayerName()).append(" ");
                            List<Card> playerCards = p.getHand().getCards();
                            for (int i = 0; i < playerCards.size(); ++i) {
                                Card card = playerCards.get(i);
                                msgBuilder.append(card.rank().getValue())
                                        .append(" ")
                                        .append(card.getSuit().getIntVal());
                                if (i == playerCards.size() - 1) {
                                    msgBuilder.append(",");
                                } else {
                                    msgBuilder.append(" ");
                                }
                            }
                        }
                        String dealMsg = msgBuilder.substring(0, msgBuilder.toString().length() - 1); // rm
                        // last ','
                        log.info("DEAL msg: {}", dealMsg);

                        // send DEAL
                        sleep(1000);
                        for (Player p : table.getPlayers()) {
                            writeToChannel(p.getChannel(), dealMsg);
                        }

                        // send TURN
                        sleep(1000);
                        Player firstPlayer = players.get(0);
                        table.setPlayerTurn(firstPlayer.getPlayerName());
                        String turnMsg =
                                "TURN=" + firstPlayer.getPlayerName() + " " + firstPlayer.getIsBlackjack();
                        log.info("TURN msg: {}", turnMsg);
                        for (Player p : table.getPlayers()) {
                            writeToChannel(p.getChannel(), turnMsg);
                        }
                    }
                } catch (PlayerException.PlayerNotFoundException e) {
                    writeToChannel(channel, "FAIL=Player not found");
                    throw e;
                } catch (TableException.TableNotFoundException e) {
                    writeToChannel(channel, "FAIL=Table not found");
                    throw e;
                } catch (PlayerException.PlayerNotInAnyTableException e) {
                    writeToChannel(channel, "FAIL=Player not in any table");
                    throw e;
                } catch (TableException.PlayerNotFoundInTableException e) {
                    writeToChannel(channel, "FAIL=table not contain request player");
                    throw e;
                }
                break;
            }
            case HIT: {
                Table table = tableService.getTableById(request.get(1));
                Player player = playerService.getPlayerByName(request.get(2));

                try {
                    String msg = tableService.processHit(table, player);    // HIT BUST BLACKJACK

                    // response to client
                    for (Player p : table.getPlayers()) {
                        writeToChannel(p.getChannel(), msg);
                    }
                } catch (TableException.TableNotFoundException ex) {
                    log.error("Table {} Not found", table.getTableId());
                    throw ex;
                }
                break;
            }
            case STAND: {
                Table table = tableService.getTableById(request.get(1));
                List<Player> playersInTable = table.getPlayers();
                Player player = playerService.getPlayerByName(request.get(2));

                try {
                    sleep(1000);
                    String processedMsg = tableService.processStand(table, player);     // CHECK or TURN
                    String standMsg = "STAND=" + player.getPlayerName();
                    for (Player p : playersInTable) {
                        writeToChannel(p.getChannel(), standMsg);
                    }
                    sleep(1000);
                    for (Player p : playersInTable) {
                        writeToChannel(p.getChannel(), processedMsg);
                    }
                } catch (TableException.TableNotFoundException ex) {
                    log.error("Table {} Not found", table.getTableId());
                    throw ex;
                }
                break;
            }
            case QUIT: {
                String tableId = request.get(1);
                String playerName = request.get(2);
                try {
                    Tuple2<Table, String> tup = tableService.removePlayer(tableId, playerName);
                    Table table = tup.getA0();
                    if (!StringUtils.isEmpty(tup.getA1())) {    // in case current turn's player quit
                        for (Player p : table.getPlayers()) {
                            writeToChannel(p.getChannel(), tup.getA1());
                        }
                    }
                    sleep(1000);
                    String msg = "QUIT=" + table.getTableId() + " " + playerName;
                    // write to requested channel
                    writeToChannel(channel, "QUIT");

                    // write to players in current table
                    for (Player player : table.getPlayers()) {
                        writeToChannel(player.getChannel(), msg);
                    }
                } catch (PlayerException.PlayerNotFoundException e) {
                    writeToChannel(channel, "FAIL=Player not found");
                    throw e;
                } catch (TableException.TableNotFoundException e) {
                    writeToChannel(channel, "FAIL=Table not found");
                    throw e;
                } catch (PlayerException.PlayerNotInAnyTableException e) {
                    writeToChannel(channel, "FAIL=Player not in any table");
                    throw e;
                } catch (TableException.PlayerNotFoundInTableException e) {
                    writeToChannel(channel, "FAIL=table not contain request player");
                    throw e;
                }
                break;
            }
            case CONTINUE: {
                Table table = tableService.getTableById(request.get(1));
                Player player = playerService.getPlayerByName(request.get(2));
                player.setIsReady(1);

                if (player.getBank() < Table.MINIMUM_BET) {
                    log.error("Invalid balance of player {} to start game, bl = {}"
                            , player.getPlayerName(), player.getBank());
                    tableService.removePlayer(table.getTableId(), player.getPlayerName());
                    writeToChannel(channel, "FAIL=Balance not enough");
                    throw new TableException.NotEnoughBankBalanceException();
                }

                List<Player> playersInTable = table.getPlayers();

                // build response string and send to each players in table
                String msg = "SUCCESS=" + table.getTableId() + " " +
                        playersInTable.stream()
                                .map(Player::getPlayerName)
                                .collect(Collectors.joining(" "));
                for (Player p : playersInTable) {
                    writeToChannel(p.getChannel(), msg);
                }
                sleep(1000);

                // continue all
                if (playersInTable.size() == Table.TABLE_SIZE && table.isAllReady()) {
                    table.refreshAndInitDeck();
                    for (Player p : playersInTable) {
                        writeToChannel(p.getChannel(), "START=" + table.getTableId());
                    }
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
            case CHAT:
                return request.size() > 3;
            case SEARCHINFO:
                return request.size() == 2 || request.size() == 1;
            case PLAY:
                return request.size() >= 2;
            case RANKING:
            case LOGOUT:
            case INFO:
            case HISTORY:
                return request.size() == 2;
            case LOGIN:
            case SIGNUP:
            case HIT:
            case STAND:
            case QUIT:
            case CONTINUE:
            case BETQUIT:
                return request.size() == 3;
            case ADDMONEY:
            case WITHDRAWMONEY:
            case BET:
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

    public void processChannelClose(SocketChannel client) {
        List<Player> players = playerService.getAllPlayers();
        for (Player player: players) {
            if (player.getChannel() == client) {
                player.logout();
                return;
            }
        }
    }

    private void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
