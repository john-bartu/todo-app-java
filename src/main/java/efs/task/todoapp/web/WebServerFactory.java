package efs.task.todoapp.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.*;

public class WebServerFactory {
    public static HttpServer createServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        Set<String> urls = new HashSet<>();

        Class<?>[] endpointClasses = WebServerFactory.class.getDeclaredClasses();


        System.out.println("REGISTERED ENDPOINTS:");

        for (Class<?> endpointClass : endpointClasses) {
            URIEndPoint annotation = endpointClass.getAnnotation(URIEndPoint.class);
            if (annotation != null) {
                String url = annotation.path();


                System.out.println("\t" + url);
                HttpHandler httpHandler = new TodoTaskEndpoint();


                Constructor<?> constructor;
                try {
                    constructor = endpointClass.getDeclaredConstructor();
                    constructor.setAccessible(true);
                    httpHandler = (HttpHandler) constructor.newInstance();

                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                    e.printStackTrace();
                }

                server.createContext(url, httpHandler);

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
            System.out.println("[" + t.getRequestMethod() + "] " + t.getRequestURI());

            HttpMethode requestMethode = HttpMethode.valueOf(t.getRequestMethod());
            HttpResponse httpResponse = defaultHandle();

            try {
                if (methodHashMap.containsKey(requestMethode)) {
                    System.out.println("Metod found " + methodHashMap.get(requestMethode).getName());
                    httpResponse = (HttpResponse) methodHashMap.get(requestMethode).invoke(null);
                } else {
                    httpResponse = defaultHandle();
                }

            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
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

        @MethodEndPoint(method = HttpMethode.GET)
        static HttpResponse taskHandleGet() {
            System.out.println("userHandleGet");
            return new HttpResponse(HttpCode.OK, "USER GET");
        }

        @MethodEndPoint(method = HttpMethode.POST)
        static HttpResponse userHandlePost() {
            System.out.println("userHandlePost");
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
            System.out.println("taskHandleGet");
            return new HttpResponse(HttpCode.OK, "TASK GET");
        }

        @MethodEndPoint(method = HttpMethode.DELETE)
        static HttpResponse taskHandleDelete() {
            System.out.println("taskHandleDelete");
            return new HttpResponse(HttpCode.OK, "TASK DELETE");
        }

        @MethodEndPoint(method = HttpMethode.POST)
        static HttpResponse taskHandlePost() {
            System.out.println("taskHandlePost");
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
            System.out.println("taskHandleGet");
            return new HttpResponse(HttpCode.OK, "TASK/ GET");
        }

        @MethodEndPoint(method = HttpMethode.PUT)
        static HttpResponse taskHandlePut() {
            System.out.println("taskHandlePut");
            return new HttpResponse(HttpCode.OK, "TASK/ PUT");
        }

        @MethodEndPoint(method = HttpMethode.DELETE)
        static HttpResponse taskHandleDelete() {
            System.out.println("taskHandleDelete");
            return new HttpResponse(HttpCode.OK, "TASK/ DELETE");
        }
    }
}
