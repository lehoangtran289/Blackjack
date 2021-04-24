package com.hust.blackjack.exception;

public class PlayerException extends Exception{
    public PlayerException() { }
    public PlayerException(Throwable throwable) { super(throwable); }
    public PlayerException(String msg) { super(msg); }
    public PlayerException(String msg, Throwable throwable) { super(msg, throwable); }

    public static class PlayerNotFoundException extends RequestException {
        public PlayerNotFoundException() { }
        public PlayerNotFoundException(Throwable throwable) { super(throwable); }
        public PlayerNotFoundException(String msg) { super(msg); }
        public PlayerNotFoundException(String msg, Throwable throwable) { super(msg, throwable); }
    }
}
