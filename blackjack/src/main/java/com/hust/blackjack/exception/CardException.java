package com.hust.blackjack.exception;

public class CardException extends Exception{
    public CardException() { }
    public CardException(Throwable throwable) { super(throwable); }
    public CardException(String msg) { super(msg); }
    public CardException(String msg, Throwable throwable) { super(msg, throwable); }

    public static class InvalidCardException extends CardException {
        public InvalidCardException() { }
        public InvalidCardException(Throwable throwable) { super(throwable); }
        public InvalidCardException(String msg) { super(msg); }
        public InvalidCardException(String msg, Throwable throwable) { super(msg, throwable); }
    }
}