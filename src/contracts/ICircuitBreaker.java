package contracts;

import java.io.IOException;

public interface ICircuitBreaker {
    void call(String uri, String body) throws IOException, InterruptedException;
}
