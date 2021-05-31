package com.hust.blackjack.service;

import com.hust.blackjack.common.tuple.Tuple2;
import com.hust.blackjack.exception.LoginException;
import com.hust.blackjack.exception.PlayerException;
import com.hust.blackjack.exception.TableException;
import com.hust.blackjack.model.*;
import com.hust.blackjack.repository.TableRepository;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Log4j2
@Service
public class TableService {
    private final TableRepository tableRepository;
    private final PlayerService playerService;
    private final MatchHistoryService matchHistoryService;

    public TableService(TableRepository tableRepository, PlayerService playerService,
                        MatchHistoryService matchHistoryService) {
        this.tableRepository = tableRepository;
        this.playerService = playerService;
        this.matchHistoryService = matchHistoryService;
    }

    public Table getTableById(String tableId) throws TableException {
        Optional<Table> optionalPlayer = tableRepository.findTableById(tableId);
        if (optionalPlayer.isEmpty()) {
            log.error("Invalid tableId {}", tableId);
            throw new TableException.TableNotFoundException();
        }
        return optionalPlayer.get();
    }

    public Table play(String playerName) throws TableException, PlayerException, LoginException {
        // get user
        Player player = playerService.getPlayerByName(playerName);
        if (player.getChannel() == null) {
            log.error("Player {} not login", playerName);
            throw new LoginException.PlayerNotLoginException();
        }
        if (player.getTableId() != null) {
            log.error("Player {} in another table, id = {}", playerName, player.getTableId());
            throw new TableException.PlayerInAnotherTableException();
        }
        if (player.getBank() < Table.MINIMUM_BET) {
            log.error("Invalid balance of player {} to start game, bl = {}", playerName, player.getBank());
            throw new TableException.NotEnoughBankBalanceException();
        }

        // get available table, create new if all table full
        Table table = tableRepository.findAvailableTable();

        // place player into table
        player.refresh();
        player.setIsReady(1);
        player.setTableId(table.getTableId());
        table.getPlayers().add(player);

        return table;
    }

    public Tuple2<Table, String> removePlayer(String tableId, String playerName) throws PlayerException, TableException {
        Player player = playerService.getPlayerByName(playerName);
        Table table = this.getTableById(tableId);

        if (player.getTableId() == null) {
            log.error("Player {} not in any table", player);
            throw new PlayerException.PlayerNotInAnyTableException("Player " + playerName + " not in any " +
                    "table");
        }
        if (!table.getPlayers().contains(player)) {
            log.error("Table {} not contain player {}", tableId, player);
            throw new TableException.PlayerNotFoundInTableException("Table not contain player");
        }

        // process QUIT
        String turnMsg = "";
        table.getPlayers().remove(player);

        // in case current turn's player quit
        if (StringUtils.equals(table.getPlayerTurn(), playerName) && table.getIsPlaying() == 1) {
            Player nextPlayer = table.getNextTurn();
            if (nextPlayer != null) {
                table.setPlayerTurn(nextPlayer.getPlayerName());
                turnMsg = "TURN=" + nextPlayer.getPlayerName() + " " + nextPlayer.getIsBlackjack();
            }
        }
        player.refresh();
        player.setTableId(null);
        log.info("Player {} is removed from the table {}", player, table);

        return new Tuple2<>(table, turnMsg);
    }

    public Player getBet(String tableId, String playerName, double bet) throws PlayerException, TableException {
        Table table = this.getTableById(tableId);
        Player player = playerService.getPlayerByName(playerName);

        if (player.getTableId() == null) {
            log.error("Player {} not in any room", player);
            throw new TableException.PlayerNotFoundInTableException();
        }
        if (!table.getPlayers().contains(player)) {
            log.error("Table {} not contain player {}", tableId, player);
            throw new TableException.PlayerNotFoundInTableException("Table not contain player");
        }
        if (table.getIsPlaying() == 0) {
            log.error("Game not START in table {}", table);
            throw new TableException.GameNotStartException();
        }
        if (player.getBet() != 0) {
            log.error("Player {} already bet {}", playerName, player.getBet());
            throw new TableException("Player already bet");
        }
        if (player.getBank() < bet) {
            log.error("Player {} invalid bet={}, bank = {}", playerName, bet, player.getBank());
            throw new TableException.NotEnoughBankBalanceException();
        }
        if (bet < Table.MINIMUM_BET) {
            log.error("Invalid bet of player {}, bet = {}", playerName, player.getBet());
            throw new TableException.InvalidBet();
        }
        player.placeBet(bet);
        return player;
    }

