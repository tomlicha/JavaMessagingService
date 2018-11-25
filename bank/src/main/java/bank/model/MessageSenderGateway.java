package bank.model;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

public class MessageSenderGateway {

    Connection connection; // to connect to the ActiveMQ
    Session session; // session for creating messages, producers and
    MessageProducer producer; // for sending messages
    Destination sendDestination; // reference to a queue/topic destination


    public MessageSenderGateway(String channelName) {
        try {
            Properties props = new Properties();
            props.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                    "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
            props.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");
            // connect to the Destination called “myFirstChannel”

            // queue or topic: “queue.myFirstDestination” or “topic.myFirstDestination”
            props.put(("queue." + channelName), channelName);

            Context jndiContext = new InitialContext(props);
            ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext
                    .lookup("ConnectionFactory");
            connection = connectionFactory.createConnection();

            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // connect to the sender destination
            producer = session.createProducer(null);

            // connect to the receiver destination
            sendDestination = (Destination) jndiContext.lookup(channelName);


        } catch (NamingException | JMSException e) {
            e.printStackTrace();
        }
    }
    public Message createTextMessage(String body) throws JMSException {
            Message msg = session.createTextMessage(body);

            return msg;
    }

    public void send (Message msg) throws JMSException {
        producer.send(sendDestination,msg);
    }
}
