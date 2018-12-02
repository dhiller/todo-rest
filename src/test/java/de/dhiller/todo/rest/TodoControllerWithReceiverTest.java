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
    public void updateItemWithReceiver() throws Exception {
        performPutUpdateReceiver(new UpdateReceiverDTO(URI.create("http://localhost:8081/todoItemUpdate")), authenticationToken);

        mockServerClient.when(request().withMethod("PUT").withPath("/todoItemUpdate")).respond(HttpResponse.response().withStatusCode(200));

        performUpdate(authenticationToken, todo);

        mockServerClient.verify(request().withPath("/todoItemUpdate"), VerificationTimes.once());
    }

}
