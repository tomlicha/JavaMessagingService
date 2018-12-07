import javafx.application.Platform;
import model.*;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.HashMap;

public abstract class BankAppGateway {

    public static String myRequestQueueBankING="BrokerToING";

    public static String myRequestQueueBankABN="BrokerToABN";

    public static String myRequestQueueBankRabo="BrokerToRabo";

    public static String myReplyQueueBank="BankToBroker";

    MessageSenderGateway messageSenderGatewayBankING = new MessageSenderGateway(myRequestQueueBankING);

    MessageReceiverGateway messageReceiverGatewayBank = new MessageReceiverGateway(myReplyQueueBank);

    MessageSenderGateway messageSenderGatewayBankABN = new MessageSenderGateway(myRequestQueueBankABN);

    MessageSenderGateway messageSenderGatewayBankRabo = new MessageSenderGateway(myRequestQueueBankRabo);

    HashMap<String,BankInterestRequest> hmap = new HashMap<>();

    HashMap<Integer,BankReplyAggregate> hmapAggregation = new HashMap<>();

    BankSerializer bankSerializer = new BankSerializer();
    private int counter;

    public BankAppGateway() {
        try {

            messageReceiverGatewayBank.setListener(new MessageListener() {
                @Override
                public void onMessage(Message msg) {
                    try {
                        int aggregationID = msg.getIntProperty("aggregationID");
                        System.out.println("message received from bank ING:\n"+msg);
                        BankInterestReply bankInterestReply = bankSerializer.replyFromString(((TextMessage) msg).getText());
                        BankReplyAggregate bankReplyAggregate = hmapAggregation.get(aggregationID);
                        bankReplyAggregate.addReply(bankInterestReply);
                        if(bankReplyAggregate.isFinished() == true){
                            BankInterestRequest bankInterestRequest = hmap.get(msg.getJMSCorrelationID());
                            System.out.println("request :"+bankInterestRequest+" reply:"+bankInterestReply);
                            onBankInterestReplyArrived(bankReplyAggregate.getBest(),bankInterestRequest);
                        }


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
        BankReplyAggregate bankReplyAggregate = new BankReplyAggregate();
        int messagesSent=0;
        counter+=1;

        System.out.println("request to be sent:"+jsonString);
        if (bankInterestRequest.getAmount() <= 100000 && bankInterestRequest.getTime() <= 10){
            // create a text message
            Message clientMessage = messageSenderGatewayBankING.createTextMessage(jsonString);
            clientMessage.setIntProperty("aggregationID",counter);
            messageSenderGatewayBankING.send(clientMessage);
            hmap.put(clientMessage.getJMSMessageID(),bankInterestRequest);
            messagesSent+=1;

        }
        if (bankInterestRequest.getAmount() >= 200000 && bankInterestRequest.getAmount() <= 300000 && bankInterestRequest.getTime() <=20){
            // create a text message
            Message clientMessage = messageSenderGatewayBankABN.createTextMessage(jsonString);
            clientMessage.setIntProperty("aggregationID",counter);
            messageSenderGatewayBankABN.send(clientMessage);
            hmap.put(clientMessage.getJMSMessageID(),bankInterestRequest);
            messagesSent+=1;


        }
        if (bankInterestRequest.getAmount() <= 250000 && bankInterestRequest.getTime() <= 15){
            // create a text message
            Message clientMessage = messageSenderGatewayBankRabo.createTextMessage(jsonString);
            clientMessage.setIntProperty("aggregationID",counter);
            messageSenderGatewayBankRabo.send(clientMessage);
            hmap.put(clientMessage.getJMSMessageID(),bankInterestRequest);
            messagesSent+=1;

        }
        else {
            System.out.println("no bank accepted the loan request");
        }
        bankReplyAggregate.setNumberRequestSent(messagesSent);
        hmapAggregation.put(counter, bankReplyAggregate);
    }

    public abstract void onBankInterestReplyArrived(BankInterestReply bankInterestReply,BankInterestRequest bankInterestRequest);
}
