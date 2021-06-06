package efs.task.todoapp.repository;

import java.util.Base64;
import java.util.Objects;
import java.util.UUID;

public class UserEntity {
    String username;
    String password;

    public UserEntity(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserEntity that = (UserEntity) o;

        return username.equals(that.username);
    }

    @Override
    public int hashCode() {
        return username != null ? username.hashCode() : 0;
    }

    public String encode() {
        return Base64.getEncoder().encodeToString((getUsername()).getBytes()) + ":" + Base64.getEncoder().encodeToString((getPassword()).getBytes());
    }
}
