import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import model.LoanReply;
import model.LoanRequest;
import org.json.JSONObject;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import java.util.ResourceBundle;

public class BrokerController implements Initializable {


    public ListView<ListViewLine> lvBrokerRequestReply;
    Context jndiContext;
    public static String myReplyQueueClient="BrokerToClient";
    public static String myRequestQueueClient="ClientToBroker";
    public static String myRequestQueueBank="BrokerToBank";
    public static String myReplyQueueBank="BankToBroker";

    Destination replyDestinationClient; // reference to a queue/topic destination
    Destination receiveDestinationClient;
    Destination sendDestinationBank; // reference to a queue/topic destination
    Destination replyDestinationBank; // reference to a queue/topic destination

    Connection connection; // to connect to the ActiveMQ
    Session session; // session for creating messages, producers and

    MessageProducer producerBank; // for sending messages
    MessageConsumer consumerBank; // for receiving messages

    MessageProducer producerClient; // for sending messages
    MessageConsumer consumerClient; // for receiving messages

    HashMap<String,ListViewLine> hmap = new HashMap<>();


    public BrokerController() {
        try {
            Properties props = new Properties();
            props.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                    "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
            props.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");
            // connect to the Destination called “myFirstChannel”

            // queue or topic: “queue.myFirstDestination” or “topic.myFirstDestination”
            props.put(("queue." + myRequestQueueClient), myRequestQueueClient);
            props.put(("queue." + myReplyQueueClient), myReplyQueueClient);
            props.put(("queue." + myRequestQueueBank), myRequestQueueBank);
            props.put(("queue." + myReplyQueueBank), myReplyQueueBank);

            Context jndiContext = new InitialContext(props);
            ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext
                    .lookup("ConnectionFactory");
            connection = connectionFactory.createConnection();

            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // connect to the sender destination
            producerClient = session.createProducer(null);
            producerBank = session.createProducer(null);

            // connect to the receiver destination
            receiveDestinationClient = (Destination) jndiContext.lookup(myRequestQueueClient);
            replyDestinationClient = (Destination) jndiContext.lookup(myReplyQueueClient);
            sendDestinationBank = (Destination) jndiContext.lookup(myRequestQueueBank);
            replyDestinationBank = (Destination) jndiContext.lookup(myReplyQueueBank);

            consumerClient = session.createConsumer(receiveDestinationClient);
            consumerBank = session.createConsumer(replyDestinationBank);

            consumerClient.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message msg) {
                    try {
                        String id=msg.getJMSMessageID();
                        JSONObject json = new JSONObject(((TextMessage) msg).getText());
                        System.out.println("\njson received: " + json+"\n");
                        System.out.println("reply queue: " + msg.getJMSReplyTo()+"\n");
                        Integer ssn = json.getInt("ssn");
                        Integer time = json.getInt("time");
                        Integer amount = json.getInt("amount");
                        LoanRequest loanRequest = new LoanRequest(ssn, amount, time);
                        System.out.println("\nnew object : ssn :"+loanRequest.getSsn()+"\n"+"time:"+loanRequest.getTime()+"\n"+"amount:"+loanRequest.getAmount());
                        ListViewLine listViewLine = new ListViewLine(loanRequest);
                        lvBrokerRequestReply.getItems().add(listViewLine);
                        System.out.println("\nhmap input: " + id+" & "+listViewLine+"\n");

                        hmap.put(id,listViewLine);

                        String jsonString = new JSONObject()
                                .put("amount", amount)
                                .put("time", time).toString();
                        // create a text message
                        Message bankmsg = session.createTextMessage(jsonString);
                        // print all message attributes; but JMSDestination is null
                        // session makes the message via MctiveMQ. AtiveMQ assigns unique JMSMessageID
                        // to each message.
                        bankmsg.setJMSCorrelationID(msg.getJMSMessageID());
                        bankmsg.setJMSReplyTo(replyDestinationBank);
                        producerBank.send(sendDestinationBank, bankmsg);
                        System.out.println("\nmessage sent:\n"+bankmsg);
                    } catch (JMSException e) {
                        e.printStackTrace();


                }
            }});

            consumerBank.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message msg) {
                    try {
                        System.out.println("map content:\n");
                        for (String key : hmap.keySet()) {
                            System.out.println(key);
                        }
                        System.out.println("\nmessage received from bank:\n"+msg);
                        JSONObject json = new JSONObject(((TextMessage) msg).getText());
                        System.out.println("\njson received: " + json+"\n");
                        Double interest = json.getDouble("interest");
                        String BANK_ID = json.getString("BANK_ID");
                        LoanReply loanReply = new LoanReply(interest,BANK_ID);
                        System.out.println("\nnew object : interest:"+loanReply.getInterest()+"\n"+"BANK_ID:"+loanReply.getQuoteID());
                        ListViewLine listViewLine = hmap.get(msg.getJMSCorrelationID());
                        System.out.println(listViewLine);
                        listViewLine.setLoanReply(loanReply);
                        lvBrokerRequestReply.refresh();
                        String jsonString = new JSONObject()
                                .put("interest", interest)
                                .put("BANK_ID", BANK_ID).toString();
                        // create a text message
                        Message clientMessage = session.createTextMessage(jsonString);
                        // print all message attributes; but JMSDestination is null
                        // session makes the message via MctiveMQ. AtiveMQ assigns unique JMSMessageID
                        // to each message.
                        clientMessage.setJMSCorrelationID(msg.getJMSCorrelationID());
                        producerClient.send(replyDestinationClient, clientMessage);
                        System.out.println("\nmessage sent:\n"+clientMessage);
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }
                }});
            connection.start(); // this is needed to start receiving messages
        } catch (NamingException | JMSException e) {
            e.printStackTrace();
        }

        }
    /**
     * This method returns the line of lvMessages which contains the given loan request.
     * @param request BankInterestRequest for which the line of lvMessages should be found and returned
     * @return The ListView line of lvMessages which contains the given request
     */
    private ListViewLine getRequestReply(LoanRequest request) {

        for (int i = 0; i < lvBrokerRequestReply.getItems().size(); i++) {
            ListViewLine rr =  lvBrokerRequestReply.getItems().get(i);
            if (rr.getLoanRequest() != null && rr.getLoanRequest() == request) {
                return rr;
            }
        }

        return null;
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
