import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CircuitBreaker {
    public CircuitBreaker(Short newLimit){
        if(newLimit != null){
            limitPermittedForFailTransactions = newLimit;
        }

    }
    State state = State.CLOSED;
    short limitPermittedForFailTransactions = 8;
    short transactionsFailed = 0;
    LocalTime timeStamp;
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

       public void calls(String uri, String body){
           if(getState() == State.CLOSED){
                   HttpRequest request = HttpRequest.newBuilder()
                           .uri(URI.create(uri))
                           .header("Accept", "application/json")
                           .timeout(Duration.ofSeconds(5))
                           .POST(HttpRequest.BodyPublishers.ofString(body))
                           .build();
               try {
                   HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                   if(response.statusCode() >=400){
                       transactionsFailed++;
                       if (transactionsFailed >= limitPermittedForFailTransactions){
                           state = State.OPEN;
                           transactionsFailed = 0;
                           Runnable runnable = () -> setState(State.HALF_OPEN);
                           executor.schedule(runnable, 30, TimeUnit.SECONDS);
                       }
                       throw new RuntimeException("Erro ao mandar a requisição");
                   }

               } catch (java.io.IOException | InterruptedException e) {
                   if (e instanceof InterruptedException) {
                       Thread.currentThread().interrupt();
                   }
                   transactionsFailed++;
                   if (transactionsFailed >= limitPermittedForFailTransactions) {
                       state = State.OPEN;
                       transactionsFailed = 0;
                       Runnable runnable = () -> setState(State.HALF_OPEN);
                       executor.schedule(runnable, 30, TimeUnit.SECONDS);
                   }
                   throw new RuntimeException("Requisição falhou");
               }
           }

           if(getState() == State.HALF_OPEN){

           }
           if(getState() == State.OPEN){
               throw new RuntimeException("Requisição falhou");
           }
       }
        public State getState() {
            return state;
        }

    public void setState(State state){
        this.state = state;
    }
    }

