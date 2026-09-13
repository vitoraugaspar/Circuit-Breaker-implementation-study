package contract;
import java.io.IOException;
import java.net.http.HttpResponse;

public interface IHttpRequestAdapter {
    public  HttpResponse<String> get(String uri);
}
