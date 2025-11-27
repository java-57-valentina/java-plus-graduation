package ru.practicum.location.exception;

public class DuplicateLocationsException extends RuntimeException {
    public DuplicateLocationsException(String msg) {
        super(msg);
    }
}
