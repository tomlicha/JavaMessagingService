import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import model.BankSerializer;
import model.LoanReply;
import model.LoanRequest;
import model.LoanSerializer;
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

    MessageSenderGateway messageSenderGatewayBank = new MessageSenderGateway(myRequestQueueBank);
    MessageSenderGateway messageSenderGatewayClient = new MessageSenderGateway(myReplyQueueClient);

    MessageReceiverGateway messageReceiverGatewayClient = new MessageReceiverGateway(myRequestQueueClient);
    MessageReceiverGateway messageReceiverGatewayBank = new MessageReceiverGateway(myReplyQueueBank);

    LoanSerializer loanSerializer = new LoanSerializer();
    BankSerializer bankSerializer = new BankSerializer();

    HashMap<String,ListViewLine> hmap = new HashMap<>();


    public BrokerController() {
        try {

            messageReceiverGatewayClient.setListener(new MessageListener() {
                @Override
                public void onMessage(Message msg) {
                    try {
                        String id=msg.getJMSMessageID();

                        LoanRequest loanRequest = loanSerializer.requestFromString(((TextMessage) msg).getText());

                        ListViewLine listViewLine = new ListViewLine(loanRequest);

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                lvBrokerRequestReply.getItems().add(listViewLine);

                            }
                        });
                        System.out.println("\nhmap input: " + id+" & "+listViewLine+"\n");

                        hmap.put(id,listViewLine);

                        String jsonString =  loanSerializer.RequestToString(loanRequest);

                        Message bankmsg = messageSenderGatewayBank.createTextMessage(jsonString);

                        bankmsg.setJMSCorrelationID(msg.getJMSMessageID());

                        messageSenderGatewayBank.send(bankmsg);
                        System.out.println("\nmessage sent:\n"+bankmsg);
                    } catch (JMSException e) {
                        e.printStackTrace();


                }
            }});
            messageReceiverGatewayClient.start();

            messageReceiverGatewayBank.setListener(new MessageListener() {
                @Override
                public void onMessage(Message msg) {
                    try {


                        LoanReply loanReply = loanSerializer.replyFromString(((TextMessage) msg).getText());

                        ListViewLine listViewLine = hmap.get(msg.getJMSCorrelationID());

                        listViewLine.setLoanReply(loanReply);

                        lvBrokerRequestReply.refresh();

                        String jsonString = loanSerializer.replyToString(loanReply);

                        // create a text message
                        Message clientMessage = messageSenderGatewayClient.createTextMessage(jsonString);

                        clientMessage.setJMSCorrelationID(msg.getJMSCorrelationID());
                        messageSenderGatewayClient.send(clientMessage);
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }
                }});
            messageReceiverGatewayBank.start(); // this is needed to start receiving messages
        } catch (JMSException e) {
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
