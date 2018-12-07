package bank.model;

/**
 *
 * This class stores all information about an request from a bank to offer
 * a loan to a specific client.
 */
public class BankInterestRequest {

    private int amount; // the ammount to borrow
    private int time; // the time-span of the loan in years
    private int credit;
    private int history;
    private int ssn;

    public int getSsn() {
        return ssn;
    }

    public void setSsn(int ssn) {
        this.ssn = ssn;
    }

    public int getCredit() {
        return credit;
    }

    public void setCredit(int credit) {
        this.credit = credit;
    }

    public int getHistory() {
        return history;
    }

    public void setHistory(int history) {
        this.history = history;
    }

    public BankInterestRequest() {
        super();
        this.amount = 0;
        this.time = 0;
        this.credit=0;
        this.history=0;
        this.ssn=0;
    }

    public BankInterestRequest(int amount, int time, int credit, int history, int ssn) {
        super();
        this.amount = amount;
        this.time = time;
        this.credit=credit;
        this.history=history;
        this.ssn=ssn;
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

    @Override
    public String toString() {
        return "ssn=" + ssn + " amount=" + amount + " time=" + time + " credit="+ credit+" history="+history;
    }
}
