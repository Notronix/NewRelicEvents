package com.notronix.newrelic.events;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(JUnit4.class)
public class NewRelicEventTest
{
    @Test
    public void testGetEventType()
    {
        NewRelicEvent event = new NewRelicEvent() {
            @Override
            public String getEventType()
            {
                return "  blah  ";
            }
        };

        assertEquals("Failed getting the event type.", "  blah  ", event.getEventType());
    }

    @Test
    public void testAddStringAttribute()
    {
        NewRelicEvent event = new NewRelicEvent() {
            @Override
            public String getEventType()
            {
                return "test";
            }
        };

        String value = "My Test Value";

        try
        {
            event.addAttribute("test1", value);
            assertEquals("Failed setting string attribute.", value, event.getAttributes().get("test1"));
        }
        catch (APIViolationException e)
        {
            // do nothing
        }
    }

    @Test(expected = APIViolationException.class)
    public void testAddStringAttributeTooLarge() throws APIViolationException
    {
        NewRelicEvent event = new NewRelicEvent() {
            @Override
            public String getEventType()
            {
                return "test";
            }
        };

        byte[] bytes = new byte[4000];

        for (int i = 0; i < bytes.length; i++)
        {
            bytes[i] = 'a';
        }

        String value = new String(bytes);
        event.addAttribute("test", value);
    }

    @Test(expected = APIViolationException.class)
    public void testTooManyAttributes() throws APIViolationException
    {
        NewRelicEvent event = new NewRelicEvent() {
            @Override
            public String getEventType()
            {
                return "test";
            }
        };

        for (int i = 0; i < 254; i++)
        {
            try
            {
                event.addAttribute("att" + i, i);
            }
            catch (APIViolationException e)
            {
                fail("Should have been able to add " + i + " attributes.");
            }
        }

        event.addAttribute("test", "test");
    }

    @Test
    public void testAddDoubleAttribute()
    {
        NewRelicEvent event = new NewRelicEvent() {
            @Override
            public String getEventType()
            {
                return "test";
            }
        };

        try
        {
            event.addAttribute("test", 22d);
        }
        catch (APIViolationException e)
        {
            fail("Should have been able to add a simple double attribute");
        }

        assertEquals("Failed to add simple double attribute", 22d, event.getAttributes().get("test"));
    }

    @Test
    public void testAddIntegerAttribute()
    {
        NewRelicEvent event = new NewRelicEvent() {
            @Override
            public String getEventType()
            {
                return "test";
            }
        };

        try
        {
            event.addAttribute("test", 22);
        }
        catch (APIViolationException e)
        {
            fail("Should have been able to add a simple integer attribute");
        }

        assertEquals("Failed to add simple integer attribute", 22, event.getAttributes().get("test"));
    }

    @Test
    public void testRemoveAttribute()
    {
        NewRelicEvent event = new NewRelicEvent() {
            @Override
            public String getEventType()
            {
                return "test";
            }
        };

        try
        {
            event.addAttribute("test", "test");
        }
        catch (APIViolationException e)
        {
            fail("Should have not failed to add a string attribute.");
        }

        assertEquals("Should only be one attribute.", 1, event.getAttributes().size());
        event.removeAttribute("test");
        assertEquals("Should be no attributes.", 0, event.getAttributes().size());
    }

    @Test
    public void testCleanAttributeNames()
    {
        NewRelicEvent event = new NewRelicEvent() {
            @Override
            public String getEventType()
            {
                return "test";
            }
        };

        try
        {
            for (String word : NewRelicEvent.RESERVED_KEYWORDS)
            {
                event.addAttribute(word, word);
            }
        }
        catch (APIViolationException e)
        {
            fail("Should have been able to add a simple attributes.");
        }

        for (String word : NewRelicEvent.RESERVED_KEYWORDS)
        {
            assertEquals("Attribute name (" + word + ") should have been cleaned.", word, event.getAttributes().get("`" + word + "`"));
        }
    }
}
