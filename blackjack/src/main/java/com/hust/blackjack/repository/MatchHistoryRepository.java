package com.hust.blackjack.repository;

import com.hust.blackjack.model.MatchHistory;
import com.hust.blackjack.repository.seed.dto.PlayerGameInfo;

import java.util.List;

public interface MatchHistoryRepository {
    List<MatchHistory> findAll();

    PlayerGameInfo getPlayerGameInfoByName(String playerName);
}
