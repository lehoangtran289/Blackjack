package com.hust.blackjack.model;

import com.hust.blackjack.exception.RequestException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@Getter
public enum RequestType {
    LOGIN("LOGIN"),
    LOGOUT("LOGOUT"),
    SIGNUP("SIGNUP"),
    SEARCHINFO("SEARCHINFO"),
    INFO("INFO"),
    HISTORY("HISTORY"),
    RANKING("GETRANKING"),
    ADDMONEY("ADD"),
    WITHDRAWMONEY("WDR"),
    PLAY("PLAY"),
    QUIT("QUIT"),
    CONTINUE("CONTINUE"),
    BET("BET"),
    HIT("HIT"),
    STAND("STAND"),
    CHAT("CHAT");

    private final String value;
    private static final Map<String, RequestType> map = new HashMap<>();

    static {
        for (RequestType type : RequestType.values()) {
            map.put(type.getValue(), type);
        }
    }

    public static RequestType from(String value) throws RequestException {
        RequestType ret = map.get(value.toUpperCase());
        if (ret == null)
            throw new RequestException();
        return ret;
    }
}
