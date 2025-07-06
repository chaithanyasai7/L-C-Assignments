package com.newsaggregator.exceptions;

public class NewsAggregatorException extends Exception {
    public NewsAggregatorException(String message) {
        super(message);
    }

    public NewsAggregatorException(String message, Throwable cause) {
        super(message, cause);
    }
}