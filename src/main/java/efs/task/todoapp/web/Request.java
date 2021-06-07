package efs.task.todoapp.web;

import efs.task.todoapp.service.BadRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class Request {
    String RequestMethod;
    String RequestURI;
    Map<String, List<String>> RequestHeaders;
    String RequestBody;

    public Request(String requestURI, String requestMethod, Map<String, List<String>> requestHeaders, String requestBody) {
        RequestMethod = requestMethod;
        RequestURI = requestURI;
        RequestHeaders = requestHeaders;
        RequestBody = requestBody;
    }

    public String getRequestBody() {
        return RequestBody;
    }


    private boolean ValidateToken(String token) throws BadRequest {
        if (token == null || token.equals(""))
            throw new BadRequest("Auth token is null or empty");

        if (token.split("[\\s:]+").length != 2)
            throw new BadRequest("Auth token has no properly format XX:XX");

        String un1 = token.split("[\\s:]+")[0];
        if (un1 == null || un1.equals(""))
            throw new BadRequest("Auth token username is empty or null");

        String un2 = token.split("[\\s:]+")[1];
        if (un2 == null || un2.equals(""))
            throw new BadRequest("Auth token password is empty or null");

        return true;
    }


    public String getHeaderAuth() throws BadRequest {
        if (RequestHeaders.containsKey("Auth")) {
            String token = RequestHeaders.getOrDefault("Auth", new ArrayList<>()).get(0);

            if (ValidateToken(token)) {
                return token;
            } else {
                throw new BadRequest("Auth: Cannot validate: " + token);
            }

        } else {
            throw new BadRequest("Header doesnt contain Auth token");
        }
    }

    public String getRequestMethod() {
        return RequestMethod;
    }

    public String getRequestURI() {
        return RequestURI;
    }


    public Map<String, List<String>> getRequestHeaders() {
        return RequestHeaders;
    }
}