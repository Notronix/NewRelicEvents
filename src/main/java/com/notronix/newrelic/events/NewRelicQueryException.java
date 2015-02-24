package com.notronix.newrelic.events;

public class NewRelicQueryException extends Exception
{
    public NewRelicQueryException(String message)
    {
        super(message);
    }

    public NewRelicQueryException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
