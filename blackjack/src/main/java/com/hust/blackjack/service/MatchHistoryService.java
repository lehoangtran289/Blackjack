package com.hust.blackjack.service;

import com.hust.blackjack.exception.PlayerException;
import com.hust.blackjack.model.MatchHistory;
import com.hust.blackjack.model.Player;
import com.hust.blackjack.model.dto.PlayerGameInfo;
import com.hust.blackjack.model.dto.PlayerRanking;
import com.hust.blackjack.repository.MatchHistoryRepository;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Log4j2
@Service
public class MatchHistoryService {
    private final int PAGING = 3;

    private final MatchHistoryRepository matchHistoryRepository;
    private final PlayerService playerService;

    public MatchHistoryService(MatchHistoryRepository matchHistoryRepository, PlayerService playerService) {
        this.matchHistoryRepository = matchHistoryRepository;
        this.playerService = playerService;
    }

    public PlayerGameInfo getPlayerGameInfoByName(String playerName) throws PlayerException.PlayerNotFoundException {
        Optional<Player> optionalPlayer = playerService.getPlayerByName(playerName);
        if (optionalPlayer.isEmpty()) {
            log.error("Invalid playerName {}", playerName);
            throw new PlayerException.PlayerNotFoundException();
        }
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
                .player(optionalPlayer.get())
                .moneyEarn(moneyEarn)
                .win(win)
                .lose(lose)
                .push(push)
                .bust(bust)
                .blackjack(blackjack)
                .build();
    }

    public List<PlayerRanking> getAllPlayerRanking(String requestPlayerName) throws PlayerException.PlayerNotFoundException {
        PlayerRanking requestPlayer = null;

        // get all rankings from match histories
        List<String> playerNames = matchHistoryRepository.findAllPlayedPlayer();
        List<PlayerRanking> rankings = playerNames.stream()
                .map(name -> PlayerRanking.builder().playerName(name).build())
                .collect(Collectors.toList());

        List<MatchHistory> matchHistories = matchHistoryRepository.findAll();
        for (PlayerRanking playerRank : rankings) {
            for (MatchHistory match : matchHistories) {
                if (StringUtils.equals(playerRank.getPlayerName(), match.getPlayerName())) {
                    switch (match.getResultState()) {
                        case WIN:
                            playerRank.setMoneyEarn(playerRank.getMoneyEarn() + match.getBet());
                            break;
                        case LOSE:
                        case BUST:
                            playerRank.setMoneyEarn(playerRank.getMoneyEarn() - match.getBet());
                            break;
                        case BLACKJACK:
                            playerRank.setMoneyEarn(playerRank.getMoneyEarn() + match.getBet() * 1.5);
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        rankings.sort(Comparator.comparingDouble(PlayerRanking::getMoneyEarn).reversed());

        // update ranking
        for (int i = 0; i < rankings.size(); ++i) {
            PlayerRanking p = rankings.get(i);
            p.setPlayerRank(i + 1);

            // get request player
            if (StringUtils.equals(p.getPlayerName(), requestPlayerName)) {
                requestPlayer = p;
            }
        }

        // get first 20 players
        if (rankings.size() > PAGING) {
            rankings = rankings.subList(0, PAGING);
        }

        // add request player to top of return list
        if (requestPlayer == null) {    // if player hasn't played any match
            requestPlayer = PlayerRanking.builder()
                    .playerName(requestPlayerName)
                    .playerRank(-1)
                    .moneyEarn(0)
                    .build();
        }
        rankings.add(0, requestPlayer);
        return rankings;
    }

    private PlayerRanking convertToPlayerRanking(PlayerGameInfo playerGameInfo) {
        return PlayerRanking.builder()
                .playerName(playerGameInfo.getPlayer().getPlayerName())
                .moneyEarn(playerGameInfo.getMoneyEarn())
                .build();
    }
}
