package com.hust.blackjack.repository.impl;

import com.hust.blackjack.model.MatchHistory;
import com.hust.blackjack.model.dto.PlayerRanking;
import com.hust.blackjack.repository.MatchHistoryRepository;
import com.hust.blackjack.repository.seed.Seed;
import com.hust.blackjack.model.dto.PlayerGameInfo;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
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
    public PlayerGameInfo getPlayerGameInfoByName(String playerName) {
        List<MatchHistory> playerMatchHistory = matchHistories.stream()
                .filter(m -> StringUtils.equals(m.getPlayerName(), playerName))
                .collect(Collectors.toList());
        int win = 0, lose = 0, push = 0, bust = 0, blackjack = 0;
        double moneyEarn = 0;
        for (MatchHistory match : playerMatchHistory) {
            switch (match.getResultState()) {
                case WIN:
                    win++;
                    moneyEarn += match.getBet();
                    break;
                case LOSE:
                    lose++;
                    moneyEarn -= match.getBet();
                    break;
                case PUSH:
                    push++;
                    break;
                case BUST:
                    bust++;
                    moneyEarn -= match.getBet();
                    break;
                case BLACKJACK:
                    blackjack++;
                    moneyEarn += match.getBet() * 1.5;
                    break;
                default:
                    break;
            }
        }
        return PlayerGameInfo.builder()
                .playerName(playerName)
                .moneyEarn(moneyEarn)
                .win(win)
                .lose(lose)
                .push(push)
                .bust(bust)
                .blackjack(blackjack)
                .build();
    }

    @Override
    public List<PlayerRanking> getAllPlayerRanking(List<String> playerNames) {
        List<PlayerRanking> rankings = playerNames.stream()
                .map(this::getPlayerGameInfoByName)
                .map(this::convertToPlayerRanking)
                .sorted(Comparator.comparingDouble(PlayerRanking::getMoneyEarn).reversed())
                .collect(Collectors.toList());
        for (int i = 0; i < rankings.size(); ++i) {
            rankings.get(i).setPlayerRank(i + 1);
        }
        return rankings;
    }

    private PlayerRanking convertToPlayerRanking(PlayerGameInfo playerGameInfo) {
        return PlayerRanking.builder()
                .playerName(playerGameInfo.getPlayerName())
                .moneyEarn(playerGameInfo.getMoneyEarn())
                .build();
    }
}