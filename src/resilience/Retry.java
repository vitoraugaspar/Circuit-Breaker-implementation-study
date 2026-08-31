package resilience;

import contracts.IResilience;

public class Retry {
    Integer tries = 2;
    Integer periodOfTImeInMilliSeconds = 1000;
    Integer multiplyTransactionsDelayBy = 2;
    int newDelayTimeInMilliSeconds;

    private IResilience circuitBreaker;
    public Retry(IResilience circuitBreaker){
        this.circuitBreaker = circuitBreaker;
    }

    public void call(Integer tries, Integer periodOfTImeInMilliSeconds, Integer multiplyTransactionsDelayBy){
        if (tries != null){
            this.tries = tries;
        }
        if (periodOfTImeInMilliSeconds != null){
            this.periodOfTImeInMilliSeconds = periodOfTImeInMilliSeconds;
        }
        if (multiplyTransactionsDelayBy != null){
            this.multiplyTransactionsDelayBy = multiplyTransactionsDelayBy;
        }

        for (int counter = 1; counter <= this.tries; counter++){
            try {
                System.out.println("Oi" + newDelayTimeInMilliSeconds);
                newDelayTimeInMilliSeconds = (int) Math.pow(this.multiplyTransactionsDelayBy ,counter - 1) * periodOfTImeInMilliSeconds;;
                Thread.sleep(newDelayTimeInMilliSeconds);
            }
            catch (InterruptedException e) {

            }

        }
        Thread.currentThread().interrupt();
    }
}
