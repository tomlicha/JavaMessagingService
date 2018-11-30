package bank;

import bank.gui.ListViewLine;
import bank.model.*;
import javafx.application.Platform;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.HashMap;

public abstract class BankBrokerAppGateway {

    public static String myRequestQueueBank="BrokerToBank";
    public static String myReplyQueueBank="BankToBroker";

    MessageSenderGateway messageSenderGatewayBroker = new MessageSenderGateway(myReplyQueueBank);
    MessageReceiverGateway messageReceiverGatewayBroker = new MessageReceiverGateway(myRequestQueueBank);

    BankSerializer bankSerializer = new BankSerializer();

    HashMap<ListViewLine,Message> hmap = new HashMap<>();

    public BankBrokerAppGateway() {
        try {

            messageReceiverGatewayBroker.setListener(new MessageListener() {
                @Override
                public void onMessage(Message msg) {
                 try {
                    BankInterestRequest bankInterestRequest = bankSerializer.requestFromString(((TextMessage) msg).getText());
                    System.out.println("\nnew object :time:"+bankInterestRequest.getTime()+"\n"+"amount:"+bankInterestRequest.getAmount());
                    ListViewLine listViewLine = new ListViewLine(bankInterestRequest);
                    hmap.put(listViewLine,msg);
                    onBankRequestArrived(bankInterestRequest,listViewLine);

                } catch (JMSException e) {
                    e.printStackTrace();


                }
                }});
            messageReceiverGatewayBroker.start();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void applyForBank(BankInterestReply bankInterestReply, ListViewLine selected) throws JMSException {
        Message requestMsg = hmap.get(selected);

        System.out.println("request message :" + requestMsg);
        String jsonString =bankSerializer.replyToString(bankInterestReply);
        // create a text message
        Message banktobrokermessage = messageSenderGatewayBroker.createTextMessage(jsonString);
        banktobrokermessage.setJMSCorrelationID(requestMsg.getJMSMessageID());
        messageSenderGatewayBroker.send(banktobrokermessage);
        System.out.println("\nmessage sent:\n"+banktobrokermessage);

    }

    public abstract void onBankRequestArrived(BankInterestRequest bankInterestRequest, ListViewLine listViewLine);
}
