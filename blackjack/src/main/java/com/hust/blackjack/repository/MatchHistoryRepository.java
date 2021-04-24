package com.hust.blackjack.repository;

import com.hust.blackjack.model.MatchHistory;
import com.hust.blackjack.model.dto.PlayerGameInfo;
import com.hust.blackjack.model.dto.PlayerRanking;

import java.util.List;

public interface MatchHistoryRepository {
    List<MatchHistory> findAll();

    PlayerGameInfo getPlayerGameInfoByName(String playerName);

    List<PlayerRanking> getAllPlayerRanking(List<String> playerNames);
}
