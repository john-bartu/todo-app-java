package efs.task.todoapp.service.exceptions;

public class Forbidden extends Exception {
    public Forbidden(String errorMessage) {
        super(errorMessage);
    }
}
