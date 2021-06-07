package efs.task.todoapp.repository;

import com.google.gson.annotations.Expose;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TaskEntity that = (TaskEntity) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "TaskEntity{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", due='" + due + '\'' +
                '}';
    }

    public void assignUUID() {
        id = UUID.randomUUID();
    }
}
