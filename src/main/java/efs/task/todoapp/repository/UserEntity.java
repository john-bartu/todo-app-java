package efs.task.todoapp.repository;

import efs.task.todoapp.service.BadRequest;

import java.util.Base64;

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
        StringBuilder token = new StringBuilder();
        token.append(Base64.getEncoder().encodeToString(username.getBytes()));
        token.append(":");
        token.append(Base64.getEncoder().encodeToString(password.getBytes()));

        return token.toString();
    }

    public boolean Validate() throws BadRequest {

        if (getUsername() == null || getUsername().equals("")) {
            throw new BadRequest("Validation: Username not provided");
        }

        if (getPassword() == null || getPassword().equals("")) {
            throw new BadRequest("Validation: Password not provided");
        }

        return true;

    }
}
