import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class CircuitBreaker {
    public CircuitBreaker(Short newLimit){
        setLimitPermittedForFailTransactions(newLimit);

    }
    private volatile State state = State.CLOSED;
    private short limitPermittedForFailTransactions = 8;
    private final short[] transactionsRegistered = new short[limitPermittedForFailTransactions];
    private final AtomicInteger transactionsFailed = new AtomicInteger(0);
    private final AtomicReference<HttpRequest> requestSavedForTestingService = new AtomicReference<>();
    private final AtomicBoolean alreadyTested = new AtomicBoolean(false);
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    private final HttpCallAdapter httpCallAdapter = new HttpCallAdapter();
       public void calls(String uri, String body) throws IOException, InterruptedException {
           if(getState() == State.CLOSED){
               try {
                   HttpResponse<String> response = httpCallAdapter.get(uri);
                   if(response.statusCode() >=400){
                       isNeededChangingStateToOpen();
                       throw new RuntimeException("Erro ao mandar a requisição");
                   }

               }
               catch (java.io.IOException | InterruptedException e) {
                   if (e instanceof InterruptedException) {
                       Thread.currentThread().interrupt();
                   }
                   isNeededChangingStateToOpen();
                   throw new RuntimeException("Requisição falhou");
               }
           }

           if(getState() == State.HALF_OPEN){
               HttpRequest request = requestSavedForTestingService.get();
               if(!alreadyTested.compareAndSet(false, true)){
                   throw new RuntimeException("Serviço indisponível");
               }
               if(request == null ){
                   HttpRequest newRequest = HttpRequest.newBuilder()
                           .uri(URI.create(uri))
                           .header("Accept", "application/json")
                           .timeout(Duration.ofSeconds(5))
                           .POST(HttpRequest.BodyPublishers.ofString(body))
                           .build();
                   request = requestSavedForTestingService.compareAndSet(null, newRequest) ? newRequest : requestSavedForTestingService.get();
               }
               try {
                   HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                   if(response.statusCode() >=400){
                       setStateToHalfOpenMode();
                       throw new RuntimeException("Erro ao mandar a requisição");
                   }

                   setStateToOpenMode();

               }
               catch (java.io.IOException | InterruptedException e) {
                   if (e instanceof InterruptedException) {
                       Thread.currentThread().interrupt();
                   }
                   setStateToHalfOpenMode();
                   throw new RuntimeException("Requisição falhou");
               }
           }
           if(getState() == State.OPEN){
               throw new RuntimeException("Requisição falhou");
           }
       }
        public State getState() {
            return this.state;
        }

        public void setState(State state){
            this.state = state;
        }

        public void setLimitPermittedForFailTransactions(Short newLimit) {
            if (newLimit != null) {
                this.limitPermittedForFailTransactions = newLimit;
            }
        }

        public void isNeededChangingStateToOpen(){
            int failures = transactionsFailed.incrementAndGet();
            if (failures >= limitPermittedForFailTransactions){
                this.state = State.OPEN;
                this.transactionsFailed.set(0);
                Runnable runnable = () -> setState(State.HALF_OPEN);
                executor.schedule(runnable, 30, TimeUnit.SECONDS);
            }
        }

        public void setStateToHalfOpenMode(){
            this.state = State.OPEN;
            this.alreadyTested.set(false);
            this.requestSavedForTestingService.set(null);
            Runnable runnable = () -> setState(State.HALF_OPEN);
            executor.schedule(runnable, 30, TimeUnit.SECONDS);
        }

        public void setStateToOpenMode(){
            this.state = State.CLOSED;
            this.alreadyTested.set(false);
            this.requestSavedForTestingService.set(null);
    }
    }

