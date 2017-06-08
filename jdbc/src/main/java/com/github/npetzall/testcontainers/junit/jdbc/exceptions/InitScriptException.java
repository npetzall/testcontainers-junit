package com.github.npetzall.testcontainers.junit.jdbc.exceptions;

public class InitScriptException extends RuntimeException {

    public InitScriptException(String message, Throwable cause) {
        super(message, cause);
    }

}
