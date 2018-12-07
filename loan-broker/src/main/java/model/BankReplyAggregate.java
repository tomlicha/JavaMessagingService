package model;

import java.util.ArrayList;
import java.util.List;

public class BankReplyAggregate {

    private int numberRequestSent;
    List<BankInterestReply> listBankReply = new ArrayList<>();

    public void addReply(BankInterestReply bankInterestReply){
        listBankReply.add(bankInterestReply);
    }

    public int getNumberRequestSent() {
        return numberRequestSent;
    }

    public void setNumberRequestSent(int numberRequestSent) {
        this.numberRequestSent = numberRequestSent;
    }

    public List<BankInterestReply> getListBankReply() {
        return listBankReply;
    }

    public void setListBankReply(List<BankInterestReply> listBankReply) {
        this.listBankReply = listBankReply;
    }

    public boolean isFinished(){
        if (numberRequestSent == listBankReply.size()) return true;
        else return false;
    }

    public BankInterestReply getBest(){
        BankInterestReply minInterest = listBankReply.get(0);
        for(int i=1;i<listBankReply.size();i++){

            if (listBankReply.get(i).getInterest() < minInterest.getInterest()){
                minInterest = listBankReply.get(i);
            }
        }
        return minInterest;
    }
}
