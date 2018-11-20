package loanclient.gui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import loanclient.model.LoanReply;
import loanclient.model.LoanRequest;
import org.json.JSONObject;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import java.util.ResourceBundle;

public class LoanClientController implements Initializable {


    public TextField tfSsn;
    public TextField tfAmount;
    public TextField tfTime;
    public ListView<ListViewLine> lvLoanRequestReply;
    HashMap<String,LoanRequest> hmap = new HashMap<>();


    Context jndiContext;

    public static String myReplyQueue="BrokerToClient";
    public static String myRequestQueue="ClientToBroker";

    Destination sendDestination; // reference to a queue/topic destination
    Destination replyDestination;
    Connection connection; // to connect to the ActiveMQ
    Session session; // session for creating messages, producers and
    Destination receiveDestination; // reference to a queue/topic destination
    MessageProducer producer; // for sending messages
    MessageConsumer consumer; // for receiving messages
    String jsonString;


    @FXML
    public void btnSendLoanRequestClicked() throws JMSException {
        // create the BankInterestRequest
        int ssn = Integer.parseInt(tfSsn.getText());
        int amount = Integer.parseInt(tfAmount.getText());
        int time = Integer.parseInt(tfTime.getText());
        LoanRequest loanRequest = new LoanRequest(ssn, amount, time);
        jsonString = new JSONObject()
                .put("ssn", ssn)
                .put("amount", amount)
                .put("time", time).toString();
        //create the ListView line with the request and add it to lvLoanRequestReply
        ListViewLine listViewLine = new ListViewLine(loanRequest);
        lvLoanRequestReply.getItems().add(listViewLine);

        // to do: send the message with this loanRequest...
        Message msg = session.createTextMessage(jsonString);
        // print all message attributes; but JMSDestination is null
        // session makes the message via MctiveMQ. AtiveMQ assigns unique JMSMessageID
        // to each message.
        System.out.println(msg);
        msg.setJMSReplyTo(replyDestination);
        producer.send(sendDestination, msg);

        //System.out.println("id :" + msg.getJMSMessageID() + "\n" + "body:" + body + "\n");
        hmap.put(msg.getJMSMessageID(),loanRequest);
        //print all message attributes; but JMSDestination is senderDestination name
        System.out.println(msg);
        System.out.println("JMSMessageID=" + msg.getJMSMessageID()
                + " JMSDestination=" + msg.getJMSDestination()
                + " Text=" + ((TextMessage) msg).getText());
    }


    /**
     * This method returns the line of lvMessages which contains the given loan request.
     * @param request BankInterestRequest for which the line of lvMessages should be found and returned
     * @return The ListView line of lvMessages which contains the given request
     */
    private ListViewLine getRequestReply(LoanRequest request) {

        for (int i = 0; i < lvLoanRequestReply.getItems().size(); i++) {
            ListViewLine rr =  lvLoanRequestReply.getItems().get(i);
            if (rr.getLoanRequest() != null && rr.getLoanRequest() == request) {
                return rr;
            }
        }

        return null;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tfSsn.setText("123456");
        tfAmount.setText("80000");
        tfTime.setText("30");
        try {
            Properties props = new Properties();
            props.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                    "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
            props.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");
            // connect to the Destination called “myFirstChannel”
            // queue or topic: “queue.myFirstDestination” or “topic.myFirstDestination”
            props.put(("queue." + myReplyQueue), myReplyQueue);
            props.put(("queue." + myRequestQueue), myRequestQueue);

            jndiContext = new InitialContext(props);
            ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext
                    .lookup("ConnectionFactory");
            connection = connectionFactory.createConnection();

            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            // connect to the receiver destination
            receiveDestination = (Destination) jndiContext.lookup(myReplyQueue);
            sendDestination = (Destination) jndiContext.lookup(myRequestQueue);
            consumer = session.createConsumer(receiveDestination);
            producer = session.createProducer(null);
            // create a text message
            consumer.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message msg) {
                    try {
                        System.out.println("\nloan reply :"+msg);
                        JSONObject json = new JSONObject(((TextMessage) msg).getText());
                        System.out.println("\njson received: " + json+"\n");
                        System.out.println("reply queue: " + msg.getJMSReplyTo()+"\n");
                        Integer interest = json.getInt("interest");
                        String BANK_ID = json.getString("BANK_ID");
                        LoanReply loanReply = new LoanReply(interest,BANK_ID);

                        ListViewLine listViewLine = getRequestReply(hmap.get(msg.getJMSCorrelationID()));
                        listViewLine.setLoanReply(loanReply);
                        lvLoanRequestReply.refresh();

                    } catch (JMSException e) {
                        e.printStackTrace();


                    }
                }});
            connection.start();
        } catch (JMSException | NamingException e) {
            e.printStackTrace();
        }
    }
}
