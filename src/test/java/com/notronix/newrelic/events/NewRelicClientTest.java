package com.notronix.newrelic.events;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.fail;

@RunWith(JUnit4.class)
public class NewRelicClientTest
{
    @Test(expected = APIViolationException.class)
    public void testInvalidEventType() throws APIViolationException
    {
        NewRelicEvent event = new NewRelicEvent() {
            @Override
            public String getEventType()
            {
                return "Bad.Event";
            }
        };

        NewRelicClient client = new NewRelicClient();
        client.setInsertKey("test");
        client.setAccountId(1);

        try
        {
            client.submit(event);
        }
        catch (IllegalStateException | NewRelicInsertException | NullPointerException e)
        {
            fail("Should have thrown an APIViolationException.");
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testUninitializedClient() throws IllegalStateException
    {
        NewRelicClient client = new NewRelicClient();

        try
        {
            client.submit(null);
        }
        catch (APIViolationException | NewRelicInsertException | NullPointerException e)
        {
            fail("Should have thrown an IllegalStateException.");
        }
    }

    @Test(expected = NullPointerException.class)
    public void testNullEventError()
    {
        NewRelicClient client = new NewRelicClient();
        client.setInsertKey("test");
        client.setAccountId(1);

        try
        {
            client.submit(null);
        }
        catch (APIViolationException | NewRelicInsertException | IllegalStateException e)
        {
            fail("Should have thrown a NullPointerException.");
        }
    }

    @Test(expected = NullPointerException.class)
    public void testNullQueryError()
    {
        NewRelicClient client = new NewRelicClient();
        client.setQueryKey("test");
        client.setAccountId(1);

        try
        {
            client.query(null);
        }
        catch (NewRelicQueryException | IllegalStateException e)
        {
            fail("Should have thrown a NullPointerException.");
        }
    }
}