    public Tuple2<Hand, List<Player>> dealCards(Table table) {
        // deal 2 cards to players
        List<Player> players = table.getPlayers();
        for (Player p : players) {
            p.setHand(new Hand());
            for (int i = 0; i < 2; ++i) {
                Card card = table.getDeck().dealCard();
                p.getHand().addCard(card);
            }
            if (p.getHand().isBlackJack()) {    // if got blackjack
                p.setIsBlackjack(1);
            }
        }
        // deal 2 cards to dealer
        Hand dealerHand = new Hand();
        for (int i = 0; i < 2; ++i) {
            Card card = table.getDeck().dealCard();
            dealerHand.addCard(card);
        }
        table.setDealerHand(dealerHand);
        return new Tuple2<>(dealerHand, players);
    }

    public String processHit(Table table, Player player) throws TableException {
        if (!StringUtils.equals(table.getPlayerTurn(), player.getPlayerName())) {
            log.error("Not player {} turn in table {}", player.getPlayerName(), table);
            throw new TableException.WrongTurnException();
        }

        // hit new card
        Card newCard = table.getDeck().dealCard();
        log.info("New card hit = {}", newCard);
        player.getHand().addCard(newCard);
        System.out.println(player.getHand() + " " + player.getHand().value());

        // check hand and return
        // in case of BLACKJACK
        if (player.getHand().isBlackJack()) {
            player.setIsBlackjack(1);
            log.info("Player {} in table {} has Blackjack. hand = {}", player.getHand(), table.getTableId(),
                    player.getHand());
            return "BLACKJACK=" + player.getPlayerName() + " " + newCard.getRank().getValue() + " " + newCard.getSuit().getIntVal();
        }
        // in case of BUST
        if (player.getHand().isBust()) {
            player.setIsBust(1);
            log.info("Player {} in table {} is BUST. total = {}", player.getPlayerName(), table.getTableId(),
                    player.getHand().value());
            return "BUST=" + player.getPlayerName() + " " + newCard.getRank().getValue() + " " + newCard.getSuit().getIntVal();
        }
        // normal HIT case
        log.info("Player {} in table {} hit a card {}", player.getPlayerName(), table.getTableId(), newCard);
        return "HIT=" + player.getPlayerName() + " " + newCard.getRank().getValue() + " " + newCard.getSuit().getIntVal();
    }

    public String processStand(Table table, Player player)
            throws TableException {
        if (!StringUtils.equals(table.getPlayerTurn(), player.getPlayerName())) {
            log.error("Not player {} turn in table {}", player.getPlayerName(), table);
            throw new TableException.WrongTurnException();
        }
        player.setIsStand(1);

        // return CHECK or TURN message based on table.isAllStand
        String msg;
        Player p = table.getNextTurn();
        if (p != null) {
            table.setPlayerTurn(p.getPlayerName());
            msg = "TURN=" + p.getPlayerName() + " " + p.getIsBlackjack();
        } else {
            msg = processCheck(table);
        }
        return msg;
    }

    private String processCheck(Table table) {
        StringBuilder msgBuilder = new StringBuilder("CHECK=");

        // process HIT of dealer
        Hand dealerHand = table.getDealerHand();
        while (dealerHand.value() < Table.DEALER_HIT_THRESHOLD) {
            Card newCard = table.getDeck().dealCard();
            dealerHand.getCards().add(newCard);
        }
        // build message of dealer
        List<Card> cards = dealerHand.getCards();
        for (int i = 0; i < cards.size(); i++) {
            Card c = cards.get(i);
            msgBuilder.append(c.getRank().getValue()).append(" ")
                    .append(c.getSuit().getIntVal());
            if (i == cards.size() - 1) {
                msgBuilder.append(",");
            } else {
                msgBuilder.append(" ");
            }
        }
        // process CHECK for players
        for (int i = 0; i < table.getPlayers().size(); i++) {
            Player p = table.getPlayers().get(i);

            // get player final state
            ResultState state = p.checkPlayerFinalState(dealerHand.value());

            // process Bet after a game
            double gain = 0;
            if (state == ResultState.BLACKJACK) {
                p.setBank(p.getBank() + p.getBet() * (1 + Table.BLACKJACK_RATE));
                gain = p.getBet() * Table.BLACKJACK_RATE;
            } else if (state == ResultState.WIN) {
                p.setBank(p.getBank() + p.getBet() * 2);
                gain = p.getBet();
            } else if (state == ResultState.PUSH) {
                p.setBank(p.getBank() + p.getBet());
            } else {
                gain = -p.getBet();
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            MatchHistory match = MatchHistory.builder()
                    .playerName(p.getPlayerName())
                    .resultState(state)
                    .bet(gain)
                    .date(LocalDate.parse(LocalDate.now().format(formatter), formatter))
                    .build();
            matchHistoryService.save(match);

            // refresh player's state for new game
            p.refresh();

            // build message of player
            msgBuilder.append(p.getPlayerName()).append(" ")
                    .append(state.getValue()).append(" ")
                    .append(gain);
            if (i != table.getPlayers().size() - 1) {
                msgBuilder.append(",");
            }
        }
        table.setIsPlaying(0);
        return msgBuilder.toString();
    }

    public Table start(Table table) {
        table.refreshAndInitDeck();
        return table;
    }
}
