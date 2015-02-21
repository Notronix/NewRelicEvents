package com.notronix.newrelic.events;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public abstract class NewRelicEvent
{
    private Map<String, Object> attributes = new HashMap<>(5);

    abstract public String getEventType();

    public void addAttribute(String name, String value)
    {
        attributes.put(name, value);
    }

    public void addAttribute(String name, double value)
    {
        attributes.put(name, value);
    }

    public void addAttribute(String name, int value)
    {
        attributes.put(name, value);
    }

    public void removeAttribute(String name)
    {
        attributes.remove(name);
    }

    public String getJSON()
    {
        return new Gson().toJson(attributes);
    }
}
