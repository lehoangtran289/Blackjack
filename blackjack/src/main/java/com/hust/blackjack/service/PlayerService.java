package com.hust.blackjack.service;

import com.hust.blackjack.exception.PlayerException;
import com.hust.blackjack.model.Player;
import com.hust.blackjack.repository.PlayerRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Optional;

@Log4j2
@Service
public class PlayerService {
    private final PlayerRepository playerRepository;

    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public boolean isChannelLoggedIn(SocketChannel channel) {
        return playerRepository.existsByChannel(channel);
    }

    public Optional<Player> getPlayerByNameAndPassword(String playerName, String password) {
        return playerRepository.getPlayerByNameAndPassword(playerName, password);
    }

    public Optional<Player> getPlayerByName(String playerName) {
        return playerRepository.getPlayerByName(playerName);
    }

    public boolean isPlayerExists(String playerName) {
        return playerRepository.isPlayerExists(playerName);
    }

    public Player saveNewPlayer(String playerName, String password) throws PlayerException.PlayerAlreadyExistsException {
        if (this.isPlayerExists(playerName)) {
            log.error("PlayerName {} already exists", playerName);
            throw new PlayerException.PlayerAlreadyExistsException();
        }
        Player newPlayer = new Player(playerName, password);
        return playerRepository.save(newPlayer);
    }

    public List<String> getAllPlayerName() {
        return playerRepository.getAllPlayerName();
    }
}
