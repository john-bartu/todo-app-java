package efs.task.todoapp.web;

import efs.task.todoapp.service.exceptions.BadRequestException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class Request {
    String requestMethod;
    String requestURI;
    Map<String, List<String>> requestHeaders;
    String requestBody;

    public Request(String requestURI, String requestMethod, Map<String, List<String>> requestHeaders, String requestBody) {
        this.requestMethod = requestMethod;
        this.requestURI = requestURI;
        this.requestHeaders = requestHeaders;
        this.requestBody = requestBody;
    }

    public String getRequestBody() {
        return requestBody;
    }


    private void validateToken(String token) throws BadRequestException {
        if (token == null || token.equals(""))
            throw new BadRequestException("Auth token is null or empty");

        if (token.split("[\\s:]+").length != 2)
            throw new BadRequestException("Auth token has no properly format XX:XX");

        String un1 = token.split("[\\s:]+")[0];
        if (un1 == null || un1.equals(""))
            throw new BadRequestException("Auth token username is empty or null");

        String un2 = token.split("[\\s:]+")[1];
        if (un2 == null || un2.equals(""))
            throw new BadRequestException("Auth token password is empty or null");

    }

    public String getHeaderAuth() throws BadRequestException {
        if (requestHeaders.containsKey("Auth")) {
            String token = requestHeaders.getOrDefault("Auth", new ArrayList<>()).get(0);

            validateToken(token);
            return token;


        } else {
            throw new BadRequestException("Header doesnt contain Auth token");
        }
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public String getRequestURI() {
        return requestURI;
    }

    public Map<String, List<String>> getRequestHeaders() {
        return requestHeaders;
    }
}