package bank.gui;

import bank.BankBrokerAppGateway;
import bank.model.BankInterestReply;
import bank.model.BankInterestRequest;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import javax.jms.JMSException;
import java.net.URL;
import java.util.ResourceBundle;

public class BankController implements Initializable {
   //private final String BANK_ID = "ABN";

    public ListView<ListViewLine> lvBankRequestReply;
    public TextField tfInterest;
    private String bankName;

    private BankBrokerAppGateway bankBrokerAppGateway;

    BankController (String bankName){
        this.bankName = bankName;
    }

    @FXML
    public void btnSendBankInterestReplyClicked() throws JMSException {
        double interest = Double.parseDouble(tfInterest.getText());
        // TO DO: send a message with bankInterestReply
        ListViewLine selected = lvBankRequestReply.getSelectionModel().getSelectedItem();
        System.out.println("selected message :" + selected);
        BankInterestReply bankInterestReply = new BankInterestReply(interest, bankName,selected.getBankInterestRequest().getSsn(),selected.getBankInterestRequest().getTime(),selected.getBankInterestRequest().getAmount());
        selected.setBankInterestReply(bankInterestReply);
        bankBrokerAppGateway.applyForBank(bankInterestReply,selected);
        lvBankRequestReply.refresh();

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
        bankBrokerAppGateway = new BankBrokerAppGateway(bankName) {
            public void onBankRequestArrived(BankInterestRequest bankInterestRequest, ListViewLine listViewLine){

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        lvBankRequestReply.getItems().add(listViewLine);

                    }
                });
                lvBankRequestReply.refresh();
            }
        };
    }



}
