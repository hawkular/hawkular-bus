package org.hawkular.bus.sample.mdb;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.jms.QueueConnectionFactory;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hawkular.bus.common.ConnectionContextFactory;
import org.hawkular.bus.common.Endpoint;
import org.hawkular.bus.common.MessageId;
import org.hawkular.bus.common.MessageProcessor;
import org.hawkular.bus.common.SimpleBasicMessage;
import org.hawkular.bus.common.producer.ProducerConnectionContext;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

public class QueueSendServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final static String CONN_FACTORY = "/HawkularBusConnectionFactory";
    private final static String QUEUE_NAME = "ExampleQueueName"; // the full name is "java:/queue/ExampleQueueName"

    private final static Map<String, String> FNF_HEADER = createMyFilterHeader("fnf");
    private final static Map<String, String> RPC_HEADER = createMyFilterHeader("rpc");

    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String userMessage = request.getParameter("jmsMessageFNF");
        if (userMessage != null) {
            fireAndForget(request, response, userMessage);
        } else {
            userMessage = request.getParameter("jmsMessageRPC");
            if (userMessage != null) {
                rpc(request, response, userMessage);
            } else {
                throw new ServletException("Don't know what to send!");
            }
        }
    }

    protected void fireAndForget(HttpServletRequest request, HttpServletResponse response, String userMessage) {
        try {
            InitialContext ctx = new InitialContext();
            QueueConnectionFactory qconFactory = (QueueConnectionFactory) ctx.lookup(CONN_FACTORY);

            ConnectionContextFactory ccf = new ConnectionContextFactory(qconFactory);
            ProducerConnectionContext pcc = ccf.createProducerConnectionContext(new Endpoint(Endpoint.Type.QUEUE, QUEUE_NAME));

            SimpleBasicMessage msg = new SimpleBasicMessage(userMessage);
            MessageId mid = new MessageProcessor().send(pcc, msg, FNF_HEADER);

            PrintWriter out = response.getWriter();
            out.println("<h1>Fire and Forget</h1>");
            out.println("<p>Message Sent [" + msg + "]</p>");
            out.println("<p>(messageId=" + mid + ")</p>");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void rpc(HttpServletRequest request, HttpServletResponse response, String userMessage) {
        try {
            InitialContext ctx = new InitialContext();
            QueueConnectionFactory qconFactory = (QueueConnectionFactory) ctx.lookup(CONN_FACTORY);

            ConnectionContextFactory ccf = new ConnectionContextFactory(qconFactory);
            ProducerConnectionContext pcc = ccf.createProducerConnectionContext(new Endpoint(Endpoint.Type.QUEUE, QUEUE_NAME));

            SimpleBasicMessage msg = new SimpleBasicMessage(userMessage);
            ListenableFuture<SimpleBasicMessage> future = new MessageProcessor().sendRPC(pcc, msg, SimpleBasicMessage.class, RPC_HEADER);
            Futures.addCallback(future, new SimpleFutureCallback());

            PrintWriter out = response.getWriter();
            out.println("<h1>RPC</h1>");
            out.println("<p>Message Sent [" + msg + "]</p>");
            out.println("<p>Check server logs for response.</p>");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // return the header that our sample MDBs' selectors will look at
    private static Map<String, String> createMyFilterHeader(String value) {
        Map<String, String> map = new HashMap<String, String>(1);
        map.put("MyFilter", value);
        return map;
    }

    private class SimpleFutureCallback implements FutureCallback<SimpleBasicMessage> {
        @Override
        public void onSuccess(SimpleBasicMessage result) {
            log("SUCCESS! Got response from MDB: " + result);
        }

        @Override
        public void onFailure(Throwable t) {
            log("FAILURE! Did not get response from MDB", t);
        }
    }
}
