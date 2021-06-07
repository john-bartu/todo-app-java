package efs.task.todoapp.service;

public class Unauthorized extends Exception {
    public Unauthorized(String errorMessage) {
        super(errorMessage);
    }
}