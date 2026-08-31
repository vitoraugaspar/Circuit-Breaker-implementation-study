import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Retry {
    Integer tries = 2;
    Integer periodOfTImeInMilliSeconds = 1000;
    Integer multiplyTransactionsDelayBy = 2;
    int newDelayTimeInMilliSeconds;


    public void retry(Integer tries, Integer periodOfTImeInMilliSeconds, Integer multiplyTransactionsDelayBy){
        if (tries != null){
            this.tries = tries;
        }
        if (periodOfTImeInMilliSeconds != null){
            this.periodOfTImeInMilliSeconds = periodOfTImeInMilliSeconds;
        }
        if (multiplyTransactionsDelayBy != null){
            this.multiplyTransactionsDelayBy = multiplyTransactionsDelayBy;
        }

        for (int counter = 1; counter <= tries; counter++){
            try {
                System.out.println("Oi" + newDelayTimeInMilliSeconds);
                newDelayTimeInMilliSeconds = (int) Math.pow(multiplyTransactionsDelayBy ,counter - 1) * periodOfTImeInMilliSeconds;;
                Thread.sleep(newDelayTimeInMilliSeconds);
            }
            catch (InterruptedException e) {

            }

        }
        Thread.currentThread().interrupt();
    }
}
