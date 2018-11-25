package loanclient.model;

/**
 *
 * This class stores all information about a bank offer
 * as a response to a client loan request.
 */
public class LoanReply {

        private double interest; // the interest that the bank offers for the requested loan
        private String bankId; // the unique quote identification of the bank which makes the offer

    public LoanReply() {
        super();
        this.interest = 0;
        this.bankId = "";
    }
    public LoanReply(double interest, String quoteID) {
        super();
        this.interest = interest;
        this.bankId = quoteID;
    }

    public double getInterest() {
        return interest;
    }

    public void setInterest(double interest) {
        this.interest = interest;
    }

    public String getBankId() {
        return bankId;
    }

    public void setBankId(String bankId) {
        this.bankId = bankId;
    }
    
    @Override
    public String toString(){
        return " interest="+String.valueOf(interest) + " quoteID="+String.valueOf(bankId);
    }
}
