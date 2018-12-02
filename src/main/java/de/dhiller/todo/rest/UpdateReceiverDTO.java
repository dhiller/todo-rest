package de.dhiller.todo.rest;

import java.net.URI;

public class UpdateReceiverDTO {

    private URI endpoint;

    public UpdateReceiverDTO() {}

    public UpdateReceiverDTO(URI endpoint) {
        this.endpoint = endpoint;
    }

    public URI getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(URI endpoint) {
        this.endpoint = endpoint;
    }

}
