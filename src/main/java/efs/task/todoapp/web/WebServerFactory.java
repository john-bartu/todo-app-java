package efs.task.todoapp.web;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import efs.task.todoapp.repository.TaskEntity;
import efs.task.todoapp.repository.TaskRepository;
import efs.task.todoapp.repository.UserEntity;
import efs.task.todoapp.repository.UserRepository;
import efs.task.todoapp.service.ToDoService;
import efs.task.todoapp.service.exceptions.*;
import efs.task.todoapp.web.annotations.MethodEndPoint;
import efs.task.todoapp.web.annotations.URIEndPoint;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebServerFactory {
    private static final Logger LOGGER = Logger.getLogger(WebServerFactory.class.getName());
    private static final ToDoService database = new ToDoService(new UserRepository(), new TaskRepository());

    public static HttpServer createServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        Set<String> urls = new HashSet<>();

        Class<?>[] endpointClasses = WebServerFactory.class.getDeclaredClasses();


        LOGGER.info("Registering endpoints...");


        for (Class<?> endpointClass : endpointClasses) {
            URIEndPoint annotation = endpointClass.getAnnotation(URIEndPoint.class);
            if (annotation != null) {
                String url = annotation.path();

                Constructor<?> constructor;
                try {
                    constructor = endpointClass.getDeclaredConstructor();
                    constructor.setAccessible(true);
                    HttpHandler httpHandler = (HttpHandler) constructor.newInstance();
                    server.createContext(url, httpHandler);

                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                    e.printStackTrace();
                }

                urls.add(url);
            }
        }

        LOGGER.info("... registering endpoints done.");

        server.setExecutor(null); // creates a default executor
        return server;
    }

    @SuppressWarnings("unused")
    @URIEndPoint(path = "/todo/user")
    static class TodoUserEndpoint extends EndpointDefault {

        TodoUserEndpoint() {
            initMethodEndpoints(this);
        }

        @MethodEndPoint(method = HttpMethod.POST)
        static HttpResponse userHandlePost(Request t) throws BadRequestException, ConflictException {

            UserEntity newUser = new Gson().fromJson(t.getRequestBody(), UserEntity.class);
            LOGGER.info("Received user: " + new Gson().toJson(newUser));
            UserEntity.Validate(newUser);
            database.addUser(newUser);
            return new HttpResponse().toJson(HttpCode.CREATED_201, "User added");

        }

    }

    @SuppressWarnings("unused")
    @URIEndPoint(path = "/todo/task")
    static class TodoTasksEndpoint extends EndpointDefault {

        TodoTasksEndpoint() {
            initMethodEndpoints(this);
        }

        @MethodEndPoint(method = HttpMethod.GET)
        static HttpResponse taskHandleGet(Request t) throws BadRequestException, UnauthorizedException, JsonSyntaxException {

            String token = t.getHeaderAuth();

            String username = database.authenticate(token);

            LOGGER.info("USER: " + username);

            List<TaskEntity> taskEntities = database.getUserTasks(username);

            return new HttpResponse(HttpCode.OK_200, new Gson().toJson(taskEntities));


        }

        @MethodEndPoint(method = HttpMethod.POST)
        static HttpResponse taskHandlePost(Request t) throws BadRequestException, UnauthorizedException, JsonSyntaxException {

            TaskEntity newTask = new Gson().fromJson(t.getRequestBody(), TaskEntity.class);

            LOGGER.info("Received task: " + new Gson().toJson(newTask));
            String token = t.getHeaderAuth();

            TaskEntity.validate(newTask);

            String username = database.authenticate(token);

            database.addUserTask(username, newTask);
            return new HttpResponse(HttpCode.CREATED_201, "{\"id\":\"" + newTask.getId() + "\"}");


        }

        @MethodEndPoint(method = HttpMethod.PUT)
        static HttpResponse taskHandlePut(Request t) throws JsonSyntaxException {
            return new HttpResponse().toJson(HttpCode.BAD_REQUEST_400, "");
        }

        @MethodEndPoint(method = HttpMethod.DELETE)
        static HttpResponse taskHandleDelete(Request t) throws JsonSyntaxException {
            return new HttpResponse().toJson(HttpCode.BAD_REQUEST_400, "");
        }
    }

    @SuppressWarnings("unused")
    @URIEndPoint(path = "/todo/task/")
    static class TodoTaskEndpoint extends EndpointDefault {

        static Pattern pattern = Pattern.compile("^/todo/task/([A-Za-z0-9]{8}-[A-Za-z0-9]{4}-[A-Za-z0-9]{4}-[A-Za-z0-9]{4}-[A-Za-z0-9]{12})$");

        TodoTaskEndpoint() {
            initMethodEndpoints(this);
        }

        static UUID GetUuidFromUrl(String uri) throws BadRequestException {
            Matcher matcher = pattern.matcher(uri);

            if (matcher.matches()) {
                return UUID.fromString(matcher.group(1));
            } else {
                throw new BadRequestException("Task UUID not found in URI");
            }
        }

        @MethodEndPoint(method = HttpMethod.GET)
        static HttpResponse taskHandleGet(Request t) throws BadRequestException, UnauthorizedException, JsonSyntaxException {

            String token = t.getHeaderAuth();

            UUID uuid = GetUuidFromUrl(t.requestURI);

            String username = database.authenticate(token);
            if (!database.isTaskExists(uuid))
                return new HttpResponse().toJson(HttpCode.NOT_FOUND_404, "Task with given uuid does not exists");

            if (!database.isTaskBelongsToUser(username, uuid)) {
                return new HttpResponse().toJson(HttpCode.FORBIDDEN_403, "Task belongs to other user");
            }

            String taskStr = new Gson().toJson(database.getTask(uuid));

            return new HttpResponse(HttpCode.OK_200, taskStr);

        }

        @MethodEndPoint(method = HttpMethod.PUT)
        static HttpResponse taskHandlePut(Request t) throws BadRequestException, UnauthorizedException, JsonSyntaxException, ForbiddenException, NotFoundException {

            String token = t.getHeaderAuth();

            UUID uuid = GetUuidFromUrl(t.requestURI);

            TaskEntity updateTask = new Gson().fromJson(t.getRequestBody(), TaskEntity.class);
            LOGGER.info("Received task to update: " + new Gson().toJson(updateTask));
            TaskEntity.validate(updateTask);

            String username = database.authenticate(token);
            LOGGER.info("USER: " + username);

            updateTask.setId(uuid);

            String taskStr = new Gson().toJson(database.updateUserTask(username, updateTask));

            return new HttpResponse(HttpCode.OK_200, taskStr);

        }

        @MethodEndPoint(method = HttpMethod.DELETE)
        static HttpResponse taskHandleDelete(Request t) throws BadRequestException, UnauthorizedException, JsonSyntaxException, ForbiddenException, NotFoundException {

            String token = t.getHeaderAuth();

            UUID uuid = GetUuidFromUrl(t.requestURI);

            String username = database.authenticate(token);

            database.removeUserTask(username, uuid);

            return new HttpResponse().toJson(HttpCode.OK_200, "Task deleted");


        }
    }
}

