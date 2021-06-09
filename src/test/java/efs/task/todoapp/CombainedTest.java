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
import java.util.Base64;
import java.util.UUID;

import static efs.task.todoapp.util.TestUtils.*;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(ToDoServerExtension.class)
class CombainedTest {

    public static final String TODO_APP_PATH = "http://localhost:8080/todo";

    private HttpClient httpClient;


    @BeforeEach
    void setUp() {
        httpClient = HttpClient.newHttpClient();
    }

    TestResponse addUser(String username, String password) throws IOException, InterruptedException {
        //given
        var createUserRequest = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "/user"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}"))
                .build();

        //when
        var httpResponseUser = httpClient.send(createUserRequest, ofString());

        return new TestResponse(httpResponseUser.statusCode());
    }

    TestTaskResponse addTask(String auth, String description, String due) throws IOException, InterruptedException {
        var createTaskRequest = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "/task"))
                .header("Content-Type", "application/json")
                .header("Auth", auth)
                .POST(HttpRequest.BodyPublishers.ofString("{\"description\": \"" + description + "\",\"due\": \"" + due + "\"}"))
                .build();

        var httpResponseTask = httpClient.send(createTaskRequest, ofString());

        TaskEntity taskEntity = new Gson().fromJson(httpResponseTask.body(), TaskEntity.class);

        System.out.println(httpResponseTask.statusCode());
        return new TestTaskResponse(httpResponseTask.statusCode(), taskEntity);
    }

    TestTaskResponse getTask(String auth, String uuid) throws IOException, InterruptedException {

        var getTaskRequest = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "/task/" + uuid))
                .header("Content-Type", "application/json")
                .header("Auth", auth)
                .GET()
                .build();

        //when
        var httpResponseTask = httpClient.send(getTaskRequest, ofString());
        TaskEntity task = new Gson().fromJson(httpResponseTask.body(), TaskEntity.class);
        return new TestTaskResponse(httpResponseTask.statusCode(), task);
    }

    TestResponse deleteTask(String auth, String uuid) throws IOException, InterruptedException {

        var getTaskRequest = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "/task/" + uuid))
                .header("Content-Type", "application/json")
                .header("Auth", auth)
                .DELETE()
                .build();

        //when
        var httpResponseTask = httpClient.send(getTaskRequest, ofString());
        return new TestResponse(httpResponseTask.statusCode());
    }

    TestTaskResponse putTask(String auth, String uuid, String description, String due) throws IOException, InterruptedException {
        var createTaskRequest = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "/task/" + uuid))
                .header("Content-Type", "application/json")
                .header("Auth", auth)
                .PUT(HttpRequest.BodyPublishers.ofString("{\"description\": \"" + description + "\",\"due\": \"" + due + "\"}"))
                .build();

        var httpResponseTask = httpClient.send(createTaskRequest, ofString());

        TaskEntity taskEntity = new Gson().fromJson(httpResponseTask.body(), TaskEntity.class);

        System.out.println(httpResponseTask.statusCode());
        return new TestTaskResponse(httpResponseTask.statusCode(), taskEntity);
    }


    @Test
    @Timeout(1)
    void Should_AddUser_AddTask_GetTask() throws IOException, InterruptedException {
        String login = randomLogin();
        String password = randomLogin();
        String token = creteToken(login, password);

        TestResponse addUserResponse = addUser(login, password);

        assertThat(addUserResponse.code).isEqualTo(HttpCode.CREATED_201.getrCode());

        TestTaskResponse addTaskResponse = addTask(token, "Kuba musi buy cos", "2021-06-30");

        assertThat(addTaskResponse.code).isEqualTo(HttpCode.CREATED_201.getrCode());

        TestTaskResponse getTaskResponse = getTask(token, addTaskResponse.taskEntity.getId().toString());
        assertThat(getTaskResponse.code).isEqualTo(HttpCode.OK_200.getrCode());
        assertThat(getTaskResponse.taskEntity.getId()).isEqualTo(addTaskResponse.taskEntity.getId());
    }

    @Test
    @Timeout(1)
    void Should_AddTwoUsers_AddTask_RandomUser401() throws IOException, InterruptedException {
        String login = randomLogin();
        String password = randomLogin();
        String token = creteToken(login, password);

        String token2 = creteRandomToken();

        TestResponse addUserResponse = addUser(login, password);
        assertThat(addUserResponse.code).isEqualTo(HttpCode.CREATED_201.getrCode());

        TestTaskResponse addTaskResponse = addTask(token, "Kuba musi buy cos", "2021-06-30");

        assertThat(addTaskResponse.code).isEqualTo(HttpCode.CREATED_201.getrCode());

        TestTaskResponse getTaskResponse = getTask(token, addTaskResponse.taskEntity.getId().toString());
        assertThat(getTaskResponse.code).isEqualTo(HttpCode.OK_200.getrCode());
        assertThat(getTaskResponse.taskEntity.getId()).isEqualTo(addTaskResponse.taskEntity.getId());

        TestTaskResponse get2TaskResponse = getTask(token2, addTaskResponse.taskEntity.getId().toString());
        assertThat(get2TaskResponse.code).isEqualTo(HttpCode.UNAUTHORIZED_401.getrCode());
    }

    @Test
    @Timeout(1)
    void Should_AddUser_Tasks404() throws IOException, InterruptedException {
        String login = randomLogin();
        String password = randomLogin();
        String token = creteToken(login, password);

        TestResponse addUserResponse = addUser(login, password);
        assertThat(addUserResponse.code).isEqualTo(HttpCode.CREATED_201.getrCode());

        TestTaskResponse addTaskResponse = addTask(token, "Kuba musi buy cos", "2021-06-30");

        assertThat(addTaskResponse.code).isEqualTo(HttpCode.CREATED_201.getrCode());

        TestTaskResponse getTaskResponse = getTask(token, UUID.randomUUID().toString());
        assertThat(getTaskResponse.code).isEqualTo(HttpCode.NOT_FOUND_404.getrCode());

    }

    @Test
    @Timeout(1)
    void Should_AddTwoUsers_AddTask_SecondUser403() throws IOException, InterruptedException {
        String login = randomLogin();
        String password = randomLogin();

        String login2 = randomLogin();
        String password2 = randomLogin();

        String token = creteToken(login, password);
        String token2 = creteToken(login2, password2);


        TestResponse response = addUser(login, password);
        assertThat(response.code).isEqualTo(HttpCode.CREATED_201.getrCode());

        TestResponse response2 = addUser(login2, password2);
        assertThat(response2.code).isEqualTo(HttpCode.CREATED_201.getrCode());

        TestTaskResponse addTaskResponse = addTask(token, "Kuba musi buy cos", "2021-06-30");

        assertThat(addTaskResponse.code).isEqualTo(HttpCode.CREATED_201.getrCode());

        TestTaskResponse getTaskResponse = getTask(token, addTaskResponse.taskEntity.getId().toString());
        assertThat(getTaskResponse.code).isEqualTo(HttpCode.OK_200.getrCode());
        assertThat(getTaskResponse.taskEntity.getId()).isEqualTo(addTaskResponse.taskEntity.getId());

        TestTaskResponse get3TaskResponse = getTask(token2, addTaskResponse.taskEntity.getId().toString());
        assertThat(get3TaskResponse.code).isEqualTo(HttpCode.FORBIDDEN_403.getrCode());
    }

    @Test
    @Timeout(1)
    void Multiple_Testing_Adding_Task() throws IOException, InterruptedException {
        String login = randomLogin();
        String password = randomLogin();
        String token = creteToken(login, password);

        String token2 = creteRandomToken();

        TestResponse addUserResponse = addUser(login, password);

        assertThat(addUserResponse.code).isEqualTo(HttpCode.CREATED_201.getrCode());

        TestTaskResponse addTaskResponse;

        addTaskResponse = addTask(token, "Test1", "2021-06-30");
        assertThat(addTaskResponse.code).isEqualTo(HttpCode.CREATED_201.getrCode());

        addTaskResponse = addTask(token, "Test2", "2021-30-06");
        assertThat(addTaskResponse.code).isEqualTo(HttpCode.BAD_REQUEST_400.getrCode());

        addTaskResponse = addTask(token, "", "2021-30-06");
        assertThat(addTaskResponse.code).isEqualTo(HttpCode.BAD_REQUEST_400.getrCode());

        addTaskResponse = addTask(token, "", "");
        assertThat(addTaskResponse.code).isEqualTo(HttpCode.BAD_REQUEST_400.getrCode());

        addTaskResponse = addTask(token, "Test5", "");
        assertThat(addTaskResponse.code).isEqualTo(HttpCode.BAD_REQUEST_400.getrCode());

        addTaskResponse = addTask(token2, "Test6", "2021-06-30");
        assertThat(addTaskResponse.code).isEqualTo(HttpCode.UNAUTHORIZED_401.getrCode());
    }

    @Test
    @Timeout(1)
    void Should_AddUser_AddTask_DeleteTask_NotGetTask() throws IOException, InterruptedException {
        String login = randomLogin();
        String password = randomLogin();

        String token = creteToken(login, password);

        TestResponse resposne = addUser(login, password);

        assertThat(resposne.code).isEqualTo(HttpCode.CREATED_201.getrCode());

        TestTaskResponse addTaskResponse = addTask(token, "Kuba musi buy cos", "2021-06-30");

        assertThat(addTaskResponse.code).isEqualTo(HttpCode.CREATED_201.getrCode());

        TestTaskResponse getTaskResponse = getTask(token, addTaskResponse.taskEntity.getId().toString());
        assertThat(getTaskResponse.code).isEqualTo(HttpCode.OK_200.getrCode());
        assertThat(getTaskResponse.taskEntity.getId()).isEqualTo(addTaskResponse.taskEntity.getId());

        TestResponse deleteTaskResponse = deleteTask(token, addTaskResponse.taskEntity.getId().toString());
        assertThat(getTaskResponse.code).isEqualTo(HttpCode.OK_200.getrCode());

        TestTaskResponse notGetTaskResponse = getTask(token, addTaskResponse.taskEntity.getId().toString());
        assertThat(notGetTaskResponse.code).isEqualTo(HttpCode.NOT_FOUND_404.getrCode());

    }

    @Test
    @Timeout(1)
    void Should_AddUser_AddTask_ShouldNotGetTask() throws IOException, InterruptedException {
        String login = randomLogin();
        String password = randomLogin();

        StringBuilder token = new StringBuilder();
        token.append(Base64.getEncoder().encodeToString(login.getBytes()));
        token.append(":");
        token.append(Base64.getEncoder().encodeToString(password.getBytes()));

        TestResponse addUserResponse = addUser(login, password);

        assertThat(addUserResponse.code).isEqualTo(HttpCode.CREATED_201.getrCode());

        TestTaskResponse addTaskResponse = addTask(token.toString(), "Kuba musi buy cos", "2021-06-30");

        assertThat(addTaskResponse.code).isEqualTo(HttpCode.CREATED_201.getrCode());

        TestTaskResponse getTaskResponse = getTask(token.toString(), UUID.randomUUID().toString());
        assertThat(getTaskResponse.code).isEqualTo(HttpCode.NOT_FOUND_404.getrCode());
    }

    @Test
    @Timeout(1)
    void Should_AddUser_AddTask_PutTask() throws IOException, InterruptedException {
        String login = randomLogin();
        String password = randomLogin();
        String token = creteToken(login, password);

        TestResponse addUserResponse = addUser(login, password);

        assertThat(addUserResponse.code).isEqualTo(HttpCode.CREATED_201.getrCode());

        TestTaskResponse addTaskResponse = addTask(token, "Kuba musi buy cos", "2021-06-30");

        assertThat(addTaskResponse.code).isEqualTo(HttpCode.CREATED_201.getrCode());

        TestTaskResponse getTaskResponse = putTask(token, addTaskResponse.taskEntity.getId().toString(), "New Description", "2021-06-30");
        assertThat(getTaskResponse.code).isEqualTo(HttpCode.OK_200.getrCode());
    }

    @Test
    @Timeout(1)
    void Should_AddTwoUser_AddTask_PutTask() throws IOException, InterruptedException {
        String login = randomLogin();
        String password = randomLogin();

        String login2 = randomLogin();
        String password2 = randomLogin();

        String token = creteToken(login, password);
        String token2 = creteToken(login2, password2);


        TestResponse addUserResponse = addUser(login, password);
        assertThat(addUserResponse.code).isEqualTo(HttpCode.CREATED_201.getrCode());


        TestResponse addUser2Response = addUser(login2, password2);
        assertThat(addUser2Response.code).isEqualTo(HttpCode.CREATED_201.getrCode());

        TestTaskResponse addTaskResponse = addTask(token, "Kuba musi buy cos", "2021-06-30");

        assertThat(addTaskResponse.code).isEqualTo(HttpCode.CREATED_201.getrCode());

        TestTaskResponse getTaskResponse = putTask(token2, addTaskResponse.taskEntity.getId().toString(), "New Description", "2021-06-30");
        assertThat(getTaskResponse.code).isEqualTo(HttpCode.FORBIDDEN_403.getrCode());
    }

}