import adapter.HttpRequestAdapterImpl;
import contract.IResilience;
import exception.FailRequestsException;
import exception.SendRequestsException;
import exception.TooManyRequestsException;
import exception.UnavailableServiceException;
import resilienceService.HttpCall;
import resilienceService.RateLimit;
import resilienceService.Retry;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
void main() throws IOException, InterruptedException, TooManyRequestsException, FailRequestsException, SendRequestsException, UnavailableServiceException {
    HttpRequestAdapterImpl httpRequest = new HttpRequestAdapterImpl();
    IResilience httpCall = new HttpCall();
    IResilience rateLimit = new RateLimit.Builder(httpCall).transactionsLimit(3).timeDurationInSeconds(60).build();
    IResilience retry = new Retry.Builder(rateLimit).tries(5).build();
        retry.call(httpRequest, "https://httpbin.org/", null);
}
