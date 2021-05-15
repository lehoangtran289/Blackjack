package com.hust.blackjack.service;

import com.hust.blackjack.exception.LoginException;
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
        return playerRepository.findPlayerByNameAndPassword(playerName, password);
    }

    public Optional<Player> getPlayerByName(String playerName) {
        return playerRepository.findPlayerByName(playerName);
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
        return playerRepository.findAllPlayerName();
    }

    public Player login(String playerName, String password)
            throws PlayerException.PlayerNotFoundException,
            LoginException.PlayerAlreadyLoginException {
        Optional<Player> optionalPlayer = this.getPlayerByNameAndPassword(playerName, password);
        if (optionalPlayer.isEmpty()) {
            log.error("Invalid playerName password");
            throw new PlayerException.PlayerNotFoundException();
        }
        Player player = optionalPlayer.get();
        if (player.getChannel() != null) {
            log.error("player already login");
            throw new LoginException.PlayerAlreadyLoginException();
        }
        return optionalPlayer.get();
    }

    public Player logout(SocketChannel channel, String playerName)
            throws LoginException, PlayerException {
        Optional<Player> optionalPlayer = this.getPlayerByName(playerName);
        if (optionalPlayer.isEmpty()) {
            log.error("Invalid playerName {}", playerName);
            throw new PlayerException.PlayerNotFoundException();
        }
        Player player = optionalPlayer.get();
        if (player.getChannel() == null) {
            log.error("player {} haven't login", player.getPlayerName());
            throw new LoginException.PlayerNotLoginException();
        }
        if (player.getChannel() != channel) {
            log.error("invalid channel to logout");
            throw new LoginException.InvalidChannelToLogoutException();
        }
        player.setChannel(null);
        return player;
    }
}
