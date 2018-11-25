package model;

import com.owlike.genson.Genson;

public class BankSerializer {

    Genson genson = new Genson();

    public String RequestToString(BankInterestRequest bankInterestRequest)
    {
        return genson.serialize(bankInterestRequest);

    }

    public BankInterestRequest requestFromString(String str){
        return genson.deserialize(str,BankInterestRequest.class);

    }

    public String replyToString(BankInterestReply bankInterestReply){
        return genson.serialize(bankInterestReply);

    }

    public BankInterestReply replyFromString(String str){
        return genson.deserialize(str,BankInterestReply.class);
    }
}
