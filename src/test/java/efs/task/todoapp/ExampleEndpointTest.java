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

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;

import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(ToDoServerExtension.class)
class ExampleEndpointTest {

    public static final String TODO_APP_PATH = "http://localhost:8080";

    private HttpClient httpClient;

    @BeforeEach
    void setUp() {
        httpClient = HttpClient.newHttpClient();
    }

    @Test
    @Timeout(1)
    void shouldReturnNotFoundStatusForUnhandledPaths() throws IOException, InterruptedException {
        //given
        var httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "/todo/non/existing/endpoint"))
                .GET()
                .build();

        //when
        var httpResponse = httpClient.send(httpRequest, ofString());

        //then
        assertThat(httpResponse.statusCode()).as("Response status code").isEqualTo(HttpCode.NotFound_404.getrCode());
    }

    @ParameterizedTest(name = "{index}: path={0}, methode={1}")
    @CsvSource({"/todo/user,POST,400", "/todo/task,POST,200", "/todo/task,GET,200"
            , "/todo/task/1,GET,200", "/todo/task/1,PUT,200", "/todo/task/1,DELETE,200"})
    @Timeout(1)
    void shouldReturnOKStatusForPaths(String path, String str_methode, int rCode) throws IOException, InterruptedException {
        HttpMethode methode = HttpMethode.valueOf(str_methode);

        HttpRequest httpRequest;


        System.out.println("Execute: " + path + " | " + str_methode);
        switch (methode) {
            case GET: {
                httpRequest = HttpRequest.newBuilder()
                        .uri(URI.create(TODO_APP_PATH + path))
                        .GET()
                        .build();
                break;
            }
            case PUT: {
                httpRequest = HttpRequest.newBuilder()
                        .uri(URI.create(TODO_APP_PATH + path))
                        .header("Content-Type", "application/json")
                        .PUT(HttpRequest.BodyPublishers.ofString(""))
                        .build();
                break;
            }
            case POST: {

                httpRequest = HttpRequest.newBuilder()
                        .uri(URI.create(TODO_APP_PATH + path))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(""))
                        .build();
                break;
            }
            case DELETE: {
                httpRequest = HttpRequest.newBuilder()
                        .uri(URI.create(TODO_APP_PATH + path))
                        .header("Content-Type", "application/json")
                        .DELETE()
                        .build();
                break;
            }
            default: {
                assertThat(true).isEqualTo(false);
                httpRequest = HttpRequest.newBuilder()
                        .uri(URI.create(TODO_APP_PATH + path))
                        .GET()
                        .build();
                break;
            }
        }

        var httpResponse = httpClient.send(httpRequest, ofString());

        System.out.println(httpResponse.body());
        //then
        assertThat(httpResponse.statusCode()).as("Response status code").isEqualTo(rCode);


    }
}