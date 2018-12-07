import com.owlike.genson.Genson;
import model.BankInterestReply;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

public class ArchiveGateway {
    public void archive(BankInterestReply bankInterestReply){

        Genson genson = new Genson();
        HttpClient httpClient = HttpClientBuilder.create().build(); //Use this instead
        try {

            HttpPost request = new HttpPost("http://localhost:8080/Archive/rest/accepted");
            request.addHeader("Accept", "text/plain");
            request.addHeader("content-type", "application/json");
            String reply = genson.serialize(bankInterestReply);
            System.out.println("reply strigifyied : "+reply);
            HttpEntity entity = new ByteArrayEntity(reply.getBytes("UTF-8"));
            request.setEntity(entity);
            HttpResponse response = httpClient.execute(request);

            //handle response here...
            System.out.println("reponse from web app :\n"+response);
        }catch (Exception ex) {

            //handle exception here

        } finally {
            //Deprecated
            //httpClient.getConnectionManager().shutdown();
        }
    }

}
