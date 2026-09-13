package resilienceService;
import contract.IHttpRequestAdapter;
import contract.IResilience;
import java.io.IOException;

public class HttpCall implements IResilience {
    public void call(IHttpRequestAdapter httpRequestAdapter, String uri, String body) {
        httpRequestAdapter.get(uri);
    }
}
