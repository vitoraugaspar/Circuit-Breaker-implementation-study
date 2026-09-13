package resilienceService;
import contract.IHttpRequestAdapter;
import contract.IResilience;

import java.net.http.HttpResponse;

public class HttpCall implements IResilience {
    public HttpResponse<String> call(IHttpRequestAdapter httpRequestAdapter, String uri, String body) {
        return httpRequestAdapter.get(uri);
    }
}
