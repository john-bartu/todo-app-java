package efs.task.todoapp.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import efs.task.todoapp.ToDoApplication;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class WebServerFactory {
    private static final Logger LOGGER = Logger.getLogger(WebServerFactory.class.getName());


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
            LOGGER.info("[" + t.getRequestMethod() + "] " + t.getRequestURI());

            HttpMethode requestMethode = HttpMethode.valueOf(t.getRequestMethod());
            HttpResponse httpResponse = defaultHandle();

            try {
                if (methodHashMap.containsKey(requestMethode)) {
                    LOGGER.info("Method found " + methodHashMap.get(requestMethode).getName());
                    httpResponse = (HttpResponse) methodHashMap.get(requestMethode).invoke(null);
                } else {
                    httpResponse = defaultHandle();
                }

            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
            LOGGER.info("[" + httpResponse.httpCode.rCode + "]: " + httpResponse.httpResponse);
            t.sendResponseHeaders(httpResponse.httpCode.rCode, httpResponse.httpResponse.length());
            OutputStream os = t.getResponseBody();
            os.write(httpResponse.httpResponse.getBytes());
            os.close();

        }

        HttpResponse defaultHandle() {
            return new HttpResponse();
        }

    }

    @URIEndPoint(path = "/todo/user")
    static class TodoUserEndpoint extends EndpointDefault {
        TodoUserEndpoint() {
            InitMethodEndpoints(this);
        }

        @MethodEndPoint(method = HttpMethode.POST)
        static HttpResponse userHandlePost() {
            return new HttpResponse(HttpCode.OK, "USER POST");
        }
    }

    @URIEndPoint(path = "/todo/task")
    static class TodoTasksEndpoint extends EndpointDefault {
        TodoTasksEndpoint() {
            InitMethodEndpoints(this);
        }

        @MethodEndPoint(method = HttpMethode.GET)
        static HttpResponse taskHandleGet() {
            return new HttpResponse(HttpCode.OK, "TASK GET");
        }

        @MethodEndPoint(method = HttpMethode.POST)
        static HttpResponse taskHandlePost() {
            return new HttpResponse(HttpCode.OK, "TASK POST");
        }
    }


    @URIEndPoint(path = "/todo/task/")
    static class TodoTaskEndpoint extends EndpointDefault {
        TodoTaskEndpoint() {
            InitMethodEndpoints(this);
        }


        @MethodEndPoint(method = HttpMethode.GET)
        static HttpResponse taskHandleGet() {
            return new HttpResponse(HttpCode.OK, "TASK/ GET");
        }

        @MethodEndPoint(method = HttpMethode.PUT)
        static HttpResponse taskHandlePut() {
            return new HttpResponse(HttpCode.OK, "TASK/ PUT");
        }

        @MethodEndPoint(method = HttpMethode.DELETE)
        static HttpResponse taskHandleDelete() {
            return new HttpResponse(HttpCode.OK, "TASK/ DELETE");
        }
    }
}
