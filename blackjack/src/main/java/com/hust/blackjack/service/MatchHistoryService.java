package com.hust.blackjack.service;

import com.hust.blackjack.model.MatchHistory;
import com.hust.blackjack.model.dto.PlayerGameInfo;
import com.hust.blackjack.model.dto.PlayerRanking;
import com.hust.blackjack.repository.MatchHistoryRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Service
public class MatchHistoryService {
    private final MatchHistoryRepository matchHistoryRepository;
    private final PlayerService playerService;

    public MatchHistoryService(MatchHistoryRepository matchHistoryRepository, PlayerService playerService) {
        this.matchHistoryRepository = matchHistoryRepository;
        this.playerService = playerService;
    }

    public PlayerGameInfo getPlayerGameInfoByName(String playerName) {
        List<MatchHistory> playerMatchHistory = matchHistoryRepository.findAllByPlayerName(playerName);
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

    public List<PlayerRanking> getAllPlayerRanking() {
        List<String> playerNames = playerService.getAllPlayerName();
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
