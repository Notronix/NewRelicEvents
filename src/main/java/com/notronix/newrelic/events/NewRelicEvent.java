package com.notronix.newrelic.events;

import org.apache.commons.codec.Charsets;

import java.util.*;

import static org.apache.commons.lang3.StringUtils.lowerCase;
import static org.apache.commons.lang3.StringUtils.trim;

/**
 * The base class for any custom New Relic event that can be submitted via the Insights API.
 *
 * @author Clint Munden
 * @version 1.0
 * @see com.notronix.newrelic.events.NewRelicClient
 */
public abstract class NewRelicEvent
{
    protected static final List<String> RESERVED_KEYWORDS = Arrays.asList("ago", "and", "as", "auto", "begin", "begintime", "compare", "day", "days", "end",
            "endtime", "explain", "facet", "from", "hour", "hours", "in", "is", "like", "limit", "minute", "minutes", "month", "months", "not", "null",
            "offset", "or", "second", "seconds", "select", "since", "timeseries", "until", "week", "weeks", "where", "with");

    private Map<String, Object> attributes = new HashMap<>(10);

    /**
     * Gets the event type of the custom event. Can contain only alphanumeric characters, _ underscores, and : colons.
     *
     * @return the custom event type.
     */
    abstract public String getEventType();

    /**
     * Adds a textual attribute to the custom event.  There is a limit of 254 total attributes per event.
     *
     * @param name  the name of the attribute
     * @param value the value of the attribute (maximum length of 4kb)
     * @throws APIViolationException if there are too many attributes or if the attribute length is exceeded.
     */
    public void addAttribute(String name, String value) throws APIViolationException
    {
        if (tooManyAttributes())
        {
            throw new APIViolationException("Attribute limit exceeded.");
        }

        String attributeValue = trim(value);

        if (attributeValue != null && attributeValue.getBytes(Charsets.UTF_8).length >= 4000)
        {
            throw new APIViolationException("Attribute is over the 4kb limit.");
        }

        attributes.put(cleanAttributeName(name), attributeValue);
    }

    /**
     * Adds a double attribute to the custom event.  There is a limit of 254 total attributes per event.
     *
     * @param name the name of the attribute
     * @param value the value of the attribute
     * @throws APIViolationException if there are too many attributes.
     */
    public void addAttribute(String name, double value) throws APIViolationException
    {
        if (tooManyAttributes())
        {
            throw new APIViolationException("Attribute limit exceeded.");
        }

        attributes.put(cleanAttributeName(name), value);
    }

    /**
     * Adds an integer attribute to the custom event.  There is a limit of 254 total attributes per event.
     *
     * @param name the name of the attribute
     * @param value the value of the attribute
     * @throws APIViolationException if there are too many attributes.
     */
    public void addAttribute(String name, int value) throws APIViolationException
    {
        if (tooManyAttributes())
        {
            throw new APIViolationException("Attribute limit exceeded.");
        }

        attributes.put(cleanAttributeName(name), value);
    }

    /**
     * Removes an attribute from the custom event.
     *
     * @param name the name of the attribute to be removed.
     */
    public void removeAttribute(String name)
    {
        attributes.remove(cleanAttributeName(name));
    }

    protected Map<String, Object> getAttributes()
    {
        return Collections.unmodifiableMap(attributes);
    }

    private boolean tooManyAttributes()
    {
        return (attributes.size() >= 254);
    }

    private static String cleanAttributeName(String name)
    {
        String attributeName = lowerCase(trim(name));

        if (RESERVED_KEYWORDS.contains(attributeName))
        {
            attributeName = "`" + attributeName + "`";
        }

        return attributeName;
    }
}
