package com.notronix.newrelic.events;

import com.google.common.net.UrlEscapers;

/**
 * The base class for any New Relic NRQL query that can be executed via the Insights API.  This class provides the convenience of handling the encoding of the
 * NRQL query.
 *
 * @author Clint Munden
 * @version 1.0
 * @see com.notronix.newrelic.events.NewRelicClient
 */
public class NewRelicQuery
{
    private String queryString;

    public NewRelicQuery(String queryString)
    {
        this.queryString = queryString;
    }

    public String getQueryString()
    {
        return UrlEscapers.urlFormParameterEscaper().escape(queryString);
    }
}
