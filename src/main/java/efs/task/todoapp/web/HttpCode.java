package efs.task.todoapp.web;

public enum HttpCode {
    OK_200(200),
    CREATED_201(201),
    BAD_REQUEST_400(400),
    UNAUTHORIZED_401(401),
    FORBIDDEN_403(403),
    NOT_FOUND_404(404),
    CONFLICT_409(409),
    INTERNAL_SERVER_ERROR_500(500);

    int responseCode;

    HttpCode(int code) {
        responseCode = code;
    }

    public int getResponseCode() {
        return responseCode;
    }
}
