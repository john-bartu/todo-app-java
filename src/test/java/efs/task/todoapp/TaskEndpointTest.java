package efs.task.todoapp;

import efs.task.todoapp.util.ToDoServerExtension;
import efs.task.todoapp.web.HttpCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.Base64;

import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(ToDoServerExtension.class)
class TaskEndpointTest {

    public static final String TODO_APP_PATH = "http://localhost:8080/todo";

    private HttpClient httpClient;

    @BeforeEach
    void setUp() {
        httpClient = HttpClient.newHttpClient();
    }


    @Test
    @Timeout(1)
    void shouldAddTask() throws IOException, InterruptedException {

        String username = "user";
        String password = "password";

        //given
        var createUserRequest = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "/user"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}"))
                .build();

        //when
        var httpResponseUser = httpClient.send(createUserRequest, ofString());

        //then
        assertThat(httpResponseUser.statusCode()).as("Response create user").isEqualTo(HttpCode.Created_201.getrCode());

        StringBuilder token = new StringBuilder();
        token.append(Base64.getEncoder().encodeToString(username.getBytes()));
        token.append(":");
        token.append(Base64.getEncoder().encodeToString(password.getBytes()));

        System.out.println("HASH: " + token);


        var createTaskRequest = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "/task"))
                .header("Content-Type", "application/json")
                .header("Auth", token.toString())
                .POST(HttpRequest.BodyPublishers.ofString("{\"description\": \"Kup mleko\",\"due\": \"2021-06-30\"}"))
                .build();

        //when
        var httpResponseTask = httpClient.send(createTaskRequest, ofString());

        //then
        assertThat(httpResponseTask.statusCode()).as("Response create task for user").isEqualTo(HttpCode.Created_201.getrCode());


    }

    @Test
    @Timeout(1)
    void shouldGetTaskList() throws IOException, InterruptedException {

        String username = "user";
        String password = "password";


        StringBuilder token = new StringBuilder();
        token.append(Base64.getEncoder().encodeToString(username.getBytes()));
        token.append(":");
        token.append(Base64.getEncoder().encodeToString(password.getBytes()));

        System.out.println("HASH: " + token);


        var listTaskRequest = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "/task"))
                .header("Content-Type", "application/json")
                .header("Auth", token.toString())
                .GET()
                .build();

        //when
        var httpResponseTask = httpClient.send(listTaskRequest, ofString());

        //then
        assertThat(httpResponseTask.statusCode()).as("Response create task for user").isEqualTo(HttpCode.OK_200.getrCode());


    }
}