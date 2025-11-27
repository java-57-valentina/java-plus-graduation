package ru.practicum.location.exception;

public class ConditionNotMetException extends RuntimeException {
    public ConditionNotMetException(String s) {
        super(s);
    }
}
