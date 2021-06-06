package efs.task.todoapp.web;

public enum HttpMethode {
    PUT,
    GET,
    POST,
    DELETE;

    HttpMethode fromString(String s) {
        return HttpMethode.valueOf(s);
    }
}
