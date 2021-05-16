package com.hust.blackjack.exception;

public class TableException extends Exception{
    public TableException() { }
    public TableException(Throwable throwable) { super(throwable); }
    public TableException(String msg) { super(msg); }
    public TableException(String msg, Throwable throwable) { super(msg, throwable); }

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
}
