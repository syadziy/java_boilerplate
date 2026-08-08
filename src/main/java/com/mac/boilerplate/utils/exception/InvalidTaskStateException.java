package com.mac.boilerplate.utils.exception;

public class InvalidTaskStateException extends IllegalArgumentException {

    public InvalidTaskStateException(String message) {
        super(message);
    }
}
