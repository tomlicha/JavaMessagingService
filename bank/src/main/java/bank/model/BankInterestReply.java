package bank.model;

/**
 * This class stores information about the bank reply
 *  to a loan request of the specific client
 * 
 */
public class BankInterestReply {

    private int SSN;
    private int amount;
    private String bank; // the unique quote identification of the bank which makes the offer
    private double interest; // the interest that the bank offers for the requested loan
    private int time;

    public BankInterestReply() {
        this.SSN=0;
        this.amount=0;
        this.time=0;
        this.interest = 0;
        this.bank = "";
    }

    public BankInterestReply(double interest, String bank, int SSN, int time, int amount) {
        this.interest = interest;
        this.bank = bank;
        this.SSN=SSN;
        this.time=time;
        this.amount=amount;
    }

    public double getInterest() {
        return interest;
    }

    public void setInterest(double interest) {
        this.interest = interest;
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public String toString() {
        return "bank=" + this.bank + " interest=" + this.interest;
    }

    public int getSSN() {
        return SSN;
    }

    public void setSSN(int SSN) {
        this.SSN = SSN;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }
}
