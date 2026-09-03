package resilience;

import adapters.HttpRequestAdapterImpl;
import contracts.IHttpRequestAdapter;
import contracts.IResilience;

import java.net.http.HttpResponse;

public class Retry implements IResilience{
    private final Integer tries;
    private final Integer periodOfTImeInMilliSeconds;
    private final Integer multiplyTransactionsDelayBy;
    private int newDelayTimeInMilliSeconds;
    private IResilience resilienceService;
    private HttpRequestAdapterImpl httpCallAdapter;
    private Retry(Builder builder) {
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
    public void call(IHttpRequestAdapter httpRequestAdapter, String uri, String body){

        for (int counter = 1; counter <= this.tries; counter++){
            try {
                System.out.println("Oi" + newDelayTimeInMilliSeconds);
                newDelayTimeInMilliSeconds = (int) Math.pow(this.multiplyTransactionsDelayBy ,counter - 1) * periodOfTImeInMilliSeconds;;
                Thread.sleep(newDelayTimeInMilliSeconds);

            }
            catch (InterruptedException e) {
                throw new RuntimeException("Falha ao se comunicar com o servidor");
            }

        }
        Thread.currentThread().interrupt();
    }
}
