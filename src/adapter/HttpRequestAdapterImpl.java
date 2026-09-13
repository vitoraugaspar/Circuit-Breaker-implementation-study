package adapter;
import contract.IHttpRequestAdapter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class HttpRequestAdapterImpl implements IHttpRequestAdapter {
    public HttpRequestAdapterImpl(){

    }
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    public HttpResponse<String> get(String uri) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();
                try {
                    return client.send(request, HttpResponse.BodyHandlers.ofString());
                } catch (IOException e) {
                    throw new RuntimeException("Erro ao enviar requisição HTTP");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Requisição interrompida");
        }

    }
}
