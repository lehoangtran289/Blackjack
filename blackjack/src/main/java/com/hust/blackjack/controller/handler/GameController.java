package com.hust.blackjack.controller.handler;

import com.hust.blackjack.common.tuple.Tuple2;
import com.hust.blackjack.exception.*;
import com.hust.blackjack.model.*;
import com.hust.blackjack.service.PlayerService;
import com.hust.blackjack.service.TableService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.stream.Collectors;

import static com.hust.blackjack.utils.MessageUtils.sleep;
import static com.hust.blackjack.utils.MessageUtils.writeToChannel;

@Log4j2
@Service
public class GameController implements IController {

    private final TableService tableService;
    private final PlayerService playerService;

    public GameController(TableService tableService, PlayerService playerService) {
        this.tableService = tableService;
        this.playerService = playerService;
    }

    private boolean isValidChannelToProcess(String username, SocketChannel channel) throws PlayerException {
        return playerService.getPlayerByName(username).getChannel() == channel;
    }

    @Override
    public void processRequest(SocketChannel channel, RequestType requestType, List<String> request)
            throws RequestException, IOException, LoginException, PlayerException, TableException {
        switch (requestType) {
            case CREATEROOM: {
                String playerName = request.get(1);
                String password = request.get(2);
                if (!isValidChannelToProcess(playerName, channel)) {
                    log.error("Invalid channel to process");
                    writeToChannel(channel, "FAIL=invalid channel to process");
                    new InvalidChannelException().printStackTrace();
                    break;
                }
                try {
                    Table table = tableService.createRoom(playerName, password);
                    // build response string and send to each players in table
                    String msg = "SUCCESS=" + table.getTableId() + " " + playerName;
                    writeToChannel(channel, msg);

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
            case PLAY: {
                String playerName = request.get(1);
                String tableId = request.size() > 2 ? request.get(2) : "";
                String password = request.size() > 3 ?
                        request.stream().skip(3).collect(Collectors.joining(" ")) : "";
                if (!isValidChannelToProcess(playerName, channel)) {
                    log.error("Invalid channel to process");
                    writeToChannel(channel, "FAIL=invalid channel to process");
                    new InvalidChannelException().printStackTrace();
                    break;
                }
                try {
                    Table table;
                    if (StringUtils.isEmpty(tableId)) {
                        table = tableService.randomPlay(playerName);
                    } else {
                        table = StringUtils.isEmpty(password) ?
                                tableService.enterRoomPlay(playerName, tableId) :
                                tableService.enterRoomPlay(playerName, tableId, password);
                    }

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
                } catch (TableException.PasswordRequireException e) {
                    writeToChannel(channel, "PASSWORD_REQUIRE");
                    throw e;
                } catch (TableException.InvalidPasswordException e) {
                    writeToChannel(channel, "FAIL=Wrong password");
                    throw e;
                } catch (TableException e) {
                    writeToChannel(channel, "FAIL=Room not require password");
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
                if (!isValidChannelToProcess(playerName, channel)) {
                    log.error("Invalid channel to process");
                    writeToChannel(channel, "FAIL=invalid channel to process");
                    new InvalidChannelException().printStackTrace();
                    break;
                }
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
                if (!isValidChannelToProcess(playerName, channel)) {
                    log.error("Invalid channel to process");
                    writeToChannel(channel, "FAIL=invalid channel to process");
                    new InvalidChannelException().printStackTrace();
                    break;
                }
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
                if (!isValidChannelToProcess(playerName, channel)) {
                    log.error("Invalid channel to process");
                    writeToChannel(channel, "FAIL=invalid channel to process");
                    new InvalidChannelException().printStackTrace();
                    break;
                }
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
                if (!isValidChannelToProcess(player.getPlayerName(), channel)) {
                    log.error("Invalid channel to process");
                    writeToChannel(channel, "FAIL=invalid channel to process");
                    new InvalidChannelException().printStackTrace();
                    break;
                }
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
                if (!isValidChannelToProcess(player.getPlayerName(), channel)) {
                    log.error("Invalid channel to process");
                    writeToChannel(channel, "FAIL=invalid channel to process");
                    new InvalidChannelException().printStackTrace();
                    break;
                }
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
                if (!isValidChannelToProcess(playerName, channel)) {
                    log.error("Invalid channel to process");
                    writeToChannel(channel, "FAIL=invalid channel to process");
                    new InvalidChannelException().printStackTrace();
                    break;
                }
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
                if (!isValidChannelToProcess(player.getPlayerName(), channel)) {
                    log.error("Invalid channel to process");
                    writeToChannel(channel, "FAIL=invalid channel to process");
                    new InvalidChannelException().printStackTrace();
                    break;
                }

                if (player.getBalance() < Table.MINIMUM_BET) {
                    log.error("Invalid balance of player {} to start game, bl = {}"
                            , player.getPlayerName(), player.getBalance());
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
}
