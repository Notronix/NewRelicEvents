package com.notronix.newrelic.events;

public class NewRelicInsertException extends Exception
{
    public NewRelicInsertException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
