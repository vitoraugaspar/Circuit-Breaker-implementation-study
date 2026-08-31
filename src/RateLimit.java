import java.sql.Time;
import java.time.Instant;
import java.time.LocalTime;
import java.time.temporal.TemporalUnit;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class RateLimit {
    private AtomicInteger transactionsSended = new AtomicInteger(0);
    private int transactionsLimit = 100;
    private int timeDurationInSeconds = 60;
    Instant timeStamp;

    public void rateLimit(Integer transactionsLimit, Integer timeDurationInSeconds){
        if(transactionsLimit != null){
            this.transactionsLimit = transactionsLimit;
        }
        if (timeDurationInSeconds != null){
            this.timeDurationInSeconds = timeDurationInSeconds;
        }
        if (timeStamp == null){
            timeStamp = Instant.now();
        }
        Instant currentTime = Instant.now();
        if (currentTime.isAfter(timeStamp.plusSeconds(timeDurationInSeconds))){
            transactionsSended.set(0);
            timeStamp = currentTime;;
        }
        int transactionsCount = transactionsSended.incrementAndGet();
        if (transactionsCount >= this.transactionsLimit){
            throw new RuntimeException("O limite de transações chegou. A chamada não será feita");
        }
        System.out.println("Oi");
    }
}
