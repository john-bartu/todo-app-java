package efs.task.todoapp.repository;

import efs.task.todoapp.service.exceptions.BadRequest;

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

    public static void Validate(TaskEntity taskEntity) throws BadRequest {

        if (taskEntity == null) {
            throw new BadRequest("Validation: Task in sull");
        }


        if (taskEntity.getDescription() == null || taskEntity.getDescription().equals("")) {
            throw new BadRequest("Validation: Description not provided");
        }

        if (taskEntity.due != null) {
            SimpleDateFormat sdfrmt = new SimpleDateFormat("yyyy-MM-dd");
            sdfrmt.setLenient(false);
            System.out.println(taskEntity.due + " is valid Date Format");
            try {
                sdfrmt.parse(taskEntity.due);
            } catch (ParseException e) {
                throw new BadRequest(taskEntity.due + " is Invalid Date format");
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
