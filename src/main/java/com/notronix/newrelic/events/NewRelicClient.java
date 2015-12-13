package com.notronix.newrelic.events;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.http.HttpHost;
import org.apache.http.StatusLine;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isAlphanumeric;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * A simple client for submitting and querying custom New Relic events via the Insights API.
 *
 * @author Clint Munden
 * @version 1.0
 * @see com.notronix.newrelic.events.NewRelicEvent
 * @see com.notronix.newrelic.events.NewRelicQuery
 */
public class NewRelicClient
{
    private int accountId;
    private String insertKey;
    private String queryKey;

    /**
     * Gets the account ID that the client will send events to.
     *
     * @return the new relic account ID.
     */
    public int getAccountId()
    {
        return accountId;
    }

    /**
     * Sets the account ID that this client shall send events to.
     *
     * @param accountId the new relic account ID.
     */
    public void setAccountId(int accountId)
    {
        this.accountId = accountId;
    }

    /**
     * Gets the New Relic API insert key that this client will use to insert custom events.
     *
     * @return the New Relic API insert key.
     */
    public String getInsertKey()
    {
        return insertKey;
    }

    /**
     * Sets the New Relic API insert key that this client will use to insert custom events.
     *
     * @param insertKey the New Relic API insert key.
     */
    public void setInsertKey(String insertKey)
    {
        this.insertKey = insertKey;
    }

    /**
     * Gets the New Relic API query key that this client will use to query custom events.
     *
     * @return the New Relic API query key.
     */
    public String getQueryKey()
    {
        return queryKey;
    }

    /**
     * Sets the New Relic API query key that this client will use to query custom events.
     *
     * @param queryKey the New Relic API query key.
     */
    public void setQueryKey(String queryKey)
    {
        this.queryKey = queryKey;
    }

    /**
     * Submits a custom new relic event via the New Relic Insights API.  Before this method is called, the client should be initialized with a valid New Relic
     * account ID and insert key.
     *
     * @param event The New Relic custom event to be submitted.
     * @return the response status returned by the New Relic Insights API.
     * @throws IllegalStateException   if this method is called before an <code>accountId</code> and <code>insertKey</code> are set.
     * @throws NewRelicInsertException if there is any unexpected exception while attempting to submit an event.
     * @throws APIViolationException   if the event type of the event violates the insights API specifications.
     * @throws NullPointerException    if the event is null
     */
    public StatusLine submit(NewRelicEvent event) throws IllegalStateException, NewRelicInsertException, APIViolationException, NullPointerException
    {
        if (accountId <= 0 || isBlank(insertKey))
        {
            throw new IllegalStateException("Uninitialized Client.  Please initialize with a valid NewRelic accountId and a valid insert key.");
        }

        if (event == null)
        {
            throw new NullPointerException("event is null.");
        }

        String eventType = event.getEventType();

        if (isInvalidEventType(eventType))
        {
            throw new APIViolationException(eventType + " is illegal.  Must be a combination of alphanumeric characters, _ underscores, and : colons.");
        }

        Map<String, Object> attributes = new HashMap<>(event.getAttributes());
        attributes.put("eventType", eventType);

        RequestConfig.Builder defaultRequestConfigBuilder = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
                .setExpectContinueEnabled(true);
        URI newRelicInsightsURI = URI.create("https://insights-collector.newrelic.com/");
        ProxySelector proxySelector = ProxySelector.getDefault();
        List<Proxy> proxies = proxySelector.select(newRelicInsightsURI);
        if (!proxies.isEmpty())
        {
            Proxy proxy = proxies.get(0);
            HttpHost proxyHost = new HttpHost(proxy.address().toString());
            defaultRequestConfigBuilder.setProxy(proxyHost);
        }
        RequestConfig defaultRequestConfig = defaultRequestConfigBuilder.build();

        RequestConfig requestConfig = RequestConfig.copy(defaultRequestConfig)
                .setSocketTimeout(30000)
                .setConnectTimeout(30000)
                .setConnectionRequestTimeout(30000)
                .build();

        HttpPost request = new HttpPost("https://insights-collector.newrelic.com/v1/accounts/" + accountId + "/events");
        request.addHeader("Content-Type", "application/json");
        request.addHeader("X-Insert-Key", insertKey);
        request.setEntity(new ByteArrayEntity(new Gson().toJson(attributes).getBytes()));
        request.setConfig(requestConfig);

        try (CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
             CloseableHttpResponse response = client.execute(request))
        {
            return response.getStatusLine();
        }
        catch (Exception e)
        {
            throw new NewRelicInsertException("NewRelic insertion failure.", e);
        }
    }

    /**
     * Executes the NRQL query via the New Relic Insights API.  Before this method is called, the client should be initialized with a valid New Relic account
     * ID and query key.
     *
     * @param query the NRQL query to be executed.
     * @return the json response obtained as a result of executing the query.
     * @throws IllegalStateException  if this method is called before an <code>accountId</code> and <code>queryKey</code> are set.
     * @throws NullPointerException   if the <code>query</code> is null
     * @throws NewRelicQueryException if there is any unexpected exception while attempting to execute the query, or if there is an error in the NRQL.
     */
    public String query(NewRelicQuery query) throws IllegalStateException, NullPointerException, NewRelicQueryException
    {
        if (accountId <= 0 || isBlank(queryKey))
        {
            throw new IllegalStateException("Uninitialized Client.  Please initialize with a valid NewRelic accountId and a valid query key.");
        }

        if (query == null)
        {
            throw new NullPointerException("query is null.");
        }

        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
                .setExpectContinueEnabled(true)
                .build();

        RequestConfig requestConfig = RequestConfig.copy(defaultRequestConfig)
                .setSocketTimeout(30000)
                .setConnectTimeout(30000)
                .setConnectionRequestTimeout(30000)
                .build();

        HttpGet request = new HttpGet("https://insights-api.newrelic.com/v1/accounts/" + accountId + "/query?nrql=" + query.getQueryString());
        request.addHeader("Accept", "application/json");
        request.addHeader("X-Query-Key", queryKey);
        request.setConfig(requestConfig);

        String json;

        try (CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
             CloseableHttpResponse response = client.execute(request))
        {
            json = EntityUtils.toString(response.getEntity());
        }
        catch (Exception e)
        {
            throw new NewRelicQueryException("NewRelic query failure.", e);
        }

        try
        {
            Map results = new Gson().fromJson(json, Map.class);
            Object error = results.get("error");

            if (error instanceof String)
            {
                throw new NewRelicQueryException((String) error);
            }
        }
        catch (JsonSyntaxException e)
        {
            throw new NewRelicQueryException("Error parsing json response.", e);
        }

        return json;
    }

    private static boolean isInvalidEventType(String eventType)
    {
        return eventType == null || !isAlphanumeric(eventType.replaceAll(":", "").replaceAll("_", ""));
    }
}
