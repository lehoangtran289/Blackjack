package com.hust.blackjack.repository.impl;

import com.hust.blackjack.model.MatchHistory;
import com.hust.blackjack.model.dto.PlayerGameInfo;
import com.hust.blackjack.model.dto.PlayerRanking;
import com.hust.blackjack.repository.MatchHistoryRepository;
import com.hust.blackjack.repository.seed.Seed;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    public List<MatchHistory> findAllByPlayerName(String playerName) {
        return matchHistories.stream()
                .filter(m -> StringUtils.equals(m.getPlayerName(), playerName))
                .collect(Collectors.toList());
    }
}
