package efs.task.todoapp.service;

public class Forbidden extends Exception {
    public Forbidden(String errorMessage) {
        super(errorMessage);
    }
}
