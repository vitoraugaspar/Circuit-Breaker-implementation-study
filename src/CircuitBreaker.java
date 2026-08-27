import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalTime;

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
                           timeStamp = LocalTime.now();
                           transactionsFailed = 0;
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
                       timeStamp = LocalTime.now();
                       transactionsFailed = 0;
                   }
                   throw new RuntimeException("Requisição falhou");
               }
           }

           if(getState() == State.HALF_OPEN){

           }
           if(getState() == State.OPEN){

           }
       }
        public State getState() {
            return state;
        }
    }
