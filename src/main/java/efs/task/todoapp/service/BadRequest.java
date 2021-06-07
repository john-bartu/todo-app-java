package efs.task.todoapp.service;

public class BadRequest extends Exception {
    public BadRequest(String errorMessage) {
        super(errorMessage);
    }
}
