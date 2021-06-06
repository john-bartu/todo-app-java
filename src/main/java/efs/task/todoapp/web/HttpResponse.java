package efs.task.todoapp.web;

import java.nio.charset.StandardCharsets;

class HttpResponse {
    String httpResponse;
    HttpCode httpCode;

    HttpResponse(HttpCode code, String data) {
        httpCode = code;
        httpResponse = data;
    }

    public HttpResponse() {
        httpResponse = "Error 404 - Default Response";
        httpCode = HttpCode.NotFound;
    }

    public int getSize() {
        return httpResponse.getBytes(StandardCharsets.UTF_8).length;
    }
}
