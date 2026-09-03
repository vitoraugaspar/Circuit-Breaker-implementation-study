package resilience;

import contracts.IHttpRequestAdapter;
import contracts.IResilience;

import java.io.IOException;

public class HttpCall implements IResilience {
    public void call(IHttpRequestAdapter httpRequestAdapter, String uri, String body) throws IOException, InterruptedException {
        httpRequestAdapter.get(uri); // chamada real, sem decoration nenhuma
    }
}
