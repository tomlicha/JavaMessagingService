import javafx.application.Platform;
import model.ContentEnricher;
import model.LoanReply;
import model.LoanRequest;
import model.LoanSerializer;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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
                        System.out.println("loan request created"+((TextMessage) msg).getText());

                        LoanRequest loanRequest = loanSerializer.requestFromString(((TextMessage) msg).getText());
                        ContentEnricher contentEnricher = new ContentEnricher();
                        String result = contentEnricher.GetRequest(loanRequest.getSsn());
                        JSONObject jsonObj = new JSONObject(result);
                        loanRequest.setCredit(jsonObj.getInt("creditScore"));
                        loanRequest.setHistory(jsonObj.getInt("history"));
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
