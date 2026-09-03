import adapters.HttpRequestAdapterImpl;
import resilience.CircuitBreaker;
import resilience.HttpCall;
import resilience.RateLimit;
import resilience.Retry;

import java.net.http.HttpResponse;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
void main() throws IOException, InterruptedException {
    HttpRequestAdapterImpl httpRequest = new HttpRequestAdapterImpl();
    HttpCall httpCall = new HttpCall();
    RateLimit rateLimit = new RateLimit.Builder(httpCall).transactionsLimit(3).build();
    Retry retry = new Retry.Builder(rateLimit).tries(5).build();
        retry.call(httpRequest, "https://httpbin.org/", null);
}
