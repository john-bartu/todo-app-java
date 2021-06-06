package efs.task.todoapp;

import efs.task.todoapp.util.ToDoServerExtension;
import efs.task.todoapp.web.HttpCode;
import efs.task.todoapp.web.HttpMethode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.concurrent.Flow;

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


    @Test()
    @Timeout(1)
    void testCreatingUser() throws IOException, InterruptedException {

        HttpRequest httpRequest;

        httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "/user"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"username\": \"janKowalski\", \"password\": \"am!sK#123\"}"))
                .build();


        System.out.println("PRZED: ");
        var httpResponse = httpClient.send(httpRequest, ofString());
        System.out.println("RECEIVED: " + httpResponse.body());
        //then
        assertThat(httpResponse.statusCode()).as("Response status code").isEqualTo(HttpCode.Created.getrCode());


    }

}