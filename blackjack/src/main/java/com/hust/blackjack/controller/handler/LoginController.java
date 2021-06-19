package com.hust.blackjack.controller.handler;

import com.hust.blackjack.exception.LoginException;
import com.hust.blackjack.exception.PlayerException;
import com.hust.blackjack.exception.RequestException;
import com.hust.blackjack.model.Player;
import com.hust.blackjack.model.RequestType;
import com.hust.blackjack.service.PlayerService;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.List;

import static com.hust.blackjack.utils.MessageUtils.writeToChannel;

@Log4j2
@Service
public class LoginController implements IController{

    private final PlayerService playerService;

    public LoginController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @Override
    public void processRequest(SocketChannel channel, RequestType requestType, List<String> request)
            throws RequestException, IOException, LoginException, PlayerException {
        switch (requestType) {
            case LOGIN: {
                if (playerService.isChannelLoggedIn(channel)) {
                    writeToChannel(channel, "LOGINFAIL=Channel already login, logout first");
                    log.error("channel {} already login", channel);
                    throw new LoginException("channel already login");
                }
                String playerName = request.get(1);
                String password = request.get(2);
                try {
                    Player player = playerService.login(playerName, password, channel);
                    writeToChannel(channel, "LOGINSUCCESS=" + player.getPlayerName() + " " + player.getBank());
                    log.info("Player {} login success at channel {}", player.getPlayerName(),
                            player.getChannel());
                } catch (PlayerException.PlayerNotFoundException e) {
                    writeToChannel(channel, "LOGINFAIL=Username or password incorrect");
                    throw e;
                } catch (LoginException.PlayerAlreadyLoginException e) {
                    writeToChannel(channel, "LOGINFAIL=Player already login");
                    throw e;
                }
                break;
            }
            case LOGOUT: {
                String playerName = request.get(1);
                try {
                    Player player = playerService.logout(channel, playerName);
                    writeToChannel(channel, "LOGOUTSUCCESS");
                    log.info("Player {} at channel {} logout success",
                            player.getPlayerName(), channel
                    );
                } catch (PlayerException.PlayerNotFoundException e) {
                    writeToChannel(channel, "LOGOUTFAIL=Username not found");
                    throw e;
                } catch (LoginException.PlayerNotLoginException e) {
                    writeToChannel(channel, "LOGOUTFAIL=Player haven't login");
                    throw e;
                } catch (LoginException.InvalidChannelToLogoutException e) {
                    writeToChannel(channel, "LOGOUTFAIL=Invalid channel to logout");
                    throw e;
                }
                break;
            }
            case SIGNUP: {
                String playerName = request.get(1);
                String password = request.get(2);
                try {
                    Player newPlayer = playerService.saveNewPlayer(playerName, password);
                    writeToChannel(channel, "SIGNUPSUCCESS");
                    log.info("Sign up success with player {}", newPlayer);
                } catch (PlayerException.PlayerAlreadyExistsException e) {
                    writeToChannel(channel, "SIGNUPFAIL=Username already exists");
                    throw e;
                }
                break;
            }
            default:
                writeToChannel(channel, "FAIL=Invalid request");
                throw new RequestException.InvalidRequestTypeException(request.get(0));
        }
    }
}
