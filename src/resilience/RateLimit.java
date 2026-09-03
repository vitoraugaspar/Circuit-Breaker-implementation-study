package resilience;

import adapters.HttpRequestAdapterImpl;
import contracts.IHttpRequestAdapter;
import contracts.IResilience;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

public class RateLimit implements IResilience{
    final private AtomicInteger transactionsSent = new AtomicInteger(0);
    private int transactionsLimit = 100;
    private int timeDurationInSeconds = 60;
    private Instant timeStamp;
    private IResilience resilienceService;
    private HttpRequestAdapterImpl httpCallAdapter;
    public RateLimit(Builder builder) {
        this.resilienceService = builder.resilienceService;
        this.transactionsLimit = builder.transactionsLimit;
        this.timeDurationInSeconds = builder.timeDurationInSeconds;
    }
    public static class Builder {
        private final IResilience resilienceService;
        private int transactionsLimit = 100;
        private int timeDurationInSeconds = 60;
        public Builder(IResilience resilienceService) {
            this.resilienceService = resilienceService;
        }
        public Builder transactionsLimit(int transactionsLimit) {
            this.transactionsLimit = transactionsLimit;
            return this;
        }
        public Builder timeDurationInSeconds(int timeDurationInSeconds) {
            this.timeDurationInSeconds = timeDurationInSeconds;
            return this;
        }
        public RateLimit build() {
            return new RateLimit(this);
        }
    }
    public void call(IHttpRequestAdapter httpRequestAdapter, String uri, String body) throws IOException, InterruptedException {
        if (timeStamp == null){
            this.timeStamp = Instant.now();
        }
        Instant currentTime = Instant.now();
        if (currentTime.isAfter(timeStamp.plusSeconds(this.timeDurationInSeconds))){
            this.transactionsSent.set(0);
            this.timeStamp = currentTime;;
        }
        int transactionsCount = transactionsSent.incrementAndGet();
        if (transactionsCount > this.transactionsLimit){
            throw new RuntimeException("O limite de transações chegou. A chamada não será feita");
        }
        resilienceService.call(httpRequestAdapter, uri, body);
    }
}
