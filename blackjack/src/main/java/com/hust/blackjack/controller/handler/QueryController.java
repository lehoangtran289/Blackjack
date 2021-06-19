package com.hust.blackjack.controller.handler;

import com.hust.blackjack.exception.PlayerException;
import com.hust.blackjack.exception.RequestException;
import com.hust.blackjack.model.MatchHistory;
import com.hust.blackjack.model.RequestType;
import com.hust.blackjack.model.dto.PlayerGameInfo;
import com.hust.blackjack.model.dto.PlayerRanking;
import com.hust.blackjack.service.MatchHistoryService;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.hust.blackjack.utils.MessageUtils.writeToChannel;

@Log4j2
@Service
public class QueryController implements IController {

    private final MatchHistoryService matchHistoryService;

    public QueryController(MatchHistoryService matchHistoryService) {
        this.matchHistoryService = matchHistoryService;
    }

    @Override
    public void processRequest(SocketChannel channel, RequestType requestType, List<String> request)
            throws RequestException, IOException, PlayerException {

        switch (requestType) {
            case SEARCHINFO: {
                if (request.size() == 1) {
                    log.error("searchinfo playername empty");
                    writeToChannel(channel, "SEARCHFAIL");
                    throw new PlayerException.PlayerNotFoundException();
                }
                String playerName = request.get(1);
                try {
                    List<PlayerGameInfo> playerGameInfos =
                            matchHistoryService.searchPlayerGameInfoByName(playerName);
                    String msg = "SEARCHSUCCESS=" + playerGameInfos.stream()
                            .map(playerGameInfo -> String.join(" ", Arrays.asList(
                                    playerGameInfo.getPlayer().getPlayerName(),
                                    String.valueOf(playerGameInfo.getPlayer().getBank()),
                                    String.valueOf(playerGameInfo.getMoneyEarn()),
                                    String.valueOf(playerGameInfo.getWin()),
                                    String.valueOf(playerGameInfo.getLose()),
                                    String.valueOf(playerGameInfo.getPush()),
                                    String.valueOf(playerGameInfo.getBust()),
                                    String.valueOf(playerGameInfo.getBlackjack())
                                    )
                            ))
                            .collect(Collectors.joining(","));
                    writeToChannel(channel, msg);
                    log.info("Game Info of player {}: {}", playerName, playerGameInfos);
                } catch (PlayerException.PlayerNotFoundException e) {
                    writeToChannel(channel, "SEARCHFAIL");
                    throw e;
                }
                break;
            }
            case INFO: {
                String playerName = request.get(1);
                try {
                    PlayerGameInfo playerGameInfo = matchHistoryService.getPlayerGameInfoByName(playerName);
                    String msg = requestType.getValue() + "=" +
                            String.join(" ", Arrays.asList(
                                    playerGameInfo.getPlayer().getPlayerName(),
                                    String.valueOf(playerGameInfo.getPlayer().getBank()),
                                    String.valueOf(playerGameInfo.getMoneyEarn()),
                                    String.valueOf(playerGameInfo.getWin()),
                                    String.valueOf(playerGameInfo.getLose()),
                                    String.valueOf(playerGameInfo.getPush()),
                                    String.valueOf(playerGameInfo.getBust()),
                                    String.valueOf(playerGameInfo.getBlackjack())
                            ));
                    writeToChannel(channel, msg);
                    log.info("Game Info of player {}: {}", playerName, playerGameInfo);
                } catch (PlayerException.PlayerNotFoundException e) {
                    writeToChannel(channel, "INFOFAIL=Player not found");
                    throw e;
                }
                break;
            }
            case HISTORY: {
                String playerName = request.get(1);
                List<MatchHistory> histories = matchHistoryService.getPlayerHistory(playerName);
                String msg = "HISTORY=" + histories.stream().map(MatchHistory::toString)
                        .collect(Collectors.joining(","));
                writeToChannel(channel, msg);
                break;
            }
            case RANKING: { // RANK={ranking1} {user_name1} {money_earn1}, ...
                String playerName = request.get(1);
                List<PlayerRanking> rankings = matchHistoryService.getAllPlayerRanking(playerName);
                String msg = "RANK=" + rankings.stream().map(PlayerRanking::toString)
                        .collect(Collectors.joining(","));
                writeToChannel(channel, msg);
                log.info("Get rankings {}", rankings);
                break;
            }

            default:
                writeToChannel(channel, "FAIL=Invalid request");
                throw new RequestException.InvalidRequestTypeException(request.get(0));
        }
    }
}
