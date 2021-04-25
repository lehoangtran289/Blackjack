package com.hust.blackjack.exception;

public class LoginException extends Exception{
    public LoginException() { }
    public LoginException(Throwable throwable) { super(throwable); }
    public LoginException(String msg) { super(msg); }
    public LoginException(String msg, Throwable throwable) { super(msg, throwable); }

    public static class PlayerAlreadyLoginException extends LoginException {
        public PlayerAlreadyLoginException() { }
        public PlayerAlreadyLoginException(Throwable throwable) { super(throwable); }
        public PlayerAlreadyLoginException(String msg) { super(msg); }
        public PlayerAlreadyLoginException(String msg, Throwable throwable) { super(msg, throwable); }
    }
    
    public static class PlayerNotLoginException extends LoginException {
        public PlayerNotLoginException() { }
        public PlayerNotLoginException(Throwable throwable) { super(throwable); }
        public PlayerNotLoginException(String msg) { super(msg); }
        public PlayerNotLoginException(String msg, Throwable throwable) { super(msg, throwable); }
    }

    public static class InvalidChannelToLogoutException extends LoginException {
        public InvalidChannelToLogoutException() { }
        public InvalidChannelToLogoutException(Throwable throwable) { super(throwable); }
        public InvalidChannelToLogoutException(String msg) { super(msg); }
        public InvalidChannelToLogoutException(String msg, Throwable throwable) { super(msg, throwable); }
    }
}
