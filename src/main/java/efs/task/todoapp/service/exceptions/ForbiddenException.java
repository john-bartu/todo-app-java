package efs.task.todoapp.service.exceptions;

public class ForbiddenException extends Exception {
    public ForbiddenException(String errorMessage) {
        super(errorMessage);
    }
}
