<html>
    <head>
        <title> Simple App Level JMS Queue/Topic Demo </title>
    </head>
    <body>
        <form action="QueueSendServlet">
            Fire-n-Forget: Enter Message to Send:<br/>
            <textarea name="jmsMessageFNF"cols="20" rows="1">Enter JMS Message</textarea><br/>
            <input type="Submit" value="Send JMS Message" />
            <input type="Reset" value="Clear" />
        </form>

        <hr/>

        <form action="QueueSendServlet">
            RPC: Enter Message to be echoed back:<br/>
            <textarea name="jmsMessageRPC"cols="20" rows="1">Enter JMS Message</textarea><br/>
            <input type="Submit" value="Send JMS Message" />
            <input type="Reset" value="Clear" />
        </form>
    </body>
</html>
