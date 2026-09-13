package contract;
import exception.FailRequestsException;
import exception.SendRequestsException;
import exception.TooManyRequestsException;
import exception.UnavailableServiceException;

import java.io.IOException;
import java.net.http.HttpResponse;

public interface IResilience {
    HttpResponse<String> call(IHttpRequestAdapter httpRequestAdapter, String uri, String body);
}
