package contracts;

import java.io.IOException;
import java.net.http.HttpResponse;

public interface IHttpCallAdapter {
    public  HttpResponse<String> get(String uri) throws IOException, InterruptedException;
}
