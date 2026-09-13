package resilienceService;
import adapter.HttpRequestAdapterImpl;
import contract.IHttpRequestAdapter;
import contract.IResilience;
import exception.FailRequestsException;
import exception.SendRequestsException;
import exception.TooManyRequestsException;
import exception.UnavailableServiceException;

import java.io.IOException;
import java.net.http.HttpResponse;

public class Retry implements IResilience{
    private final Integer tries;
    private final Integer periodOfTImeInMilliSeconds;
    private final Integer multiplyTransactionsDelayBy;
    private int newDelayTimeInMilliSeconds;
    private IResilience resilienceService;
    private HttpRequestAdapterImpl httpCallAdapter;

    public Retry(Builder builder) {
        this.resilienceService = builder.resilienceService;
        this.tries = builder.tries;
        this.periodOfTImeInMilliSeconds = builder.periodOfTImeInMilliSeconds;
        this.multiplyTransactionsDelayBy = builder.multiplyTransactionsDelayBy;
    }
    public static class Builder {
        private final IResilience resilienceService;
        private int tries = 3;
        private int periodOfTImeInMilliSeconds = 1000;
        private int multiplyTransactionsDelayBy = 2;
        public Builder(IResilience resilienceService) {
            this.resilienceService = resilienceService;
        }
        public Builder tries(int tries) {
            this.tries = tries;
            return this;
        }
        public Builder periodOfTImeInMilliSeconds(int period) {
            this.periodOfTImeInMilliSeconds = period;
            return this;
        }
        public Builder multiplyTransactionsDelayBy(int multiplier) {
            this.multiplyTransactionsDelayBy = multiplier;
            return this;
        }
        public Retry build() {
            return new Retry(this);
        }
    }
    public HttpResponse<String> call(IHttpRequestAdapter httpRequestAdapter, String uri, String body) {
        HttpResponse<String> response = null;
        for (int counter = 1; counter <= this.tries; counter++){
                response = resilienceService.call(httpRequestAdapter, uri, body);
                if(response.statusCode() < 300){
                    return response;
                }
                newDelayTimeInMilliSeconds = (int) Math.pow(this.multiplyTransactionsDelayBy ,counter - 1) * periodOfTImeInMilliSeconds;;
                try {
                    Thread.sleep(newDelayTimeInMilliSeconds);
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                    break;
            }
        }
        Thread.currentThread().interrupt();
        return response;
    }
}
