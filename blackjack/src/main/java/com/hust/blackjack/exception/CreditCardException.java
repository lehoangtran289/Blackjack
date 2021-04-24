package com.hust.blackjack.exception;

public class CreditCardException extends Exception{
    public CreditCardException() { }
    public CreditCardException(Throwable throwable) { super(throwable); }
    public CreditCardException(String msg) { super(msg); }
    public CreditCardException(String msg, Throwable throwable) { super(msg, throwable); }

    public static class CreditCardNotFoundException extends RequestException {
        public CreditCardNotFoundException() { }
        public CreditCardNotFoundException(Throwable throwable) { super(throwable); }
        public CreditCardNotFoundException(String msg) { super(msg); }
        public CreditCardNotFoundException(String msg, Throwable throwable) { super(msg, throwable); }
    }

    public static class NotEnoughBalanceException extends RequestException {
        public NotEnoughBalanceException() { }
        public NotEnoughBalanceException(Throwable throwable) { super(throwable); }
        public NotEnoughBalanceException(String msg) { super(msg); }
        public NotEnoughBalanceException(String msg, Throwable throwable) { super(msg, throwable); }
    }
}
