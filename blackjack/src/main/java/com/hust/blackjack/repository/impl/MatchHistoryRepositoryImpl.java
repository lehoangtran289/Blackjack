package com.hust.blackjack.repository.impl;

import com.hust.blackjack.model.MatchHistory;
import com.hust.blackjack.model.Player;
import com.hust.blackjack.repository.MatchHistoryRepository;
import com.hust.blackjack.repository.seed.Seed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MatchHistoryRepositoryImpl implements MatchHistoryRepository {
    private final Seed seed;
    private List<MatchHistory> matchHistories;

    @PostConstruct
    public void init() {
        matchHistories = seed.getMatchHistories();
    }

    @Override
    public List<MatchHistory> findAll() {
        return matchHistories;
    }
}
