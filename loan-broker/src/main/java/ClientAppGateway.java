import javafx.application.Platform;
import model.LoanReply;
import model.LoanRequest;
import model.LoanSerializer;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.HashMap;

public abstract class ClientAppGateway {

    public static String myReplyQueueClient="BrokerToClient";
    public static String myRequestQueueClient="ClientToBroker";

    MessageSenderGateway messageSenderGatewayClient = new MessageSenderGateway(myReplyQueueClient);
    MessageReceiverGateway messageReceiverGatewayClient = new MessageReceiverGateway(myRequestQueueClient);

    HashMap<LoanRequest, Message> hmap = new HashMap<>();

    LoanSerializer loanSerializer = new LoanSerializer();

    public ClientAppGateway() {
        try {

            messageReceiverGatewayClient.setListener(new MessageListener() {
                @Override
                public void onMessage(Message msg) {
                    try {

                        LoanRequest loanRequest = loanSerializer.requestFromString(((TextMessage) msg).getText());

                        onLoanRequestArrived(loanRequest);

                        hmap.put(loanRequest,msg);
                    } catch (JMSException e) {
                        e.printStackTrace();


                    }
                }});
            messageReceiverGatewayClient.start();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void sendLoanReply(LoanReply loanReply, LoanRequest loanRequest) throws JMSException {
        String jsonString = loanSerializer.replyToString(loanReply);

        // create a text message
        Message clientMessage = messageSenderGatewayClient.createTextMessage(jsonString);

        clientMessage.setJMSCorrelationID(hmap.get(loanRequest).getJMSMessageID());
        messageSenderGatewayClient.send(clientMessage);
        hmap.remove(loanRequest);
    }

    public abstract void onLoanRequestArrived(LoanRequest loanRequest);
}
