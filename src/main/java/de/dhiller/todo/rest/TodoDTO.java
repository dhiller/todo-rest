package de.dhiller.todo.rest;

public class TodoDTO {

    private long id;
    private boolean done;
    private String content;

    public TodoDTO() {}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public boolean isDone() {
        return done;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
