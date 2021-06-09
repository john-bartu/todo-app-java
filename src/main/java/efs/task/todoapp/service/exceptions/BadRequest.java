package efs.task.todoapp.service.exceptions;

public class BadRequest extends Exception {
    public BadRequest(String errorMessage) {
        super(errorMessage);
    }
}
