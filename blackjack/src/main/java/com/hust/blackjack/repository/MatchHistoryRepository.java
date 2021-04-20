package com.hust.blackjack.repository;

import com.hust.blackjack.model.MatchHistory;

import java.util.List;

public interface MatchHistoryRepository {
    List<MatchHistory> findAll();
}
