import javafx.application.Platform;
import model.*;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.HashMap;

public abstract class BankAppGateway {

    public static String myRequestQueueBank="BrokerToBank";
    public static String myReplyQueueBank="BankToBroker";

    MessageSenderGateway messageSenderGatewayBank = new MessageSenderGateway(myRequestQueueBank);

    MessageReceiverGateway messageReceiverGatewayBank = new MessageReceiverGateway(myReplyQueueBank);

    HashMap<String,BankInterestRequest> hmap = new HashMap<>();

    BankSerializer bankSerializer = new BankSerializer();

    public BankAppGateway() {
        try {

            messageReceiverGatewayBank.setListener(new MessageListener() {
                @Override
                public void onMessage(Message msg) {
                    try {
                        System.out.println("message received from bank:_n"+msg);
                        BankInterestReply bankInterestReply = bankSerializer.replyFromString(((TextMessage) msg).getText());
                        BankInterestRequest bankInterestRequest = hmap.get(msg.getJMSCorrelationID());

                        onBankInterestReplyArrived(bankInterestReply,bankInterestRequest);

                    } catch (JMSException e) {
                        e.printStackTrace();


                    }
                }});
            messageReceiverGatewayBank.start();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void sendLoanReply(BankInterestRequest bankInterestRequest) throws JMSException {
        String jsonString = bankSerializer.RequestToString(bankInterestRequest);

        // create a text message
        Message clientMessage = messageSenderGatewayBank.createTextMessage(jsonString);

        messageSenderGatewayBank.send(clientMessage);

        hmap.put(clientMessage.getJMSMessageID(),bankInterestRequest);
    }

    public abstract void onBankInterestReplyArrived(BankInterestReply bankInterestReply,BankInterestRequest bankInterestRequest);
}
