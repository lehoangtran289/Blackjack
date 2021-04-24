package com.hust.blackjack.repository;

import com.hust.blackjack.model.Player;

import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Optional;

public interface PlayerRepository {
    List<Player> findAll();

    Optional<Player> getPlayerByNameAndPassword(String playerName, String password);

    Optional<Player> getPlayerByName(String playerName);

    boolean isPlayerExists(String playerName);

    void save(Player newPlayer);

    boolean existsByChannel(SocketChannel channel);

    List<String> getAllPlayerName();
}
