package com.hust.blackjack.exception;

public class RequestException extends Exception{
    public RequestException() { }
    public RequestException(Throwable throwable) { super(throwable); }
    public RequestException(String msg) { super(msg); }
    public RequestException(String msg, Throwable throwable) { super(msg, throwable); }

    public static class InvalidRequestTypeException extends RequestException {
        public InvalidRequestTypeException() { }
        public InvalidRequestTypeException(Throwable throwable) { super(throwable); }
        public InvalidRequestTypeException(String msg) { super(msg); }
        public InvalidRequestTypeException(String msg, Throwable throwable) { super(msg, throwable); }
    }

    public static class InvalidRequestLengthException extends RequestException {
        public InvalidRequestLengthException() { }
        public InvalidRequestLengthException(Throwable throwable) { super(throwable); }
        public InvalidRequestLengthException(String msg) { super(msg); }
        public InvalidRequestLengthException(String msg, Throwable throwable) { super(msg, throwable); }
    }
}