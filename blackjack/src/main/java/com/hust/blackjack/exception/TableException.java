package com.hust.blackjack.exception;

public class TableException extends Exception{
    public TableException() { }
    public TableException(Throwable throwable) { super(throwable); }
    public TableException(String msg) { super(msg); }
    public TableException(String msg, Throwable throwable) { super(msg, throwable); }

    public static class TableNotFoundException extends TableException {
        public TableNotFoundException() { }
        public TableNotFoundException(Throwable throwable) { super(throwable); }
        public TableNotFoundException(String msg) { super(msg); }
        public TableNotFoundException(String msg, Throwable throwable) { super(msg, throwable); }
    }

    public static class NotEnoughBankBalanceException extends TableException {
        public NotEnoughBankBalanceException() { }
        public NotEnoughBankBalanceException(Throwable throwable) { super(throwable); }
        public NotEnoughBankBalanceException(String msg) { super(msg); }
        public NotEnoughBankBalanceException(String msg, Throwable throwable) { super(msg, throwable); }
    }

    public static class PlayerInAnotherTableException extends TableException {
        public PlayerInAnotherTableException() { }
        public PlayerInAnotherTableException(Throwable throwable) { super(throwable); }
        public PlayerInAnotherTableException(String msg) { super(msg); }
        public PlayerInAnotherTableException(String msg, Throwable throwable) { super(msg, throwable); }
    }

    public static class PlayerNotFoundInTableException extends TableException {
        public PlayerNotFoundInTableException() { }
        public PlayerNotFoundInTableException(Throwable throwable) { super(throwable); }
        public PlayerNotFoundInTableException(String msg) { super(msg); }
        public PlayerNotFoundInTableException(String msg, Throwable throwable) { super(msg, throwable); }
    }

    public static class GameNotStartException extends TableException {
        public GameNotStartException() { }
        public GameNotStartException(Throwable throwable) { super(throwable); }
        public GameNotStartException(String msg) { super(msg); }
        public GameNotStartException(String msg, Throwable throwable) { super(msg, throwable); }
    }

    public static class WrongTurnException extends TableException {
        public WrongTurnException() { }
        public WrongTurnException(Throwable throwable) { super(throwable); }
        public WrongTurnException(String msg) { super(msg); }
        public WrongTurnException(String msg, Throwable throwable) { super(msg, throwable); }
    }
}
