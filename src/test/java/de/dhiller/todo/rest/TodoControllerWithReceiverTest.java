package de.dhiller.todo.rest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.HttpResponse;
import org.mockserver.verify.VerificationTimes;
import org.springframework.http.MediaType;

import javax.transaction.Transactional;
import java.net.URI;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockserver.model.HttpRequest.request;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TodoControllerWithReceiverTest extends ControllerTestBase {

    @Rule
    public MockServerRule mockServerRule = new MockServerRule(this, 8081);

    private MockServerClient mockServerClient = new MockServerClient("localhost", 8081);

    private String authenticationToken;

    private final TodoDTO todo = new TodoDTO(true, "Clean up the kitchen (finished)");

    @Before
    public void requestAuthentication() throws Exception {
        authenticationToken = authenticateJaneDoe();
    }

    @Test
    @Transactional
    public void updateItemWithWrongReceiverURI() throws Exception {
        performPutUpdateReceiver(new UpdateReceiverDTO(URI.create("http://localhost:9999/wrongUrl404")), authenticationToken);

        performUpdate(authenticationToken, todo);
    }

    @Test
    @Transactional
    public void updateItemWithReceiver() throws Exception {
        mockServerClient.when(request().withMethod("PUT").withPath("/todoItemUpdate")).respond(HttpResponse.response().withStatusCode(200));

        performPutUpdateReceiver(new UpdateReceiverDTO(URI.create("http://localhost:8081/todoItemUpdate")), authenticationToken);

        performUpdate(authenticationToken, todo);

        await().atMost(10, TimeUnit.SECONDS).until(verify());
    }

    public Callable<Boolean> verify() {
        return () -> {
            try {
                mockServerClient.verify(request().withPath("/todoItemUpdate"), VerificationTimes.once());
                return true;
            } catch (Exception e) {
                return false;
            }
        };
    }

}
