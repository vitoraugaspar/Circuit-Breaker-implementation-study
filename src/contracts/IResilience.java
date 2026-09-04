package contracts;
import exceptions.FailRequestsException;
import exceptions.SendRequestsException;
import exceptions.TooManyRequestsException;
import exceptions.UnavailableServiceException;

import java.io.IOException;

public interface IResilience {
    void call(IHttpRequestAdapter httpRequestAdapter, String uri, String body) throws IOException, InterruptedException, TooManyRequestsException, SendRequestsException, FailRequestsException, UnavailableServiceException;
}
