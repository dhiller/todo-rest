package de.dhiller.todo.persistence;

import javax.persistence.*;

@Entity
public class Todo {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "userId")
    private User user;

    private boolean done;

    private String content;

    public Todo() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
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
