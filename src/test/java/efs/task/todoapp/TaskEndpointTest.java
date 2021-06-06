package efs.task.todoapp;

import com.google.gson.Gson;
import efs.task.todoapp.repository.TaskEntity;
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
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

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
    void shouldNotGetTaskList_NoUser() throws IOException, InterruptedException {

        String username = "this";
        String password = "not_exist";


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
        assertThat(httpResponseTask.statusCode()).as("Response create task for user").isEqualTo(HttpCode.Unauthorized_401.getrCode());
    }

    @Test
    @Timeout(1)
    void shouldNotGetTaskList_NoHeader() throws IOException, InterruptedException {

        var listTaskRequest = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "/task"))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        //when
        var httpResponseTask = httpClient.send(listTaskRequest, ofString());

        //then
        assertThat(httpResponseTask.statusCode()).as("Response create task for user").isEqualTo(HttpCode.BadRequest_400.getrCode());
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

    @Test
    @Timeout(1)
    void shouldGetTask() throws IOException, InterruptedException {

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
        var httpResponseTaskList = httpClient.send(listTaskRequest, ofString());

        //then
        assertThat(httpResponseTaskList.statusCode()).as("Response create task for user").isEqualTo(HttpCode.OK_200.getrCode());

        TaskEntity[] taskEntities = new Gson().fromJson(httpResponseTaskList.body(), TaskEntity[].class);
        List<TaskEntity> taskEntityList = Arrays.asList(taskEntities);


        var getTaskRequest = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "/task/" + taskEntities[0].getId()))
                .header("Content-Type", "application/json")
                .header("Auth", token.toString())
                .GET()
                .build();

        //when
        var httpResponseTask = httpClient.send(getTaskRequest, ofString());
        assertThat(httpResponseTask.statusCode()).as("Response get task").isEqualTo(HttpCode.OK_200.getrCode());
    }


    @Test
    @Timeout(1)
    void shouldDeleteTask() throws IOException, InterruptedException {

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
        var httpResponseTaskList = httpClient.send(listTaskRequest, ofString());

        //then
        assertThat(httpResponseTaskList.statusCode()).as("Response create task for user").isEqualTo(HttpCode.OK_200.getrCode());

        TaskEntity[] taskEntities = new Gson().fromJson(httpResponseTaskList.body(), TaskEntity[].class);
        List<TaskEntity> taskEntityList = Arrays.asList(taskEntities);


        var deleteTaskRequest = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "/task/" + taskEntities[0].getId()))
                .header("Content-Type", "application/json")
                .header("Auth", token.toString())
                .DELETE()
                .build();

        //when
        var httpResponseDeleteTask = httpClient.send(deleteTaskRequest, ofString());
        assertThat(httpResponseDeleteTask.statusCode()).as("Response get task").isEqualTo(HttpCode.OK_200.getrCode());


        var getTaskRequest = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "/task/" + taskEntities[0].getId()))
                .header("Content-Type", "application/json")
                .header("Auth", token.toString())
                .GET()
                .build();

        //when
        var httpResponseTask = httpClient.send(getTaskRequest, ofString());
        assertThat(httpResponseTask.statusCode()).as("Response get task").isEqualTo(HttpCode.NotFound_404.getrCode());
    }
}