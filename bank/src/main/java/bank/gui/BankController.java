package bank.gui;

import bank.model.BankInterestReply;
import bank.model.BankInterestRequest;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import org.json.JSONObject;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import java.util.ResourceBundle;

public class BankController implements Initializable {
    private final String BANK_ID = "ABN";

    public ListView<ListViewLine> lvBankRequestReply;
    public TextField tfInterest;

    Destination sendDestinationBank; // reference to a queue/topic destination
    Destination replyDestinationBank; // reference to a queue/topic destination

    Connection connection; // to connect to the ActiveMQ
    Session session; // session for creating messages, producers and

    MessageProducer producer; // for sending messages
    MessageConsumer consumer; // for receiving messages

    public static String myRequestQueueBank="BrokerToBank";
    public static String myReplyQueueBank="BankToBroker";

    HashMap<ListViewLine,Message> hmap = new HashMap<>();


    @FXML
    public void btnSendBankInterestReplyClicked() throws JMSException {
        double interest = Double.parseDouble(tfInterest.getText());
        BankInterestReply bankInterestReply = new BankInterestReply(interest, BANK_ID);
        // TO DO: send a message with bankInterestReply
        ListViewLine selected = lvBankRequestReply.getSelectionModel().getSelectedItem();
        System.out.println("selected message :" + selected);
        Message requestMsg = hmap.get(selected);
        selected.setBankInterestReply(bankInterestReply);
        lvBankRequestReply.refresh();
        System.out.println("request message :" + requestMsg);
        String jsonString = new JSONObject()
                .put("interest", interest)
                .put("BANK_ID", BANK_ID).toString();
        // create a text message
        Message banktobrokermessage = session.createTextMessage(jsonString);
        banktobrokermessage.setJMSCorrelationID(requestMsg.getJMSCorrelationID());
        System.out.println("\nmessage to be sent:"+banktobrokermessage);
        replyDestinationBank =requestMsg.getJMSReplyTo();
        producer.send(requestMsg.getJMSReplyTo(),banktobrokermessage);


    }

    /**
     * This method returns the line of lvMessages which contains the given loan request.
     * @param request BankInterestRequest for which the line of lvMessages should be found and returned
     * @return The ListView line of lvMessages which contains the given request
     */
    private ListViewLine getRequestReply(BankInterestRequest request) {

        for (int i = 0; i < lvBankRequestReply.getItems().size(); i++) {
            ListViewLine rr =  lvBankRequestReply.getItems().get(i);
            if (rr.getBankInterestRequest() != null && rr.getBankInterestRequest() == request) {
                return rr;
            }
        }
        return null;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            Properties props = new Properties();
            props.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                    "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
            props.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");
            // connect to the Destination called “myFirstChannel”
            // queue or topic: “queue.myFirstDestination” or “topic.myFirstDestination”
            props.put(("queue." + myRequestQueueBank), myRequestQueueBank);
            props.put(("queue." + myReplyQueueBank), myReplyQueueBank);


            Context jndiContext = new InitialContext(props);
            ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext
                    .lookup("ConnectionFactory");
            connection = connectionFactory.createConnection();

            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            // connect to the sender destination
            producer = session.createProducer(null);
            // connect to the receiver destination

            sendDestinationBank = (Destination) jndiContext.lookup(myRequestQueueBank);
            replyDestinationBank = (Destination) jndiContext.lookup(myReplyQueueBank);

            consumer = session.createConsumer(sendDestinationBank);
            consumer.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message msg) {
                    try {
                        JSONObject json = new JSONObject(((TextMessage) msg).getText());
                        System.out.println("\njson received: " + json+"\n");
                        System.out.println("reply queue: " + msg.getJMSReplyTo()+"\n");
                        Integer time = json.getInt("time");
                        Integer amount = json.getInt("amount");
                        BankInterestRequest bankInterestRequest = new BankInterestRequest(amount, time);
                        System.out.println("\nnew object :time:"+bankInterestRequest.getTime()+"\n"+"amount:"+bankInterestRequest.getAmount());
                        ListViewLine listViewLine = new ListViewLine(bankInterestRequest);
                        lvBankRequestReply.getItems().add(listViewLine);
                        System.out.println("hashmap input :"+bankInterestRequest.toString()+" & "+msg);
                        hmap.put(listViewLine,msg);

                    } catch (JMSException e) {
                        e.printStackTrace();


                    }
                }});
            connection.start(); // this is needed to start receiving messages
        } catch (NamingException | JMSException e) {
            e.printStackTrace();
        }
    }
}
