package com.hust.blackjack.exception;

public class InvalidChannelException extends Exception {
    public InvalidChannelException() { }
    public InvalidChannelException(Throwable throwable) { super(throwable); }
    public InvalidChannelException(String msg) { super(msg); }
    public InvalidChannelException(String msg, Throwable throwable) { super(msg, throwable); }
}
