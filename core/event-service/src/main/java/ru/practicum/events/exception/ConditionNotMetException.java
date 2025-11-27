package ru.practicum.events.exception;

public class ConditionNotMetException extends RuntimeException {
    public ConditionNotMetException(String s) {
        super(s);
    }
}
