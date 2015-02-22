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
        catch (IllegalStateException | NewRelicLoggingException | NullPointerException e)
        {
            fail("Should not have thrown any other exception than an APIViolationException.");
        }
    }
}
