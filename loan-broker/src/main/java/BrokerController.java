import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import model.*;

import javax.jms.JMSException;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;


public class BrokerController implements Initializable {


    public ListView<ListViewLine> lvBrokerRequestReply;

    ClientAppGateway clientAppGateway;
    BankAppGateway bankAppGateway;
    ArchiveGateway archiveGateway = new ArchiveGateway();
    HashMap<BankInterestRequest,LoanRequest> hmap2 = new HashMap<>();

    public BrokerController() {


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
        clientAppGateway = new ClientAppGateway() {
            @Override
            public void onLoanRequestArrived(LoanRequest loanRequest) {

                ListViewLine listViewLine = new ListViewLine(loanRequest);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {

                        lvBrokerRequestReply.getItems().add(listViewLine);
                        lvBrokerRequestReply.refresh();
                    }
                });
                //call the credit agency to get credit and history
                BankInterestRequest bankInterestRequest = new BankInterestRequest(loanRequest.getAmount(),loanRequest.getTime(),loanRequest.getCredit(),loanRequest.getHistory(),loanRequest.getSsn());
                hmap2.put(bankInterestRequest,loanRequest);
                try {
                    bankAppGateway.sendLoanReply(bankInterestRequest);
                } catch (JMSException e) {
                    e.printStackTrace();
                }

            }
        };
        bankAppGateway = new BankAppGateway() {
            @Override
            public void onBankInterestReplyArrived(BankInterestReply bankInterestReply, BankInterestRequest bankInterestRequest) {

                ListViewLine listViewLine = getRequestReply(hmap2.get(bankInterestRequest));

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {

                        listViewLine.setBankInterestReply(bankInterestReply);
                        lvBrokerRequestReply.refresh();

                    }

                });
                if (bankInterestReply.getInterest() > 0){
                    //send to archive service and then to the client
                    //if not, just send it to the client / the loan hasn't been approved
                    System.out.println("sending reply to archive :" + bankInterestReply.toString());
                    archiveGateway.archive(bankInterestReply);

                }

                LoanReply loanReply = new LoanReply(bankInterestReply.getInterest(),bankInterestReply.getBank());

                try {
                    clientAppGateway.sendLoanReply(loanReply,hmap2.get(bankInterestRequest));
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        };
    }
}
