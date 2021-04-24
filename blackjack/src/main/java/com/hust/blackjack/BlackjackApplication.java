package com.hust.blackjack;

import com.hust.blackjack.service.BlackjackSocketServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RequiredArgsConstructor
@Slf4j
public class BlackjackApplication implements CommandLineRunner {

    private final BlackjackSocketServer blackjackSocketServer;

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(BlackjackApplication.class);
        app.run(args);
    }

    @Override
    public void run(String... args) {
        try {
            blackjackSocketServer.run();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
