package bank.gui;

import bank.model.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

public class BankController implements Initializable {
    private final String BANK_ID = "ABN";

    public ListView<ListViewLine> lvBankRequestReply;
    public TextField tfInterest;

    public static String myRequestQueueBank="BrokerToBank";
    public static String myReplyQueueBank="BankToBroker";

    MessageSenderGateway messageSenderGatewayBroker = new MessageSenderGateway(myReplyQueueBank);
    MessageReceiverGateway messageReceiverGatewayBroker = new MessageReceiverGateway(myRequestQueueBank);

    BankSerializer bankSerializer = new BankSerializer();

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
        String jsonString =bankSerializer.replyToString(bankInterestReply);
        // create a text message
        Message banktobrokermessage = messageSenderGatewayBroker.createTextMessage(jsonString);
        banktobrokermessage.setJMSCorrelationID(requestMsg.getJMSCorrelationID());
        System.out.println("\nmessage to be sent:"+banktobrokermessage);
        messageSenderGatewayBroker.send(banktobrokermessage);


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
            messageReceiverGatewayBroker.setListener(new MessageListener() {
                @Override
                public void onMessage(Message msg) {
                    try {
                        BankInterestRequest bankInterestRequest = bankSerializer.requestFromString(((TextMessage) msg).getText());
                        System.out.println("\nnew object :time:"+bankInterestRequest.getTime()+"\n"+"amount:"+bankInterestRequest.getAmount());
                        ListViewLine listViewLine = new ListViewLine(bankInterestRequest);
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                lvBankRequestReply.getItems().add(listViewLine);

                            }
                        });
                        System.out.println("hashmap input :"+bankInterestRequest.toString()+" & "+msg);
                        hmap.put(listViewLine,msg);

                    } catch (JMSException e) {
                        e.printStackTrace();


                    }
                }});
            messageReceiverGatewayBroker.start(); // this is needed to start receiving messages
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    void onBankReplyArrived(BankInterestReply bankInterestReply, BankInterestRequest bankInterestRequest){

    }


}
