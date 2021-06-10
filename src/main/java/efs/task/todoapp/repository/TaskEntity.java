package efs.task.todoapp.repository;

import efs.task.todoapp.service.exceptions.BadRequestException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    public static void validate(TaskEntity taskEntity) throws BadRequestException {

        if (taskEntity == null) {
            throw new BadRequestException("Validation: Task is null");
        }


        if (taskEntity.getDescription() == null || taskEntity.getDescription().equals("")) {
            throw new BadRequestException("Validation: Description not provided");
        }

        if (taskEntity.due != null) {
            SimpleDateFormat sdfrmt = new SimpleDateFormat("yyyy-MM-dd");
            sdfrmt.setLenient(false);
            try {
                sdfrmt.parse(taskEntity.due);
            } catch (ParseException e) {
                throw new BadRequestException(taskEntity.due + " is Invalid Date format");
            }

        }


    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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
