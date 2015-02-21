package com.notronix.newrelic.events;

public class NewRelicLoggingException extends Exception
{
    public NewRelicLoggingException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
