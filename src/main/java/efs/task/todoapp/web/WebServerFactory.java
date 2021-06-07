package efs.task.todoapp.web;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import efs.task.todoapp.repository.TaskEntity;
import efs.task.todoapp.repository.UserEntity;
import efs.task.todoapp.service.BadRequest;
import efs.task.todoapp.service.ToDoService;
import efs.task.todoapp.service.Unauthorized;

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

    @MethodEndPoint(method = HttpMethode.DELETE)
    static HttpResponse taskHandleDelete(Request t) {

        try {


            Pattern pattern = Pattern.compile("^/todo/task/([A-Za-z0-9]{8}-[A-Za-z0-9]{4}-[A-Za-z0-9]{4}-[A-Za-z0-9]{4}-[A-Za-z0-9]{12})$");
            Matcher matcher = pattern.matcher(t.getRequestURI().toString());
            LOGGER.info("Got task from: " + t.getRequestURI().toString());

            if (matcher.matches()) {
                LOGGER.info("Got task UUID: " + matcher.group(1));
                UUID uuid = UUID.fromString(matcher.group(1));

                String token = t.getHeaderAuth();


                if (!database.TaskExists(uuid))
                    return new HttpResponse().toJson(HttpCode.NotFound_404, "Task with given uuid does not exists");


                String username = database.Authenticate(token);

                if (!database.TaskBelongsToUser(username, uuid))
                    return new HttpResponse().toJson(HttpCode.Forbidden_403, "Task belongs to other user");


                database.removeTask(uuid);

                return new HttpResponse().toJson(HttpCode.OK_200, "Task deleted");

            }

            return new HttpResponse().toJson(HttpCode.BadRequest_400, "No task uuid provided");

        } catch (BadRequest badRequest) {
            return new HttpResponse().toJson(HttpCode.BadRequest_400, badRequest.getMessage());
        } catch (Unauthorized unauthorized) {
            return new HttpResponse().toJson(HttpCode.Unauthorized_401, unauthorized.getMessage());
        }
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
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }

            LOGGER.info("SERVER:\n[" + httpResponse.httpCode.rCode + "]: " + httpResponse.httpResponse);

            httpExchange.sendResponseHeaders(httpResponse.httpCode.rCode, httpResponse.getSize());
            httpExchange.getResponseHeaders().add("Content-type", "application/json");
            OutputStream os = httpExchange.getResponseBody();
            os.write(httpResponse.httpResponse.getBytes());
            os.close();

        }

        HttpResponse defaultHandle(Request t) {
            return new HttpResponse().toJson(HttpCode.NotFound_404, "For URI: " + t.getRequestURI());
        }


    }

    @URIEndPoint(path = "/todo/user")
    static class TodoUserEndpoint extends EndpointDefault {
        TodoUserEndpoint() {
            InitMethodEndpoints(this);
        }

        @MethodEndPoint(method = HttpMethode.POST)
        static HttpResponse userHandlePost(Request t) {

            try {

                UserEntity newUser = new Gson().fromJson(t.getRequestBody(), UserEntity.class);
                LOGGER.info("Received user: " + new Gson().toJson(newUser));

                if (newUser != null && newUser.getUsername() != null && newUser.getPassword() != null)
                    if (!newUser.getUsername().equals("") && !newUser.getPassword().equals("")) {
                        if (database.AddUser(newUser)) {
                            return new HttpResponse().toJson(HttpCode.Created_201, "User added");
                        } else {
                            return new HttpResponse().toJson(HttpCode.Conflict_409, "User with given login exists");
                        }
                    }

            } catch (JsonSyntaxException e) {
                LOGGER.warning(e.getMessage());
            }

            return new HttpResponse().toJson(HttpCode.BadRequest_400, "No required fields for user");
        }

    }

    @URIEndPoint(path = "/todo/task")
    static class TodoTasksEndpoint extends EndpointDefault {
        TodoTasksEndpoint() {
            InitMethodEndpoints(this);
        }

        @MethodEndPoint(method = HttpMethode.GET)
        static HttpResponse taskHandleGet(Request t) {
            try {
                String token = t.getHeaderAuth();


                String username = database.Authenticate(token);
                LOGGER.info("USER: " + username);

                List<TaskEntity> taskEntities = database.GetTasks(username);

                return new HttpResponse(HttpCode.OK_200, new Gson().toJson(taskEntities));
            } catch (BadRequest badRequest) {
                return new HttpResponse().toJson(HttpCode.BadRequest_400, badRequest.getMessage());
            } catch (Unauthorized unauthorized) {
                return new HttpResponse().toJson(HttpCode.Unauthorized_401, unauthorized.getMessage());
            }

        }

        @MethodEndPoint(method = HttpMethode.POST)
        static HttpResponse taskHandlePost(Request t) {
            try {

                TaskEntity newTask = new Gson().fromJson(t.getRequestBody(), TaskEntity.class);

                LOGGER.info("Received task: " + new Gson().toJson(newTask));
                String token = t.getHeaderAuth();
                if (newTask != null) {

                    newTask.Validate();


                    if (newTask.getDescription() != null && !newTask.getDescription().equals("")) {

                        String username = database.Authenticate(token);
                        if (database.AddTask(username, newTask)) {

                            return new HttpResponse(HttpCode.Created_201, "{\"id\":\"" + newTask.getId() + "\"}")
                                    ;
                        }
                    }
                }
            } catch (JsonSyntaxException e) {
                return new HttpResponse().toJson(HttpCode.BadRequest_400, "JSON Parse error" + e.getMessage());
            } catch (BadRequest badRequest) {
                return new HttpResponse().toJson(HttpCode.BadRequest_400, badRequest.getMessage());
            } catch (Unauthorized unauthorized) {
                return new HttpResponse().toJson(HttpCode.Unauthorized_401, unauthorized.getMessage());
            }

            return new HttpResponse().toJson(HttpCode.BadRequest_400, "No required fields for task provided");
        }
    }

    @URIEndPoint(path = "/todo/task/")
    static class TodoTaskEndpoint extends EndpointDefault {
        static Pattern pattern = Pattern.compile("^/todo/task/([A-Za-z0-9]{8}-[A-Za-z0-9]{4}-[A-Za-z0-9]{4}-[A-Za-z0-9]{4}-[A-Za-z0-9]{12})$");

        TodoTaskEndpoint() {
            InitMethodEndpoints(this);
        }


        @MethodEndPoint(method = HttpMethode.GET)
        static HttpResponse taskHandleGet(Request t) {
            try {

                Matcher matcher = pattern.matcher(t.getRequestURI());
                LOGGER.info("Got task from: " + t.getRequestURI());
                String token = t.getHeaderAuth();

                if (matcher.matches()) {
                    LOGGER.info("Got task UUID: " + matcher.group(1));
                    UUID uuid = UUID.fromString(matcher.group(1));


                    String username = database.Authenticate(token);

                    if (!database.TaskBelongsToUser(username, uuid))
                        return new HttpResponse().toJson(HttpCode.Forbidden_403, "Task belongs to other user");


                    if (!database.TaskExists(uuid))
                        return new HttpResponse().toJson(HttpCode.NotFound_404, "Task with given uuid does not exists");


                    String taskStr = new Gson().toJson(database.GetTask(uuid));

                    return new HttpResponse(HttpCode.OK_200, taskStr);

                }

                throw new BadRequest("No task uuid provided");

            } catch (BadRequest badRequest) {
                return new HttpResponse().toJson(HttpCode.BadRequest_400, badRequest.getMessage());
            } catch (Unauthorized unauthorized) {
                return new HttpResponse().toJson(HttpCode.Unauthorized_401, unauthorized.getMessage());
            }
        }

        @MethodEndPoint(method = HttpMethode.PUT)
        static HttpResponse taskHandlePut(Request t) {

            try {

                Matcher matcher = pattern.matcher(t.getRequestURI());
                LOGGER.info("Got task from: " + t.getRequestURI());
                String token = t.getHeaderAuth();
                if (matcher.matches()) {

                    LOGGER.info("Got task UUID: " + matcher.group(1));
                    UUID uuid = UUID.fromString(matcher.group(1));


                    TaskEntity updateTask = new Gson().fromJson(t.getRequestBody(), TaskEntity.class);

                    LOGGER.info("Received task to update: " + new Gson().toJson(updateTask));

                    if (updateTask != null && !updateTask.getDescription().equals("")) {

                        updateTask.Validate();

                        String username = database.Authenticate(token);
                        LOGGER.info("USER: " + username);

                        if (!database.TaskBelongsToUser(username, uuid))
                            return new HttpResponse().toJson(HttpCode.Forbidden_403, "Task belongs to other user");

                        if (!database.TaskExists(uuid))
                            return new HttpResponse().toJson(HttpCode.NotFound_404, "Task with given uuid does not exists");


                        updateTask.setId(uuid);

                        String taskStr = new Gson().toJson(database.UpdateTask(updateTask));

                        return new HttpResponse(HttpCode.OK_200, taskStr);

                    }
                }

                throw new BadRequest("No task uuid or body provided");

            } catch (BadRequest | JsonSyntaxException badRequest) {
                return new HttpResponse().toJson(HttpCode.BadRequest_400, badRequest.getMessage());
            } catch (Unauthorized unauthorized) {
                return new HttpResponse().toJson(HttpCode.Unauthorized_401, unauthorized.getMessage());
            }
        }

        @MethodEndPoint(method = HttpMethode.DELETE)
        static HttpResponse taskHandleDelete(Request t) {

            try {

                Matcher matcher = pattern.matcher(t.getRequestURI());
                LOGGER.info("Got task from: " + t.getRequestURI());
                String token = t.getHeaderAuth();

                if (matcher.matches()) {
                    LOGGER.info("Got task UUID: " + matcher.group(1));
                    UUID uuid = UUID.fromString(matcher.group(1));


                    String username = database.Authenticate(token);

                    if (!database.TaskBelongsToUser(username, uuid))
                        return new HttpResponse().toJson(HttpCode.Forbidden_403, "Task belongs to other user");

                    if (!database.TaskExists(uuid))
                        return new HttpResponse().toJson(HttpCode.NotFound_404, "Task with given uuid does not exists");


                    database.removeTask(uuid);

                    return new HttpResponse().toJson(HttpCode.OK_200, "Task deleted");

                }

                throw new BadRequest("No task uuid provided");

            } catch (BadRequest badRequest) {
                return new HttpResponse().toJson(HttpCode.BadRequest_400, badRequest.getMessage());
            } catch (Unauthorized unauthorized) {
                return new HttpResponse().toJson(HttpCode.Unauthorized_401, unauthorized.getMessage());
            }
        }
    }

}

