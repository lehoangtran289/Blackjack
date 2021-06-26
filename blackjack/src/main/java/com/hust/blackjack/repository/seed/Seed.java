package com.hust.blackjack.repository.seed;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hust.blackjack.model.CreditCard;
import com.hust.blackjack.model.MatchHistory;
import com.hust.blackjack.model.Player;
import com.hust.blackjack.model.dto.PlayerDTO;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Component
@RequiredArgsConstructor
public class Seed {
    private static final String ROOT_PATH = "src/main/java/com/hust/blackjack/repository/seed/json/";
    private final ObjectMapper mapper;
    private List<Player> players;
    private List<CreditCard> creditCards;
    private List<MatchHistory> matchHistories;

    @PostConstruct
    public void init() throws JsonProcessingException {
        players = new ArrayList<>();
        String playersJson = FileReader.read(new File(ROOT_PATH + "players.json").getAbsolutePath());
        List<PlayerDTO> playerDTOList = new ArrayList<>(mapper.readValue(playersJson, new TypeReference<>() {}));
        players.addAll(
                playerDTOList.stream()
                .map(p -> Player.builder()
                        .playerName(p.getPlayerName())
                        .password(p.getPassword())
                        .balance(p.getBank())
                        .build())
                .collect(Collectors.toList())
        );

        creditCards = new ArrayList<>();
        String creditCardsJson = FileReader.read(new File(ROOT_PATH + "creditCards.json").getAbsolutePath());
        creditCards.addAll(mapper.readValue(creditCardsJson, new TypeReference<>() {}));

        matchHistories = new ArrayList<>();
        String matchHistoriesJson = FileReader.read(new File(ROOT_PATH + "matchHistories.json").getAbsolutePath());
        matchHistories.addAll(mapper.readValue(matchHistoriesJson, new TypeReference<>() {}));
    }
}
