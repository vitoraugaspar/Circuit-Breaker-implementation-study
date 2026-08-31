package resilience;

import adapters.HttpRequestAdapterImpl;
import contracts.IResilience;
import contracts.State;

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

public class CircuitBreaker implements IResilience {
    private volatile State state = State.CLOSED;
    private short limitPermittedForFailTransactions = 8;
    private final short[] transactionsRegistered = new short[limitPermittedForFailTransactions];
    private final AtomicInteger transactionsFailed = new AtomicInteger(0);
    private final AtomicReference<HttpRequest> requestSavedForTestingService = new AtomicReference<>();
    private final AtomicBoolean alreadyTested = new AtomicBoolean(false);
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    public CircuitBreaker(Short newLimit){
        if (newLimit != null) {
            this.limitPermittedForFailTransactions = newLimit;
        }
    }
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    private final HttpRequestAdapterImpl httpCallAdapter = new HttpRequestAdapterImpl();
       public void call(String uri, String body) throws IOException, InterruptedException {
           if(getState() == State.CLOSED){
               try {
                   HttpResponse<String> response = httpCallAdapter.get(uri);
                   if(response.statusCode() >=400){
                       int failures = transactionsFailed.incrementAndGet();
                       if (failures >= limitPermittedForFailTransactions){
                           setStateToOpenMode();
                       }
                       throw new RuntimeException("Erro ao mandar a requisição");
                   }

               }
               catch (java.io.IOException | InterruptedException e) {
                   if (e instanceof InterruptedException) {
                       Thread.currentThread().interrupt();
                   }
                   int failures = transactionsFailed.incrementAndGet();
                   if (failures >= limitPermittedForFailTransactions){
                       setStateToOpenMode();
                   }
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
                       setStateToOpenModeFromHalfMode();
                       throw new RuntimeException("Erro ao mandar a requisição");
                   }

                   setStateToClosedMode();

               }
               catch (java.io.IOException | InterruptedException e) {
                   if (e instanceof InterruptedException) {
                       Thread.currentThread().interrupt();
                   }
                   setStateToOpenModeFromHalfMode();
                   throw new RuntimeException("Requisição falhou");
               }
           }
           if(getState() == State.OPEN){
               throw new RuntimeException("Requisição falhou");
           }
       }
        private State getState() {
            return this.state;
        }

        private void setState(State state){
            this.state = state;
        }

        private void setStateToOpenMode(){
                setState(State.OPEN);
                this.transactionsFailed.set(0);
                Runnable runnable = () -> setState(State.HALF_OPEN);
                executor.schedule(runnable, 30, TimeUnit.SECONDS);
        }

        private void setStateToOpenModeFromHalfMode(){
            setState(State.OPEN);
            this.alreadyTested.set(false);
            this.requestSavedForTestingService.set(null);
            Runnable runnable = () -> setState(State.HALF_OPEN);
            executor.schedule(runnable, 30, TimeUnit.SECONDS);
        }

        public void setStateToClosedMode(){
            setState(State.CLOSED);
            this.alreadyTested.set(false);
            this.requestSavedForTestingService.set(null);
    }
    }

