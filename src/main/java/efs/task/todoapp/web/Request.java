package efs.task.todoapp.web;

import java.util.ArrayList;
import java.util.HashMap;
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


    public String getFirstRequestHeader(String header) {
        if (RequestHeaders.containsKey(header)) {
            return RequestHeaders.getOrDefault(header, new ArrayList<>()).get(0);
        } else {
            return null;
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