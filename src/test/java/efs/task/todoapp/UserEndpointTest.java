package efs.task.todoapp;

import efs.task.todoapp.util.ToDoServerExtension;
import efs.task.todoapp.web.HttpCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.Base64;

import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(ToDoServerExtension.class)
class UserEndpointTest {

    public static final String TODO_APP_PATH = "http://localhost:8080/todo";

    private HttpClient httpClient;

    @BeforeEach
    void setUp() {
        httpClient = HttpClient.newHttpClient();
    }


    @Timeout(1)
    @ParameterizedTest(name = "{index}: weight={0},height={1}")
    @CsvFileSource(resources = "/users_tests.csv", numLinesToSkip = 1, delimiter = '|')
    void testCreatingUserFile(String jsonData, int code) throws IOException, InterruptedException {

        HttpRequest httpRequest;

        httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "/user"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonData))
                .build();


        var httpResponse = httpClient.send(httpRequest, ofString());
        //then
        assertThat(httpResponse.statusCode()).as("Response status code").isEqualTo(code);


    }

    @ParameterizedTest(name = "{index}: path={0}, methode={1}")
    @CsvSource({
            "1,asdasdasd:,401",
            "1,:aasdasd,400",
            "1,asdasdasd,400",
            "1,,400",
    })
    void testUserToken() throws IOException, InterruptedException {

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