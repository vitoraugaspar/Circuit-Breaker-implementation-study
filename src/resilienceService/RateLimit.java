package resilienceService;

import contract.IHttpRequestAdapter;
import contract.IResilience;
import exception.TooManyRequestsException;

import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

public class RateLimit implements IResilience{
    final private AtomicInteger transactionsSent = new AtomicInteger(0);
    private final Object lock = new Object();
    private final int transactionsLimit;
    private final int timeDurationInSeconds;
    private Instant timeStamp;
    private IResilience resilienceService;

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
    public HttpResponse<String> call(IHttpRequestAdapter httpRequestAdapter, String uri, String body) {
        if (timeStamp == null){
            this.timeStamp = Instant.now();
        }
        synchronized (lock){
            Instant currentTime = Instant.now();
            if (currentTime.isAfter(timeStamp.plusSeconds(this.timeDurationInSeconds))){
                this.transactionsSent.set(0);
                this.timeStamp = currentTime;
            }
        }
        int transactionsCount = transactionsSent.incrementAndGet();
        System.out.println("transactionsCount " + transactionsCount);
        if (transactionsCount > this.transactionsLimit){
            throw new TooManyRequestsException("O limite de requisições foi atingido");
        }
        return resilienceService.call(httpRequestAdapter, uri, body);
    }
}
