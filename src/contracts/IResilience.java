package contracts;

import java.io.IOException;

public interface IResilience {
    void call(String uri, String body) throws IOException, InterruptedException;
}
