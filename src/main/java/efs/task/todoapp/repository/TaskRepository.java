package efs.task.todoapp.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TaskRepository implements Repository<UUID, TaskEntity> {

    Map<UUID, TaskEntity> taskEntityMap = new HashMap<>();

    @Override
    public UUID save(TaskEntity taskEntity) {
        taskEntityMap.put(taskEntity.id, taskEntity);

        if (taskEntityMap.get(taskEntity.id).equals(taskEntity))
            return taskEntity.id;
        else
            return null;
    }

    @Override
    public TaskEntity query(UUID uuid) {
        return taskEntityMap.getOrDefault(uuid, null);
    }

    @Override
    public List<TaskEntity> query(Predicate<TaskEntity> condition) {
        return taskEntityMap.values().stream().filter(condition).collect(Collectors.toList());
    }

    @Override
    public TaskEntity update(UUID uuid, TaskEntity taskEntity) {
        if (taskEntityMap.containsKey(uuid)) {
            taskEntityMap.replace(uuid, taskEntity);
            return taskEntity;
        } else {
            return null;
        }
    }

    @Override
    public boolean delete(UUID uuid) {
        return (taskEntityMap.remove(uuid) != null);
    }
}
