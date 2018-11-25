package loanclient.gui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import loanclient.model.*;


import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

public class LoanClientController implements Initializable {


    public TextField tfSsn;
    public TextField tfAmount;
    public TextField tfTime;
    public ListView<ListViewLine> lvLoanRequestReply;
    HashMap<String,LoanRequest> hmap = new HashMap<>();

    public static String myReplyQueue="BrokerToClient";
    public static String myRequestQueue="ClientToBroker";

    MessageSenderGateway messageSenderGatewayBroker = new MessageSenderGateway(myRequestQueue);
    MessageReceiverGateway messageReceiverGatewayBroker = new MessageReceiverGateway(myReplyQueue);

    LoanSerializer loanSerializer = new LoanSerializer();

    String jsonString;


    @FXML
    public void btnSendLoanRequestClicked() throws JMSException {
        // create the BankInterestRequest
        int ssn = Integer.parseInt(tfSsn.getText());
        int amount = Integer.parseInt(tfAmount.getText());
        int time = Integer.parseInt(tfTime.getText());
        LoanRequest loanRequest = new LoanRequest(ssn, amount, time);

        //create the ListView line with the request and add it to lvLoanRequestReply
        ListViewLine listViewLine = new ListViewLine(loanRequest);
        lvLoanRequestReply.getItems().add(listViewLine);

        // to do: send the message with this loanRequest...
        Message msg = messageSenderGatewayBroker.createTextMessage(loanSerializer.RequestToString(loanRequest));
        // print all message attributes; but JMSDestination is null
        // session makes the message via MctiveMQ. AtiveMQ assigns unique JMSMessageID
        // to each message.
        System.out.println(msg);

        messageSenderGatewayBroker.send(msg);

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

            messageReceiverGatewayBroker.setListener(new MessageListener() {
                @Override
                public void onMessage(Message msg) {
                    try {
                        System.out.println("\nloan reply :"+msg);

                        ListViewLine listViewLine = getRequestReply(hmap.get(msg.getJMSCorrelationID()));
                        listViewLine.setLoanReply(loanSerializer.replyFromString(((TextMessage) msg).getText()));
                        lvLoanRequestReply.refresh();

                    } catch (JMSException e) {
                        e.printStackTrace();


                    }
                }});
            messageReceiverGatewayBroker.start();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
