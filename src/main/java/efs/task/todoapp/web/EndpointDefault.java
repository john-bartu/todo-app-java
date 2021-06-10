package efs.task.todoapp.web;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import efs.task.todoapp.service.exceptions.*;
import efs.task.todoapp.web.annotations.MethodEndPoint;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.logging.Logger;

class EndpointDefault implements HttpHandler {
    private static final Logger LOGGER = Logger.getLogger(WebServerFactory.class.getName());

    final HashMap<HttpMethod, Method> methodHashMap = new HashMap<>() {
        @Override
        public String toString() {

            StringBuilder stringBuilder = new StringBuilder();

            this.forEach((methode, method) -> {
                String response = String.format("\t [%10s] -> %s\n", methode.toString(), method.getName());
                stringBuilder.append(response);
            });

            return stringBuilder.toString();
        }
    };


    void initMethodEndpoints(Object o) {

        Method[] endpointClasses = o.getClass().getDeclaredMethods();
        for (Method endpointMethode : endpointClasses) {
            MethodEndPoint annotation = endpointMethode.getAnnotation(MethodEndPoint.class);

            if (annotation != null) {
                HttpMethod method = annotation.method();
                methodHashMap.put(method, endpointMethode);
            }
        }

        LOGGER.info("- New Endpoint: " + o.getClass().getName() + "\n" + methodHashMap);
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {

        Request request = new Request(
                httpExchange.getRequestURI().toString(),
                httpExchange.getRequestMethod(),
                httpExchange.getRequestHeaders(),
                new String(httpExchange.getRequestBody().readAllBytes())
        );
        LOGGER.info("-> Handling client request:\n"
                + "\t    URI: " + request.getRequestURI() + "\t[" + request.getRequestMethod() + "]\n"
                + "\tHEADERS: " + request.getRequestHeaders().keySet() + "\n"
                + "\t   BODY: " + request.getRequestBody() + "\n"
        );

        HttpMethod requestMethode = HttpMethod.valueOf(request.getRequestMethod());
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
            } catch (BadRequestException badRequestException) {
                httpResponse = new HttpResponse().toJson(HttpCode.BAD_REQUEST_400, badRequestException.getMessage());
            } catch (UnauthorizedException unauthorized) {
                httpResponse = new HttpResponse().toJson(HttpCode.UNAUTHORIZED_401, unauthorized.getMessage());
            } catch (ConflictException conflictException) {
                httpResponse = new HttpResponse().toJson(HttpCode.CONFLICT_409, conflictException.getMessage());
            } catch (NotFoundException notFoundException) {
                httpResponse = new HttpResponse().toJson(HttpCode.NOT_FOUND_404, notFoundException.getMessage());
            } catch (ForbiddenException forbiddenException) {
                httpResponse = new HttpResponse().toJson(HttpCode.FORBIDDEN_403, forbiddenException.getMessage());
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                httpResponse = new HttpResponse().toJson(HttpCode.INTERNAL_SERVER_ERROR_500, throwable.getMessage());
            }
        }

        LOGGER.info("<- Server response:\n"
                + "\t   CODE: [" + httpResponse.httpCode.responseCode + "] \n"
                + "\t   BODY: " + httpResponse.httpResponse + "\n");
        httpExchange.sendResponseHeaders(httpResponse.httpCode.responseCode, httpResponse.getSize());
        httpExchange.getResponseHeaders().add("Content-type", "application/json");
        OutputStream os = httpExchange.getResponseBody();
        os.write(httpResponse.httpResponse.getBytes());
        os.close();

        LOGGER.info("-------------------------------------------\n\n");
    }

    HttpResponse defaultHandle(Request t) {
        return new HttpResponse().toJson(HttpCode.NOT_FOUND_404, "For URI: " + t.getRequestURI());
    }


}
