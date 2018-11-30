package loanclient.gui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import loanclient.LoanBrokerAppGateway;
import loanclient.model.LoanReply;
import loanclient.model.LoanRequest;

import javax.jms.JMSException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoanClientController implements Initializable {


    public TextField tfSsn;
    public TextField tfAmount;
    public TextField tfTime;
    public ListView<ListViewLine> lvLoanRequestReply;
    private LoanBrokerAppGateway loanBrokerAppGateway;

    @FXML
    public void btnSendLoanRequestClicked() {
        // create the BankInterestRequest
        int ssn = Integer.parseInt(tfSsn.getText());
        int amount = Integer.parseInt(tfAmount.getText());
        int time = Integer.parseInt(tfTime.getText());
        LoanRequest loanRequest = new LoanRequest(ssn, amount, time);

        //create the ListView line with the request and add it to lvLoanRequestReply
        ListViewLine listViewLine = new ListViewLine(loanRequest);
        lvLoanRequestReply.getItems().add(listViewLine);
        try {
            loanBrokerAppGateway.applyForLoan(loanRequest);
        } catch (JMSException e) {
            e.printStackTrace();
        }
        // to do: send the message with this loanRequest...

        //print all message attributes; but JMSDestination is senderDestination name
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
        loanBrokerAppGateway = new LoanBrokerAppGateway() {
            public void onLoanReplyArrived(LoanRequest loanRequest, LoanReply loanReply){
                ListViewLine listViewLine = getRequestReply(loanRequest);
                listViewLine.setLoanReply(loanReply);
                lvLoanRequestReply.refresh();
            }
        };

    }

    void onLoanRequestArrived(LoanRequest loanRequest){

    }

    void sendLoanReply(LoanRequest loanRequest, LoanReply loanReply){

    }

}
