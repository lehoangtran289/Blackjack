package com.hust.blackjack.service;

import com.hust.blackjack.exception.LoginException;
import com.hust.blackjack.exception.PlayerException;
import com.hust.blackjack.exception.TableException;
import com.hust.blackjack.model.Player;
import com.hust.blackjack.model.Table;
import com.hust.blackjack.repository.TableRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Log4j2
@Service
public class TableService {
    private final TableRepository tableRepository;
    private final PlayerService playerService;

    public TableService(TableRepository tableRepository, PlayerService playerService) {
        this.tableRepository = tableRepository;
        this.playerService = playerService;
    }

    public Table play(String playerName) throws TableException, PlayerException, LoginException {
        // get user
        Optional<Player> optionalPlayer = playerService.getPlayerByName(playerName);
        if (optionalPlayer.isEmpty()) {
            log.error("Invalid playerName {}", playerName);
            throw new PlayerException.PlayerNotFoundException();
        }
        Player player = optionalPlayer.get();
        if (player.getChannel() == null) {
            log.error("Player {} not login", playerName);
            throw new LoginException.PlayerNotLoginException();
        }
        if (player.getTable() != null) {
            log.error("Player {} in another table, id = {}", playerName, player.getTable().getTableId());
            throw new TableException.PlayerInAnotherTableException();
        }
        if (player.getBank() < 10) {
            log.error("Invalid balance of player {} to start game, bl = {}", playerName, player.getBank());
            throw new TableException.NotEnoughBankBalanceException();
        }

        // get available table, create new if all table full
        Table table = tableRepository.findAvailableTable();

        // place player into table
        player.setTable(table);
        table.getPlayers().add(player);
        return table;
    }
}
