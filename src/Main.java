import adapters.HttpRequestAdapterImpl;
import contracts.IResilience;
import exceptions.FailRequestsException;
import exceptions.SendRequestsException;
import exceptions.TooManyRequestsException;
import exceptions.UnavailableServiceException;
import resilienceServices.HttpCall;
import resilienceServices.RateLimit;
import resilienceServices.Retry;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
void main() throws IOException, InterruptedException, TooManyRequestsException, FailRequestsException, SendRequestsException, UnavailableServiceException {
    HttpRequestAdapterImpl httpRequest = new HttpRequestAdapterImpl();
    IResilience httpCall = new HttpCall();
    IResilience rateLimit = new RateLimit.Builder(httpCall).transactionsLimit(3).timeDurationInSeconds(2).build();
    IResilience retry = new Retry.Builder(rateLimit).tries(5).build();
        retry.call(httpRequest, "https://httpbin.org/", null);
}
