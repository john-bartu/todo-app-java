package efs.task.todoapp.service.exceptions;

public class Unauthorized extends Exception {
    public Unauthorized(String errorMessage) {
        super(errorMessage);
    }
}