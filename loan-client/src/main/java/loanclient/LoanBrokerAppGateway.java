package loanclient;

import loanclient.model.*;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.HashMap;

public abstract class LoanBrokerAppGateway {

    MessageSenderGateway messageSenderGatewayBroker = new MessageSenderGateway(myRequestQueue);
    MessageReceiverGateway messageReceiverGatewayBroker = new MessageReceiverGateway(myReplyQueue);
    public static String myReplyQueue="BrokerToClient";
    public static String myRequestQueue="ClientToBroker";
    HashMap<String,LoanRequest> hmap = new HashMap<>();

    LoanSerializer loanSerializer = new LoanSerializer();

    public LoanBrokerAppGateway() {
        try {

            messageReceiverGatewayBroker.setListener(new MessageListener() {
                @Override
                public void onMessage(Message msg) {
                    try {
                        System.out.println("\nloan reply :"+msg);
                        LoanReply loanReply = loanSerializer.replyFromString(((TextMessage) msg).getText());
                        LoanRequest loanRequest = (hmap.get(msg.getJMSCorrelationID()));
                        onLoanReplyArrived(loanRequest,loanReply);


                    } catch (JMSException e) {
                        e.printStackTrace();


                    }
                }});
            messageReceiverGatewayBroker.start();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void applyForLoan(LoanRequest loanRequest) throws JMSException {
        Message msg = messageSenderGatewayBroker.createTextMessage(loanSerializer.RequestToString(loanRequest));
        // print all message attributes; but JMSDestination is null
        // session makes the message via MctiveMQ. AtiveMQ assigns unique JMSMessageID
        // to each message.
        System.out.println(msg);

        messageSenderGatewayBroker.send(msg);

        //System.out.println("id :" + msg.getJMSMessageID() + "\n" + "body:" + body + "\n");
        hmap.put(msg.getJMSMessageID(),loanRequest);
    }

    public abstract void onLoanReplyArrived(LoanRequest loanRequest, LoanReply loanReply);
}
