package efs.task.todoapp.service.exceptions;

public class NotFoundException extends Exception {
    public NotFoundException(String errorMessage) {
        super(errorMessage);
    }
}
