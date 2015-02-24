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

            event.addAttribute("endTime", "this is a test");
            event.addAttribute("thisIsATest", "Another Test");
        }
        catch (APIViolationException e)
        {
            fail("Should have been able to add a simple attributes.");
        }

        for (String word : NewRelicEvent.RESERVED_KEYWORDS)
        {
            assertEquals("Attribute name (" + word + ") should have been cleaned.", word, event.getAttributes().get("`" + word + "`"));
        }

        assertEquals("Attribute name should have been cleaned.", "this is a test", event.getAttributes().get("`endTime`"));
        assertEquals("Attribute name should not have been cleaned.", "Another Test", event.getAttributes().get("thisIsATest"));
    }

    @Test
    public void testGetAttribute()
    {
        String attr1 = "string attribute";
        double attr2 = 2.5;
        int attr3 = 25;

        NewRelicEvent event = new NewRelicEvent() {
            @Override
            public String getEventType()
            {
                return "test";
            }
        };

        try
        {
            event.addAttribute("attr1", attr1);
            assertEquals("Failed getting string attribute", attr1, event.getAttribute("attr1"));

            event.addAttribute("attr2", attr2);
            assertEquals("Failed getting double attribute", attr2, event.getAttribute("attr2"));

            event.addAttribute("attr3", attr3);
            assertEquals("Failed getting integer attribute", attr3, event.getAttribute("attr3"));
        }
        catch (APIViolationException e)
        {
            fail("Should be able to add items without error.");
        }
    }
}
