package efs.task.todoapp.service;

public class NotFound extends Exception {
    public NotFound(String errorMessage) {
        super(errorMessage);
    }
}
