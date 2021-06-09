package efs.task.todoapp.web;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import efs.task.todoapp.repository.TaskEntity;
import efs.task.todoapp.repository.UserEntity;
import efs.task.todoapp.service.ToDoService;
import efs.task.todoapp.service.exceptions.*;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebServerFactory {
    private static final Logger LOGGER = Logger.getLogger(WebServerFactory.class.getName());
    private static final ToDoService database = new ToDoService();

    public static HttpServer createServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        Set<String> urls = new HashSet<>();

        Class<?>[] endpointClasses = WebServerFactory.class.getDeclaredClasses();


        LOGGER.info("[REGISTERED ENDPOINTS:]");

        for (Class<?> endpointClass : endpointClasses) {
            URIEndPoint annotation = endpointClass.getAnnotation(URIEndPoint.class);
            if (annotation != null) {
                String url = annotation.path();


//                System.out.println("\t" + url);


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

        server.setExecutor(null); // creates a default executor
        return server;
    }

    static class EndpointDefault implements HttpHandler {
        HashMap<HttpMethode, Method> methodHashMap = new HashMap<>();

        void InitMethodEndpoints(Object o) {

            Method[] endpointClasses = o.getClass().getDeclaredMethods();
            for (Method endpointMethode : endpointClasses) {
                MethodEndPoint annotation = endpointMethode.getAnnotation(MethodEndPoint.class);

                if (annotation != null) {
                    HttpMethode method = annotation.method();
                    methodHashMap.put(method, endpointMethode);
//                    System.out.println("\t\t" + method);
                }
            }
        }

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {

            Request request = new Request(
                    httpExchange.getRequestURI().toString(),
                    httpExchange.getRequestMethod(),
                    httpExchange.getRequestHeaders(),
                    new String(httpExchange.getRequestBody().readAllBytes())
            );
            LOGGER.info("[" + request.getRequestMethod() + "]\n"
                    + "URI: " + request.getRequestURI() + "\n"
                    + "HEADERS: " + request.getRequestHeaders().keySet() + "\n"
                    + "BODY: " + request.getRequestBody() + "\n"
            );

            HttpMethode requestMethode = HttpMethode.valueOf(request.getRequestMethod());
            HttpResponse httpResponse = defaultHandle(request);
            try {
                if (methodHashMap.containsKey(requestMethode)) {
                    LOGGER.info("Method found " + methodHashMap.get(requestMethode).getName());
                    httpResponse = (HttpResponse) methodHashMap.get(requestMethode).invoke(null, request);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                try {
                    throw e.getCause();
                } catch (JsonSyntaxException jsonSyntaxException) {
                    httpResponse = new HttpResponse().toJson(HttpCode.BAD_REQUEST_400, "JSON Parse error" + e.getMessage());
                } catch (BadRequest badRequest) {
                    httpResponse = new HttpResponse().toJson(HttpCode.BAD_REQUEST_400, badRequest.getMessage());
                } catch (Unauthorized unauthorized) {
                    httpResponse = new HttpResponse().toJson(HttpCode.UNAUTHORIZED_401, unauthorized.getMessage());
                } catch (Conflict conflict) {
                    httpResponse = new HttpResponse().toJson(HttpCode.CONFLICT_409, conflict.getMessage());
                } catch (NotFound notFound) {
                    httpResponse = new HttpResponse().toJson(HttpCode.NOT_FOUND_404, notFound.getMessage());
                } catch (Forbidden forbidden) {
                    httpResponse = new HttpResponse().toJson(HttpCode.FORBIDDEN_403, forbidden.getMessage());
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                    httpResponse = new HttpResponse().toJson(HttpCode.INTERNAL_SERVER_ERROR_500, throwable.getMessage());
                }
            }


            LOGGER.info("SERVER:\n[" + httpResponse.httpCode.rCode + "]: " + httpResponse.httpResponse);
            httpExchange.sendResponseHeaders(httpResponse.httpCode.rCode, httpResponse.getSize());
            httpExchange.getResponseHeaders().add("Content-type", "application/json");
            OutputStream os = httpExchange.getResponseBody();
            os.write(httpResponse.httpResponse.getBytes());
            os.close();

        }

        HttpResponse defaultHandle(Request t) {
            return new HttpResponse().toJson(HttpCode.NOT_FOUND_404, "For URI: " + t.getRequestURI());
        }


    }

    @SuppressWarnings("unused")
    @URIEndPoint(path = "/todo/user")
    static class TodoUserEndpoint extends EndpointDefault {
        TodoUserEndpoint() {
            InitMethodEndpoints(this);
        }

        @MethodEndPoint(method = HttpMethode.POST)
        static HttpResponse userHandlePost(Request t) throws BadRequest, Conflict {

            UserEntity newUser = new Gson().fromJson(t.getRequestBody(), UserEntity.class);
            LOGGER.info("Received user: " + new Gson().toJson(newUser));
            UserEntity.Validate(newUser);
            database.AddUser(newUser);
            return new HttpResponse().toJson(HttpCode.CREATED_201, "User added");

        }

    }

    @SuppressWarnings("unused")
    @URIEndPoint(path = "/todo/task")
    static class TodoTasksEndpoint extends EndpointDefault {

        TodoTasksEndpoint() {
            InitMethodEndpoints(this);
        }

        @MethodEndPoint(method = HttpMethode.GET)
        static HttpResponse taskHandleGet(Request t) throws BadRequest, Unauthorized, JsonSyntaxException {

            String token = t.getHeaderAuth();

            String username = database.authenticate(token);

            LOGGER.info("USER: " + username);

            List<TaskEntity> taskEntities = database.getUserTasks(username);

            return new HttpResponse(HttpCode.OK_200, new Gson().toJson(taskEntities));


        }

        @MethodEndPoint(method = HttpMethode.POST)
        static HttpResponse taskHandlePost(Request t) throws BadRequest, Unauthorized, JsonSyntaxException {

            TaskEntity newTask = new Gson().fromJson(t.getRequestBody(), TaskEntity.class);

            LOGGER.info("Received task: " + new Gson().toJson(newTask));
            String token = t.getHeaderAuth();

            TaskEntity.Validate(newTask);

            String username = database.authenticate(token);

            database.addUserTask(username, newTask);
            return new HttpResponse(HttpCode.CREATED_201, "{\"id\":\"" + newTask.getId() + "\"}");


        }

        @MethodEndPoint(method = HttpMethode.PUT)
        static HttpResponse taskHandlePut(Request t) throws JsonSyntaxException {
            return new HttpResponse().toJson(HttpCode.BAD_REQUEST_400, "");
        }

        @MethodEndPoint(method = HttpMethode.DELETE)
        static HttpResponse taskHandleDelete(Request t) throws JsonSyntaxException {
            return new HttpResponse().toJson(HttpCode.BAD_REQUEST_400, "");
        }
    }

    @SuppressWarnings("unused")
    @URIEndPoint(path = "/todo/task/")
    static class TodoTaskEndpoint extends EndpointDefault {

        static Pattern pattern = Pattern.compile("^/todo/task/([A-Za-z0-9]{8}-[A-Za-z0-9]{4}-[A-Za-z0-9]{4}-[A-Za-z0-9]{4}-[A-Za-z0-9]{12})$");

        TodoTaskEndpoint() {
            InitMethodEndpoints(this);
        }

        static UUID GetUuidFromUrl(String uri) throws BadRequest {
            Matcher matcher = pattern.matcher(uri);

            if (matcher.matches()) {
                return UUID.fromString(matcher.group(1));
            } else {
                throw new BadRequest("Task UUID not found in URI");
            }
        }

        @MethodEndPoint(method = HttpMethode.GET)
        static HttpResponse taskHandleGet(Request t) throws BadRequest, Unauthorized, JsonSyntaxException {

            String token = t.getHeaderAuth();

            UUID uuid = GetUuidFromUrl(t.RequestURI);

            String username = database.authenticate(token);
            if (!database.isTaskExists(uuid))
                return new HttpResponse().toJson(HttpCode.NOT_FOUND_404, "Task with given uuid does not exists");

            if (!database.isTaskBelongsToUser(username, uuid)) {
                return new HttpResponse().toJson(HttpCode.FORBIDDEN_403, "Task belongs to other user");
            }

            String taskStr = new Gson().toJson(database.getTask(uuid));

            return new HttpResponse(HttpCode.OK_200, taskStr);

        }

        @MethodEndPoint(method = HttpMethode.PUT)
        static HttpResponse taskHandlePut(Request t) throws BadRequest, Unauthorized, JsonSyntaxException, Forbidden, NotFound {

            String token = t.getHeaderAuth();

            UUID uuid = GetUuidFromUrl(t.RequestURI);

            TaskEntity updateTask = new Gson().fromJson(t.getRequestBody(), TaskEntity.class);

            LOGGER.info("Received task to update: " + new Gson().toJson(updateTask));

            TaskEntity.Validate(updateTask);

            String username = database.authenticate(token);
            LOGGER.info("USER: " + username);

            updateTask.setId(uuid);

            String taskStr = new Gson().toJson(database.updateUserTask(username, updateTask));

            return new HttpResponse(HttpCode.OK_200, taskStr);

        }

        @MethodEndPoint(method = HttpMethode.DELETE)
        static HttpResponse taskHandleDelete(Request t) throws BadRequest, Unauthorized, JsonSyntaxException, Forbidden, NotFound {

            String token = t.getHeaderAuth();

            UUID uuid = GetUuidFromUrl(t.RequestURI);

            String username = database.authenticate(token);

            database.removeUserTask(username, uuid);

            return new HttpResponse().toJson(HttpCode.OK_200, "Task deleted");


        }
    }
}

