package com.hust.blackjack.repository;

import com.hust.blackjack.model.Player;

import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Optional;

public interface PlayerRepository {
    List<Player> findAll();

    Optional<Player> findPlayerByNameAndPassword(String playerName, String password);

    Optional<Player> findPlayerByName(String playerName);

    boolean isPlayerExists(String playerName);

    Player save(Player newPlayer);

    boolean existsByChannel(SocketChannel channel);

    List<String> findAllPlayerName();

    List<Player> findPlayerByNameLIKE(String playerName);
}
