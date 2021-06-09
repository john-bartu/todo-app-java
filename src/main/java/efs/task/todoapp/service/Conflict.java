package efs.task.todoapp.service;

public class Conflict extends Exception {
    public Conflict(String errorMessage) {
        super(errorMessage);
    }
}
