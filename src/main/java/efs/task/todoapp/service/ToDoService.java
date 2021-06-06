package efs.task.todoapp.service;

import efs.task.todoapp.repository.TaskEntity;
import efs.task.todoapp.repository.TaskRepository;
import efs.task.todoapp.repository.UserEntity;
import efs.task.todoapp.repository.UserRepository;

import java.util.*;

public class ToDoService {
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    private final Map<UUID, String> taskUserMap;

    public ToDoService(UserRepository userRepository, TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.taskUserMap = new HashMap<>();
    }

    public ToDoService() {
        this.userRepository = new UserRepository();
        this.taskRepository = new TaskRepository();
        this.taskUserMap = new HashMap<>();
    }

    public boolean AddUser(UserEntity userEntity) {
        if (userRepository.query(userEntity.getUsername()) == null) {
            String addedUser = userRepository.save(userEntity);
            return (addedUser.equals(userEntity.getUsername()));
        } else {
            return false;
        }
    }


    public String Authenticate(String token) {
        List<UserEntity> foundUsers = userRepository.query(userEntity -> userEntity.encode().equals(token));
        if (foundUsers.size() > 0)
            return foundUsers.get(0).getUsername();
        else
            return null;

    }

    public boolean AddTask(String username, TaskEntity newTask) {
        if (taskRepository.save(newTask) == null)
            return false;

        taskUserMap.put(newTask.getId(), username);

        return true;

    }

    public List<TaskEntity> GetTasks(String username) {
        List<TaskEntity> userTasks = new ArrayList<>();

        taskUserMap.forEach((uuid, s) -> {
            if (s.equals(username)) userTasks.add(taskRepository.query(uuid));
        });

        return userTasks;

    }

    public boolean TaskExists(UUID uuid) {
        return taskRepository.query(uuid) != null;
    }

    public boolean TaskBelongsToUser(String username, UUID uuid) {
        return taskUserMap.get(uuid).equals(username);
    }

    public TaskEntity GetTask(UUID uuid) {
        return taskRepository.query(uuid);
    }

    public void removeTask(UUID uuid) {
        taskRepository.delete(uuid);
        taskUserMap.remove(uuid);

    }

    public boolean UpdateTask(TaskEntity newTask) {
        taskRepository.query(newTask.getId()).setDescription(newTask.getDescription());
        taskRepository.query(newTask.getId()).setDue(newTask.getDue());
        return true;
    }
}