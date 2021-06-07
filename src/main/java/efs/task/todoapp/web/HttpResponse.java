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
        httpCode = HttpCode.NotFound_404;
    }

    public HttpResponse toJson(HttpCode code, String data) {
//        httpResponse = "{\"data\":\"" + data + "\"}";
        httpResponse = "";
        httpCode = code;
        return this;
    }

    public int getSize() {
        return httpResponse.getBytes().length;
    }
}
