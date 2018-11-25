package loanclient.model;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

public class MessageReceiverGateway {

    Connection connection; // to connect to the ActiveMQ
    Session session; // session for creating messages, producers and
    MessageConsumer consumer; // for sending messages
    Destination receiveDestination; // reference to a queue/topic destination


    public MessageReceiverGateway(String channelName) {
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

            // connect to the receiver destination
            receiveDestination = (Destination) jndiContext.lookup(channelName);

            // connect to the consumer destination
            consumer = session.createConsumer(receiveDestination);



        } catch (NamingException | JMSException e) {
            e.printStackTrace();
        }
    }


    public void setListener (MessageListener messageListener) throws JMSException {
        consumer.setMessageListener(messageListener);
    }

    public void start() throws JMSException {
        connection.start();
    }
}
