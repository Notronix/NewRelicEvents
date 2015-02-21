package com.notronix.newrelic.events;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public class NewRelicClient
{
    private String accountId;
    private String insertKey;

    public String getAccountId()
    {
        return accountId;
    }

    public void setAccountId(String accountId)
    {
        this.accountId = accountId;
    }

    public String getInsertKey()
    {
        return insertKey;
    }

    public void setInsertKey(String insertKey)
    {
        this.insertKey = insertKey;
    }

    public int submit(NewRelicEvent event) throws NewRelicLoggingException
    {
        event.addAttribute("eventType", event.getEventType());

        HttpPost request = new HttpPost("https://insights-collector.newrelic.com/v1/accounts/" + accountId + "/events");
        request.addHeader("Content-Type", "application/json");
        request.addHeader("X-Insert-Key", insertKey);
        request.setEntity(new ByteArrayEntity(event.getJSON().getBytes()));

        try (CloseableHttpClient client = HttpClientBuilder.create().build(); CloseableHttpResponse response = client.execute(request))
        {
            return response.getStatusLine().getStatusCode();
        }
        catch (Exception e)
        {
            throw new NewRelicLoggingException("NewRelic logging failure.", e);
        }
    }
}
