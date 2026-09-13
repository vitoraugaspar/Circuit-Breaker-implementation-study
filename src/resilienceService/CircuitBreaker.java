package resilienceService;
import adapter.HttpRequestAdapterImpl;
import contract.IHttpRequestAdapter;
import contract.IResilience;
import contract.State;
import exception.FailRequestsException;
import exception.SendRequestsException;
import exception.UnavailableServiceException;

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
    private HttpRequestAdapterImpl httpCallAdapter;
    private final IResilience resilienceService;
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    public CircuitBreaker( IResilience resilienceService, Short newLimit){
        if (newLimit != null) {
            this.limitPermittedForFailTransactions = newLimit;
        }
        this.resilienceService = resilienceService;
    }
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        public void call(IHttpRequestAdapter httpRequestAdapter, String uri, String body) {
            if(getState() == State.CLOSED){
                    HttpResponse<String> response = httpCallAdapter.get(uri);
                    if(response.statusCode() >=400){
                        int failures = transactionsFailed.incrementAndGet();
                        if (failures >= limitPermittedForFailTransactions){
                            setStateToOpenMode();
                        }
                        throw new FailRequestsException("Falha ao enviar a solicitação. O serviço parece estar indisponível.");
                    }
            }
            if(getState() == State.HALF_OPEN){
                HttpRequest request = requestSavedForTestingService.get();
                if(!alreadyTested.compareAndSet(false, true)){
                   throw new UnavailableServiceException("Serviço indisponível");
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
                HttpResponse<String> response;
                try {
                    response = client.send(request, HttpResponse.BodyHandlers.ofString());
                } catch (IOException e) {
                    throw new RuntimeException("Erro ao enviar requisição HTTP", e);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Requisição interrompida", e);
                }

                if(response.statusCode() >=400){
                    setStateToOpenModeFromHalfMode();
                    throw new SendRequestsException("Não foi possível enviar a solicitação. O serviço parece estar indisponível");
                }
                setStateToClosedMode();
            }
            if(getState() == State.OPEN){
                throw new FailRequestsException("A requisição falhou. O serviço parece estar indisponível");
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
        private void setStateToClosedMode(){
            setState(State.CLOSED);
            this.alreadyTested.set(false);
            this.requestSavedForTestingService.set(null);
        }
    }

