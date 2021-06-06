package efs.task.todoapp.web;

public enum HttpCode {
    OK_200(200),
    BadRequest_400(400),
    Unauthorized_401(401),
    Forbidden_403(403),
    NotFound_404(404),
    Conflict_409(409),
    Created_201(201);
    int rCode;

    HttpCode(int code) {
        rCode = code;
    }

    public int getrCode() {
        return rCode;
    }
}
