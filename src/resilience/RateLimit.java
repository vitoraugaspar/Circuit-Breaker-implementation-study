package resilience;

import contracts.IResilience;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

public class RateLimit {
    private AtomicInteger transactionsSended = new AtomicInteger(0);
    private int transactionsLimit = 100;
    private int timeDurationInSeconds = 60;
    private Instant timeStamp;
    private IResilience circuitBreaker;
    public RateLimit(IResilience circuitBreaker){
        this.circuitBreaker = circuitBreaker;
    }

    public void call(Integer transactionsLimit, Integer timeDurationInSeconds){
        if(transactionsLimit != null){
            this.transactionsLimit = transactionsLimit;
        }
        if (timeDurationInSeconds != null){
            this.timeDurationInSeconds = timeDurationInSeconds;
        }
        if (timeStamp == null){
            this.timeStamp = Instant.now();
        }
        Instant currentTime = Instant.now();
        if (currentTime.isAfter(timeStamp.plusSeconds(this.timeDurationInSeconds))){
            this.transactionsSended.set(0);
            this.timeStamp = currentTime;;
        }
        int transactionsCount = transactionsSended.incrementAndGet();
        if (transactionsCount >= this.transactionsLimit){
            throw new RuntimeException("O limite de transações chegou. A chamada não será feita");
        }
        System.out.println("Oi");
    }
}
