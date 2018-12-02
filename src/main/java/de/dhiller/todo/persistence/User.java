package de.dhiller.todo.persistence;

import javax.persistence.*;

@Entity
@TableGenerator(name="userIds", initialValue=10, allocationSize=10)
public class User {

    @Id
    @GeneratedValue(strategy=GenerationType.TABLE, generator = "userIds")
    private Long id;

    @Column(unique = true)
    private String username;

    private String realname;

    private String password;

    private String salt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRealname() {
        return realname;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }
}
