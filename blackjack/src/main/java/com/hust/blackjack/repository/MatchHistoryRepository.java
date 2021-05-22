package com.hust.blackjack.repository;

import com.hust.blackjack.model.MatchHistory;

import java.util.List;

public interface MatchHistoryRepository {
    List<MatchHistory> findAll();

    List<MatchHistory> findAllByPlayerName(String playerName);

    List<String> findAllPlayedPlayer();

    void save(MatchHistory match);
}
