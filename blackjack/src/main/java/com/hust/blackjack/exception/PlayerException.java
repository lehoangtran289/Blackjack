package com.hust.blackjack.exception;

public class PlayerException extends Exception{
    public PlayerException() { }
    public PlayerException(Throwable throwable) { super(throwable); }
    public PlayerException(String msg) { super(msg); }
    public PlayerException(String msg, Throwable throwable) { super(msg, throwable); }

    public static class PlayerNotFoundException extends PlayerException {
        public PlayerNotFoundException() { }
        public PlayerNotFoundException(Throwable throwable) { super(throwable); }
        public PlayerNotFoundException(String msg) { super(msg); }
        public PlayerNotFoundException(String msg, Throwable throwable) { super(msg, throwable); }
    }

    public static class PlayerAlreadyExistsException extends PlayerException {
        public PlayerAlreadyExistsException() { }
        public PlayerAlreadyExistsException(Throwable throwable) { super(throwable); }
        public PlayerAlreadyExistsException(String msg) { super(msg); }
        public PlayerAlreadyExistsException(String msg, Throwable throwable) { super(msg, throwable); }
    }

    public static class NotEnoughBankBalanceException extends PlayerException{
        public NotEnoughBankBalanceException() { }
        public NotEnoughBankBalanceException(Throwable throwable) { super(throwable); }
        public NotEnoughBankBalanceException(String msg) { super(msg); }
        public NotEnoughBankBalanceException(String msg, Throwable throwable) { super(msg, throwable); }
    }
}
