# New Relic Events
A simple Java library that can be used to submit [custom events](http://newrelic.com/insights/technology/integrations) to [New Relic Insights](http://newrelic.com/insights).

This library is useful when you need to send custom events to Insights, but you aren't running New Relic's [APM Java language agent](http://newrelic.com/java) (which has a [built-in capability for sending custom events to Insights](https://docs.newrelic.com/docs/agents/java-agent/custom-instrumentation/java-agent-api#api_methods))

## Download
Version 1.1.001 is available for download here https://github.com/Notronix/NewRelicEvents/releases/tag/v1.1.001

The NewRelicEvents library (and its dependencies) can be found in the releases directory

## Usage

Create a custom event object

<pre>
package mypackage

public class MeaninglessEvent extends NewRelicEvent
{
    @Override
    public String getEventType()
    {
        return "Meaningless";
    }
    
    public void setAttributeOne(String value) throws APIViolationException
    {
        addAttribute("attributeOne", value);
    }
}
</pre>

Then you can add values and submit your event as follows...

<pre>
...
MeaninglessEvent meaninglessEvent = new MeaninglessEvent();

try
{
    meaninglessEvent.setAttributeOne("some value");
}
catch (APIViolationException e)
{
    System.out.println("Uh oh... I have violated the New Relic Insights API.");
}

NewRelicClient client = new NewRelicClient();
client.setAccountId(0); // this should be your New Relic account ID, which is the 12345 part of your Insights account URL https://insights.newrelic.com/accounts/12345
client.setInsertKey("YOUR KEY HERE"); // this should be your [Insights Insert Key](https://docs.newrelic.com/docs/insights/new-relic-insights/adding-querying-data/inserting-custom-events-via-insights-api#register)

try
{
    StatusLine responseStatus = client.submit(meaninglessEvent);

    System.out.println("New Relic responded with status code: " + responseStatus.getStatusCode());
}
catch (APIViolationException e)
{
    System.out.println("This can happen if your event's eventType is invalid according to the New Relic Insights API");
}
catch (NewRelicLoggingException e)
{
    System.out.println("This can happen if there is some unexpected failure during the event submission.");
}
catch (IllegalStateException e)
{
    System.out.println("This will happen if the client is not initialized with an account ID and an insert key.");
}
...
</pre>
