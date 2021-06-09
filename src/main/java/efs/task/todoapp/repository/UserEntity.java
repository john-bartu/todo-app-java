package efs.task.todoapp.repository;

import efs.task.todoapp.service.exceptions.BadRequest;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class UserEntity {
    String username;
    String password;

    public UserEntity(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public static void Validate(UserEntity userEntity) throws BadRequest {

        if (userEntity == null) {
            throw new BadRequest("Validation: UserEntity is null");
        }

        if (userEntity.getUsername() == null || userEntity.getUsername().equals("")) {
            throw new BadRequest("Validation: Username not provided");
        }

        if (userEntity.getPassword() == null || userEntity.getPassword().equals("")) {
            throw new BadRequest("Validation: Password not provided");
        }

    }

    public static void checkToken(String token) throws BadRequest {

        List<String> test = Arrays.asList(token.split("[\\s:]+"));

        try {
            Base64.getDecoder().decode(test.get(0));
            Base64.getDecoder().decode(test.get(1));

        } catch (IllegalArgumentException e) {
            throw new BadRequest("Token is not a token");
        }

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
}
