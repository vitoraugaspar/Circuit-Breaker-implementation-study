package contracts;

import java.io.IOException;

public interface IResilience {
    void call(IHttpRequestAdapter httpRequestAdapter, String uri, String body) throws IOException, InterruptedException;
}
