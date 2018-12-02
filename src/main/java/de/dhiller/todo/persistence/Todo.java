package de.dhiller.todo.persistence;

import javax.persistence.*;

@Entity
@TableGenerator(name="todoIds", initialValue=10, allocationSize=10)
public class Todo {

    @Id
    @GeneratedValue(strategy=GenerationType.TABLE, generator = "todoIds")
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

    public void setUser(User user) {
        this.user = user;
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
