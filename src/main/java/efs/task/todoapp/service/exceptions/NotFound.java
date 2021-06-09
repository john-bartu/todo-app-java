package efs.task.todoapp.service.exceptions;

public class NotFound extends Exception {
    public NotFound(String errorMessage) {
        super(errorMessage);
    }
}
