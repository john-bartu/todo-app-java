package efs.task.todoapp.repository;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public class TaskRepository implements Repository<UUID, TaskEntity> {
    @Override
    public UUID save(TaskEntity taskEntity) {
        return null;
    }

    @Override
    public TaskEntity query(UUID uuid) {
        return null;
    }

    @Override
    public List<TaskEntity> query(Predicate<TaskEntity> condition) {
        return null;
    }

    @Override
    public TaskEntity update(UUID uuid, TaskEntity taskEntity) {
        return null;
    }

    @Override
    public boolean delete(UUID uuid) {
        return false;
    }
}
