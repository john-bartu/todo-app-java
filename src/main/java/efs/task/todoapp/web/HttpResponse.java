package efs.task.todoapp.web;

class HttpResponse {
    String httpResponse;
    HttpCode httpCode;

    HttpResponse(HttpCode code, String data) {
        httpCode = code;
        httpResponse = data;
    }

    public HttpResponse() {
        httpResponse = "Error 404 - Default Response";
        httpCode =  HttpCode.NotFound;
    }
}
