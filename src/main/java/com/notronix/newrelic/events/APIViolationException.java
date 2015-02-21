package com.notronix.newrelic.events;

public class APIViolationException extends Exception
{
    public APIViolationException(String message)
    {
        super(message);
    }
}
