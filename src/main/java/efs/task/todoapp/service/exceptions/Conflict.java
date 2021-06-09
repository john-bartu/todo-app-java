package efs.task.todoapp.service.exceptions;

public class Conflict extends Exception {
    public Conflict(String errorMessage) {
        super(errorMessage);
    }
}
