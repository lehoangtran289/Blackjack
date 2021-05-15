package com.hust.blackjack.repository.impl;

import com.hust.blackjack.model.Player;
import com.hust.blackjack.repository.PlayerRepository;
import com.hust.blackjack.repository.seed.Seed;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class PlayerRepositoryImpl implements PlayerRepository {
    private final Seed seed;
    private List<Player> players;

    @PostConstruct
    public void init() {
        players = seed.getPlayers();
    }

    @Override
    public List<Player> findAll() {
        return players;
    }

    @Override
    public Optional<Player> findPlayerByNameAndPassword(String playerName, String password) {
        return players.stream()
                .filter(p ->
                        StringUtils.equals(playerName, p.getPlayerName()) &&
                        StringUtils.equals(password, p.getPassword())
                )
                .findFirst();
    }

    @Override
    public Optional<Player> findPlayerByName(String playerName) {
        return players.stream()
                .filter(p -> StringUtils.equals(playerName, p.getPlayerName()))
                .findFirst();
    }

    @Override
    public List<Player> findPlayerByNameLIKE(String playerName) {
        return players.stream()
                .filter(p -> StringUtils.contains(p.getPlayerName(), playerName))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isPlayerExists(String playerName) {
        return players.stream()
                .anyMatch(p -> StringUtils.equals(playerName, p.getPlayerName()));
    }

    @Override
    public Player save(Player newPlayer) {
        players.add(newPlayer);
        return newPlayer;
    }

    @Override
    public boolean existsByChannel(SocketChannel channel) {
        return players.stream()
                .anyMatch(p -> p.getChannel() == channel);
    }

    @Override
    public List<String> findAllPlayerName() {
        return players.stream()
                .map(Player::getPlayerName)
                .collect(Collectors.toList());
    }
}
