package contract;
import exception.FailRequestsException;
import exception.SendRequestsException;
import exception.TooManyRequestsException;
import exception.UnavailableServiceException;

import java.io.IOException;

public interface IResilience {
    void call(IHttpRequestAdapter httpRequestAdapter, String uri, String body);
}
