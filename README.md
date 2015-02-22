# New Relic Events
A simple library that can be used to submit new relic custom events.

## Download
You can download the very first release of this library (and its dependencies) from the releases directory
https://github.com/Notronix/NewRelicEvents/blob/master/releases/NewRelicEvents-1.0.0.jar

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
    System.out.println("Uh oh... I have violated the NR Insights API.");
}

NewRelicClient client = new NewRelicClient();
client.setAccountId(0); // this should be your NR account ID
client.setInsertKey("YOUR KEY HERE"); // this should be your NR Insights Insert Key

try
{
    StatusLine responseStatus = client.submit(meaninglessEvent);

    System.out.println("NR responded with status code: " + responseStatus.getStatusCode());
}
catch (APIViolationException e)
{
    System.out.println("This can happen if your event's eventType is invalid according to the NR Insights API");
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
