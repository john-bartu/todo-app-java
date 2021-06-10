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
import java.util.List;

import static efs.task.todoapp.util.TestUtils.createToken;
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
        assertThat(httpResponseUser.statusCode()).as("Response create user").isEqualTo(HttpCode.CREATED_201.getResponseCode());

        String token = createToken(username, password);

        var createTaskRequest = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "/task"))
                .header("Content-Type", "application/json")
                .header("Auth", token)
                .POST(HttpRequest.BodyPublishers.ofString("{\"description\": \"Kup mleko\",\"due\": \"2021-06-30\"}"))
                .build();

        //when
        var httpResponseTask = httpClient.send(createTaskRequest, ofString());

        //then
        assertThat(httpResponseTask.statusCode()).as("Response create task for user").isEqualTo(HttpCode.CREATED_201.getResponseCode());


    }


    @Test
    @Timeout(1)
    void shouldNotAddTask() throws IOException, InterruptedException {

        String username = "userTest";
        String password = "passwordTest";

        //given
        var createUserRequest = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "/user"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}"))
                .build();

        //when
        var httpResponseUser = httpClient.send(createUserRequest, ofString());

        //then
        assertThat(httpResponseUser.statusCode()).as("Response create user").isEqualTo(HttpCode.CREATED_201.getResponseCode());

        String token = createToken(username, password);

        System.out.println("HASH: " + token);


        var createTaskRequest = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "/task"))
                .header("Content-Type", "application/json")
                .header("Auth", token)
                .POST(HttpRequest.BodyPublishers.ofString("{\"description\": \"Kup mleko\",\"due\": \"2021-30-06\"}"))
                .build();

        //when
        var httpResponseTask = httpClient.send(createTaskRequest, ofString());

        //then
        assertThat(httpResponseTask.statusCode()).as("Response create task for user").isEqualTo(HttpCode.BAD_REQUEST_400.getResponseCode());


    }


    @Test
    @Timeout(1)
    void shouldNotGetTaskList_NoUser() throws IOException, InterruptedException {

        String username = "this";
        String password = "not_exist";
        String token = createToken(username, password);


        var listTaskRequest = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "/task"))
                .header("Content-Type", "application/json")
                .header("Auth", token)
                .GET()
                .build();

        //when
        var httpResponseTask = httpClient.send(listTaskRequest, ofString());

        //then
        assertThat(httpResponseTask.statusCode()).as("Response create task for user").isEqualTo(HttpCode.UNAUTHORIZED_401.getResponseCode());
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
        assertThat(httpResponseTask.statusCode()).as("Response create task for user").isEqualTo(HttpCode.BAD_REQUEST_400.getResponseCode());
    }


    @Test
    @Timeout(1)
    void shouldGetTaskList() throws IOException, InterruptedException {

        String username = "user";
        String password = "password";
        String token = createToken(username, password);

        System.out.println("HASH: " + token);


        var listTaskRequest = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "/task"))
                .header("Content-Type", "application/json")
                .header("Auth", token)
                .GET()
                .build();

        //when
        var httpResponseTask = httpClient.send(listTaskRequest, ofString());

        //then
        assertThat(httpResponseTask.statusCode()).as("Response create task for user").isEqualTo(HttpCode.OK_200.getResponseCode());
    }

    @Test
    @Timeout(1)
    void shouldGetTask() throws IOException, InterruptedException {

        String username = "user";
        String password = "password";
        String token = createToken(username, password);

        System.out.println("HASH: " + token);


        var listTaskRequest = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "/task"))
                .header("Content-Type", "application/json")
                .header("Auth", token)
                .GET()
                .build();

        //when
        var httpResponseTaskList = httpClient.send(listTaskRequest, ofString());

        //then
        assertThat(httpResponseTaskList.statusCode()).as("Response create task for user").isEqualTo(HttpCode.OK_200.getResponseCode());

        TaskEntity[] taskEntities = new Gson().fromJson(httpResponseTaskList.body(), TaskEntity[].class);
        List<TaskEntity> taskEntityList = Arrays.asList(taskEntities);


        var getTaskRequest = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "/task/" + taskEntities[0].getId()))
                .header("Content-Type", "application/json")
                .header("Auth", token)
                .GET()
                .build();

        //when
        var httpResponseTask = httpClient.send(getTaskRequest, ofString());
        assertThat(httpResponseTask.statusCode()).as("Response get task").isEqualTo(HttpCode.OK_200.getResponseCode());
    }


    @Test
    @Timeout(1)
    void shouldDeleteTask() throws IOException, InterruptedException {

        String username = "user";
        String password = "password";
        String token = createToken(username, password);

        var listTaskRequest = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "/task"))
                .header("Content-Type", "application/json")
                .header("Auth", token)
                .GET()
                .build();

        //when
        var httpResponseTaskList = httpClient.send(listTaskRequest, ofString());

        //then
        assertThat(httpResponseTaskList.statusCode()).as("Response create task for user").isEqualTo(HttpCode.OK_200.getResponseCode());

        TaskEntity[] taskEntities = new Gson().fromJson(httpResponseTaskList.body(), TaskEntity[].class);
        List<TaskEntity> taskEntityList = Arrays.asList(taskEntities);


        var deleteTaskRequest = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "/task/" + taskEntities[0].getId()))
                .header("Content-Type", "application/json")
                .header("Auth", token)
                .DELETE()
                .build();

        //when
        var httpResponseDeleteTask = httpClient.send(deleteTaskRequest, ofString());
        assertThat(httpResponseDeleteTask.statusCode()).as("Response get task").isEqualTo(HttpCode.OK_200.getResponseCode());


        var getTaskRequest = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "/task/" + taskEntities[0].getId()))
                .header("Content-Type", "application/json")
                .header("Auth", token)
                .GET()
                .build();

        //when
        var httpResponseTask = httpClient.send(getTaskRequest, ofString());
        assertThat(httpResponseTask.statusCode()).as("Response get task").isEqualTo(HttpCode.NOT_FOUND_404.getResponseCode());
    }

    @Test
    @Timeout(1)
    void shouldAddTaskAndReceive3Tasks() throws IOException, InterruptedException {

        String username = "user3";
        String password = "password3";

        //given
        var createUserRequest = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "/user"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}"))
                .build();

        //when
        var httpResponseUser = httpClient.send(createUserRequest, ofString());

        //then
        assertThat(httpResponseUser.statusCode()).as("Response create user").isEqualTo(HttpCode.CREATED_201.getResponseCode());

        String token = createToken(username, password);

        System.out.println("HASH: " + token);

        for (int i = 0; i < 3; i++) {

            var createTaskRequest = HttpRequest.newBuilder()
                    .uri(URI.create(TODO_APP_PATH + "/task"))
                    .header("Content-Type", "application/json")
                    .header("Auth", token)
                    .POST(HttpRequest.BodyPublishers.ofString("{\"description\": \"TASK:" + i + "\",\"due\": \"2021-06-30\"}"))
                    .build();

            var httpResponseTask = httpClient.send(createTaskRequest, ofString());
            //then
            assertThat(httpResponseTask.statusCode()).as("Response create task for user").isEqualTo(HttpCode.CREATED_201.getResponseCode());


        }
        var listTaskRequest = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "/task"))
                .header("Content-Type", "application/json")
                .header("Auth", token)
                .GET()
                .build();

        //when
        var httpResponseTaskList = httpClient.send(listTaskRequest, ofString());

        //then
        assertThat(httpResponseTaskList.statusCode()).as("Response create task for user").isEqualTo(HttpCode.OK_200.getResponseCode());

        TaskEntity[] taskEntities = new Gson().fromJson(httpResponseTaskList.body(), TaskEntity[].class);
        List<TaskEntity> taskEntityList = Arrays.asList(taskEntities);
        assertThat(taskEntityList.size()).as("Response create task for user").isEqualTo(3);


    }
}