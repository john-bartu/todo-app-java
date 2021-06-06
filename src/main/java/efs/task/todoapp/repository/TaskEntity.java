package efs.task.todoapp.repository;

import java.util.UUID;

public class TaskEntity {
    UUID id;
    String description;
    String due;

    public TaskEntity(String description, String due) {
        id = UUID.randomUUID();
        this.description = description;
        this.due = due;
    }

    public UUID getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDue() {
        return due;
    }

    public void setDue(String due) {
        this.due = due;
    }
}
