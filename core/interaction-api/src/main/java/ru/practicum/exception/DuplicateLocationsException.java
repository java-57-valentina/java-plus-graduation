package ru.practicum.exception;

public class DuplicateLocationsException extends RuntimeException {
    public DuplicateLocationsException(String msg) {
        super(msg);
    }
}
