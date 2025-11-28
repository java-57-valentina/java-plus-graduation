package ru.practicum.events.exception;

public class NoAccessException extends RuntimeException {
    public NoAccessException(String s) {
        super(s);
    }
}
