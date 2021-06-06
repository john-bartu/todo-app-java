package efs.task.todoapp.service;

import efs.task.todoapp.repository.TaskRepository;
import efs.task.todoapp.repository.UserEntity;
import efs.task.todoapp.repository.UserRepository;

import java.util.*;

public class ToDoService {
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    private final Map<String, UUID> userTaskMap;

    public ToDoService(UserRepository userRepository, TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.userTaskMap = new HashMap<>();
    }

    public ToDoService() {
        this.userRepository = new UserRepository();
        this.taskRepository = new TaskRepository();
        this.userTaskMap = new HashMap<>();
    }

    public boolean AddUser(UserEntity userEntity) {
        if (userRepository.query(userEntity.getUsername()) == null) {
            String addedUser = userRepository.save(userEntity);
            return (addedUser.equals(userEntity.getUsername()));
        } else {
            return false;
        }
    }


    public boolean Authenticate(String token) {
        List<UserEntity> foundUsers = userRepository.query(userEntity -> userEntity.encode().equals(token));

        return foundUsers.size() == 1;

    }
}