package com.hust.blackjack.repository.seed;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Getter
@Component
@RequiredArgsConstructor
public class Seed {
    private static final String ROOT_PATH = "src/main/java/com/hust/blackjack/repository/seed/json/";
    private final ObjectMapper mapper;

    @PostConstruct
    public void init() throws JsonProcessingException {

    }
}
