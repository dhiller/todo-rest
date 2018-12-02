package de.dhiller.todo.rest;

public class TodoDTO {

    private Long id;
    private Boolean done;
    private String content;

    public TodoDTO() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setDone(Boolean done) {
        this.done = done;
    }

    public Boolean isDone() {
        return done;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
