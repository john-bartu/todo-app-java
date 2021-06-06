package efs.task.todoapp.web;

public enum HttpCode {
    OK(200),
    BadRequest(400),
    Unauthorized(401),
    Forbidden(403),
    NotFound(404),
    Conflict(409);
    int rCode;

    HttpCode(int code) {
        rCode = code;
    }

    public int getrCode() {
        return rCode;
    }
}
