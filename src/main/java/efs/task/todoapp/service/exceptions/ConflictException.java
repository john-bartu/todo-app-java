package efs.task.todoapp.service.exceptions;

public class ConflictException extends Exception {
    public ConflictException(String errorMessage) {
        super(errorMessage);
    }
}
