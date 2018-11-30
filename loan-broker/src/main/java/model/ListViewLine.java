package model;

/**
 * This class is an item/line for a ListView. It makes it possible to put both BankInterestRequest and BankInterestReply object in one item in a ListView.
 */
public class ListViewLine {
	
	private LoanRequest loanRequest;
	private LoanReply loanReply;
	private BankInterestRequest bankInterestRequest;
	private BankInterestReply bankInterestReply;
	
	public ListViewLine(LoanRequest loanRequest) {
		setLoanRequest(loanRequest);
		setLoanReply(null);
		setBankInterestReply(null);
		setBankInterestRequest(null);
	}

	public ListViewLine(BankInterestReply bankInterestReply) {
		setBankInterestRequest(null);
		setBankInterestReply(bankInterestReply);
		setLoanReply(null);
		setLoanRequest(null);

	}
	
	public LoanRequest getLoanRequest() {
		return loanRequest;
	}
	
	private void setLoanRequest(LoanRequest loanRequest) {
		this.loanRequest = loanRequest;
	}
	
	public LoanReply getLoanReply() {
		return loanReply;
	}
	
	public void setLoanReply(LoanReply loanReply) {
		this.loanReply = loanReply;
	}

    /**
     * This method defines how one line is shown in the ListView.
     * @return
     *  a) if BankInterestReply is null, then this item will be shown as "loanRequest.toString ---> waiting for loan reply..."
     *  b) if BankInterestReply is not null, then this item will be shown as "loanRequest.toString ---> loanReply.toString"
     */
	@Override
	public String toString() {
	   return loanRequest.toString() + "  --->  " + ((bankInterestReply !=null)? bankInterestReply.toString():"waiting for bank reply...");
	}

	public BankInterestRequest getBankInterestRequest() {
		return bankInterestRequest;
	}

	private void setBankInterestRequest(BankInterestRequest bankInterestRequest) {
		this.bankInterestRequest = bankInterestRequest;
	}

	public BankInterestReply getBankInterestReply() {
		return bankInterestReply;
	}

	public void setBankInterestReply(BankInterestReply bankInterestReply) {
		this.bankInterestReply = bankInterestReply;
	}

	/**
	 * This method defines how one line is shown in the ListView.
	 * @return
	 *  a) if BankInterestReply is null, then this item will be shown as "bankInterestRequest.toString ---> waiting for loan reply..."
	 *  b) if BankInterestReply is not null, then this item will be shown as "bankInterestRequest.toString ---> bankInterestReply.toString"
	 */

}
