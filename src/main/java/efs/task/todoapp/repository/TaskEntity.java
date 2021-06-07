package efs.task.todoapp.repository;

import efs.task.todoapp.service.BadRequest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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

    public void Validate() throws BadRequest {
        if (due != null) {
            SimpleDateFormat sdfrmt = new SimpleDateFormat("yyyy-MM-dd");

            try {
                Date javaDate = sdfrmt.parse(due);
                System.out.println(due + " is valid date format");
            } catch (ParseException e) {
                System.out.println();
                throw new BadRequest(due + " is Invalid Date format");
            }

        }

    }
}
