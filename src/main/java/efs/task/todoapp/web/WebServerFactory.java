package efs.task.todoapp.web;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import efs.task.todoapp.ToDoApplication;
import efs.task.todoapp.repository.TaskEntity;
import efs.task.todoapp.repository.UserEntity;
import efs.task.todoapp.service.ToDoService;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.LogManager;
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


                System.out.println("\t" + url);


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
                    System.out.println("\t\t" + method);
                }
            }
        }

        @Override
        public void handle(HttpExchange t) throws IOException {
            LOGGER.info("[" + t.getRequestMethod() + "]\n" +
                            "URI: " + t.getRequestURI() + "\n" +
                            "HEADERS: " + t.getRequestHeaders().keySet() + "\n"
//                    + "BODY: " + new String(t.getRequestBody().readAllBytes())
            );

            HttpMethode requestMethode = HttpMethode.valueOf(t.getRequestMethod());
            HttpResponse httpResponse = defaultHandle(t);

            try {
                if (methodHashMap.containsKey(requestMethode)) {
                    LOGGER.info("Method found " + methodHashMap.get(requestMethode).getName());
                    httpResponse = (HttpResponse) methodHashMap.get(requestMethode).invoke(null, t);
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }

            LOGGER.info("[" + httpResponse.httpCode.rCode + "]: " + httpResponse.httpResponse);
            t.sendResponseHeaders(httpResponse.httpCode.rCode, httpResponse.getSize());
            OutputStream os = t.getResponseBody();
            os.write(httpResponse.httpResponse.getBytes());
            os.close();

        }

        HttpResponse defaultHandle(HttpExchange t) {
            return new HttpResponse(HttpCode.NotFound, "For URI: " + t.getRequestURI());
        }

    }

    @URIEndPoint(path = "/todo/user")
    static class TodoUserEndpoint extends EndpointDefault {
        TodoUserEndpoint() {
            InitMethodEndpoints(this);
        }

        @MethodEndPoint(method = HttpMethode.POST)
        static HttpResponse userHandlePost(HttpExchange t) {

            try {
                UserEntity newUser = new Gson().fromJson(new String(t.getRequestBody().readAllBytes()), UserEntity.class);

                LOGGER.info("Received user: " + new Gson().toJson(newUser));
                if (newUser != null)
                    if (!newUser.getUsername().equals("") && !newUser.getPassword().equals("")) {
                        if (database.AddUser(newUser)) {
                            return new HttpResponse(HttpCode.Created, "Uzytkownik dodany.");
                        } else {
                            return new HttpResponse(HttpCode.Conflict, "Uzytkownik o podanej nazwie juz istnieje.");
                        }
                    }

            } catch (JsonSyntaxException | IOException e) {
                LOGGER.warning(e.getMessage());
            }
            return new HttpResponse(HttpCode.BadRequest, "Brak wymaganej tresci.");

        }
    }

    @URIEndPoint(path = "/todo/task")
    static class TodoTasksEndpoint extends EndpointDefault {
        TodoTasksEndpoint() {
            InitMethodEndpoints(this);
        }

        @MethodEndPoint(method = HttpMethode.GET)
        static HttpResponse taskHandleGet(HttpExchange t) {


            if (!t.getResponseHeaders().containsKey("auth"))
                return new HttpResponse(HttpCode.BadRequest, "No auth header");

            String username = database.Authenticate(t.getRequestHeaders().getFirst("auth"));
            if (username == null)
                return new HttpResponse(HttpCode.Unauthorized, "Authentication failed");


            List<TaskEntity> taskEntities = database.GetTasks(username);
            return new HttpResponse(HttpCode.BadRequest, new Gson().toJson(taskEntities));
        }

        @MethodEndPoint(method = HttpMethode.POST)
        static HttpResponse taskHandlePost(HttpExchange t) {
            if (!t.getResponseHeaders().containsKey("auth"))
                return new HttpResponse(HttpCode.BadRequest, "No auth header");

            String username = database.Authenticate(t.getRequestHeaders().getFirst("auth"));
            if (username == null)
                return new HttpResponse(HttpCode.Unauthorized, "Authentication failed");


            try {
                TaskEntity newTask = new Gson().fromJson(new String(t.getRequestBody().readAllBytes()), TaskEntity.class);

                LOGGER.info("Received task: " + new Gson().toJson(newTask));

                if (newTask != null)
                    if (!newTask.getDescription().equals("")) {
                        if (database.AddTask(username, newTask)) {
                            return new HttpResponse(HttpCode.Created, "Task added.");
                        }
                    }
            } catch (JsonSyntaxException | IOException e) {
                LOGGER.warning(e.getMessage());
            }

            return new HttpResponse(HttpCode.BadRequest, "No required fields for task provided");
        }
    }


    @URIEndPoint(path = "/todo/task/")
    static class TodoTaskEndpoint extends EndpointDefault {
        TodoTaskEndpoint() {
            InitMethodEndpoints(this);
        }


        @MethodEndPoint(method = HttpMethode.GET)
        static HttpResponse taskHandleGet(HttpExchange t) {

            if (!t.getResponseHeaders().containsKey("auth"))
                return new HttpResponse(HttpCode.BadRequest, "No auth header");


            Pattern pattern = Pattern.compile("^\\\\task\\\\([A-Za-z0-9]{8}-[A-Za-z0-9]{4}-[A-Za-z0-9]{4}-[A-Za-z0-9]{4}-[A-Za-z0-9]{12})$");
            Matcher matcher = pattern.matcher(t.getRequestURI().toString());

            if (matcher.matches()) {
                UUID uuid = UUID.fromString(matcher.group(0));

                if (!database.TaskExists(uuid))
                    return new HttpResponse(HttpCode.NotFound, "Task with given uuid does not exists");

                String username = database.Authenticate(t.getRequestHeaders().getFirst("auth"));
                if (username == null)
                    return new HttpResponse(HttpCode.Unauthorized, "Authentication failed");

                if(!database.TaskBelongsToUser(username,uuid))
                    return new HttpResponse(HttpCode.Forbidden, "Task belongs to other user");





                String taskStr = new Gson().toJson(database.GetTask(uuid));

                return new HttpResponse(HttpCode.OK, taskStr);

            }

            return new HttpResponse(HttpCode.NotFound, "Uri not found");
        }

        @MethodEndPoint(method = HttpMethode.PUT)
        static HttpResponse taskHandlePut(HttpExchange t) {
            if (!t.getResponseHeaders().containsKey("auth"))
                return new HttpResponse(HttpCode.BadRequest, "No auth header");


            Pattern pattern = Pattern.compile("^\\\\task\\\\([A-Za-z0-9]{8}-[A-Za-z0-9]{4}-[A-Za-z0-9]{4}-[A-Za-z0-9]{4}-[A-Za-z0-9]{12})$");
            Matcher matcher = pattern.matcher(t.getRequestURI().toString());

            if (matcher.matches()) {
                UUID uuid = UUID.fromString(matcher.group(0));

                if (!database.TaskExists(uuid))
                    return new HttpResponse(HttpCode.NotFound, "Task with given uuid does not exists");

                String username = database.Authenticate(t.getRequestHeaders().getFirst("auth"));
                if (username == null)
                    return new HttpResponse(HttpCode.Unauthorized, "Authentication failed");

                if(!database.TaskBelongsToUser(username,uuid))
                    return new HttpResponse(HttpCode.Forbidden, "Task belongs to other user");


                try {
                    TaskEntity newTask = new Gson().fromJson(new String(t.getRequestBody().readAllBytes()), TaskEntity.class);

                    LOGGER.info("Received task to update: " + new Gson().toJson(newTask));

                    if (newTask != null)
                        if (!newTask.getDescription().equals("")) {
                            String taskStr = new Gson().toJson(database.UpdateTask(newTask));
                            return new HttpResponse(HttpCode.OK, taskStr);
                        }
                } catch (JsonSyntaxException | IOException e) {
                    LOGGER.warning(e.getMessage());
                }





            }

            return new HttpResponse(HttpCode.NotFound, "Uri not found");
        }

        @MethodEndPoint(method = HttpMethode.DELETE)
        static HttpResponse taskHandleDelete(HttpExchange t) {
            if (!t.getResponseHeaders().containsKey("auth"))
                return new HttpResponse(HttpCode.BadRequest, "No auth header");


            Pattern pattern = Pattern.compile("^\\\\task\\\\([A-Za-z0-9]{8}-[A-Za-z0-9]{4}-[A-Za-z0-9]{4}-[A-Za-z0-9]{4}-[A-Za-z0-9]{12})$");
            Matcher matcher = pattern.matcher(t.getRequestURI().toString());

            if (matcher.matches()) {
                UUID uuid = UUID.fromString(matcher.group(0));

                if (!database.TaskExists(uuid))
                    return new HttpResponse(HttpCode.NotFound, "Task with given uuid does not exists");

                String username = database.Authenticate(t.getRequestHeaders().getFirst("auth"));
                if (username == null)
                    return new HttpResponse(HttpCode.Unauthorized, "Authentication failed");

                if(!database.TaskBelongsToUser(username,uuid))
                    return new HttpResponse(HttpCode.Forbidden, "Task belongs to other user");



                database.removeTask(uuid);
                return new HttpResponse(HttpCode.OK, "Task deleted");

            }

            return new HttpResponse(HttpCode.NotFound, "Uri not found");
        }
    }
}
