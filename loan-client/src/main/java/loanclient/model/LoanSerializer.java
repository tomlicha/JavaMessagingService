package loanclient.model;

import com.owlike.genson.Genson;



public class LoanSerializer {

    Genson genson = new Genson();

    public String RequestToString(LoanRequest loanRequest){

        return genson.serialize(loanRequest);
    }

    public LoanRequest requestFromString(String str){

        return genson.deserialize(str,LoanRequest.class);
    }

    public String replyToString(LoanReply loanReply){

        return genson.serialize(loanReply);
    }

    public LoanReply replyFromString(String str){
        return genson.deserialize(str,LoanReply.class);
    }
}
