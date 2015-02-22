package com.notronix.newrelic.events;

import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.StatusLine;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isAlphanumeric;

/**
 * A simple client for submitting custom New Relic events.
 *
 * @author Clint Munden
 * @version 1.0
 * @see com.notronix.newrelic.events.NewRelicEvent
 */
public class NewRelicClient
{
    private int accountId;
    private String insertKey;

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
     * Submits a custom new relic event via the New Relic Insights API.
     * <p/>
     * Before this method is called, the client should be initialized with a valid New Relic account ID and insert key.
     *
     * @param event The New Relic custom event to be submitted.
     * @return the response status returned by the New Relic Insights API.
     * @throws IllegalStateException    if this method is called before an <code>accountId</code> and <code>insertKey</code> are set.
     * @throws NewRelicLoggingException if there is any unexpected exception while attempting to submit an event.
     * @throws APIViolationException    if the event type of the event violates the insights API specifications.
     */
    public StatusLine submit(NewRelicEvent event) throws IllegalStateException, NewRelicLoggingException, APIViolationException
    {
        if (accountId <= 0 || StringUtils.isBlank(insertKey))
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

        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
                .setExpectContinueEnabled(true)
                .build();

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
            throw new NewRelicLoggingException("NewRelic logging failure.", e);
        }
    }

    private static boolean isInvalidEventType(String eventType)
    {
        return eventType == null || !isAlphanumeric(eventType.replaceAll(":", "").replaceAll("_", ""));
    }
}
