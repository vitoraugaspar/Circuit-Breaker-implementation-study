package resilienceService;

import contract.IHttpRequestAdapter;
import contract.IResilience;
import contract.State;
import dto.SavedTransaction;
import exception.FailRequestsException;
import exception.SendRequestsException;
import exception.UnavailableServiceException;
import java.net.http.HttpResponse;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class CircuitBreaker implements IResilience {
    private volatile State state = State.CLOSED;
    private final int limitPermittedForFailTransactions;
    private final AtomicInteger transactionsFailed = new AtomicInteger(0);
    private final AtomicReference<SavedTransaction> requestSavedForTestingService = new AtomicReference<>();
    private final AtomicBoolean alreadyTested = new AtomicBoolean(false);
    private final IResilience resilienceService;
    private HttpResponse<String> response;
    public CircuitBreaker(Builder builder){
        this.resilienceService = builder.resilienceService;
        this.limitPermittedForFailTransactions = builder.limitPermittedForFailTransactions;
    }

    public static class Builder {
        private final IResilience resilienceService;
        private int limitPermittedForFailTransactions = 8;

        public Builder(IResilience resilienceService) {
            this.resilienceService = resilienceService;
        }

        public Builder limitPermittedForFailTransactions(int limitPermittedForFailTransactions){
            this.limitPermittedForFailTransactions = limitPermittedForFailTransactions;
            return this;
        }
        public CircuitBreaker build() {
            return new CircuitBreaker(this);
        }
    }
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        public HttpResponse<String> call(IHttpRequestAdapter httpRequestAdapter, String uri, String body) {
                if(getState() == State.CLOSED){
                    response = resilienceService.call(httpRequestAdapter, uri, body);
                    if(response.statusCode() >=400){
                        int failures = transactionsFailed.incrementAndGet();
                        if (failures >= limitPermittedForFailTransactions){
                            setStateToOpenMode();
                        }
                        throw new FailRequestsException("Falha ao enviar a solicitação. O serviço parece estar indisponível.");
                    }
                    return response;
                }
                if(getState() == State.HALF_OPEN){
                    SavedTransaction current = this.requestSavedForTestingService.get();
                    if(!alreadyTested.compareAndSet(false, true)){
                        throw new UnavailableServiceException("Serviço indisponível");
                    }
                    if(current == null ){
                        this.requestSavedForTestingService.compareAndSet(null, new SavedTransaction(uri, body));
                    }
                    current = this.requestSavedForTestingService.get();
                    HttpResponse<String> response;
                    try {
                        response = resilienceService.call(httpRequestAdapter, current.uri(), current.body());
                        } catch (Exception e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Requisição interrompida", e);
                    }

                    if(response.statusCode() >=400){
                        setStateToOpenModeFromHalfMode();
                        throw new SendRequestsException("Não foi possível enviar a solicitação. O serviço parece estar indisponível");
                    }
                    setStateToClosedMode();
                    return response;
                }
                if(getState() == State.OPEN){
                    throw new FailRequestsException("A requisição falhou. O serviço parece estar indisponível");
                }
            return response;
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

