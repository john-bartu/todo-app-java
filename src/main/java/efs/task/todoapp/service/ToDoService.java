package efs.task.todoapp.service;

import efs.task.todoapp.repository.TaskEntity;
import efs.task.todoapp.repository.TaskRepository;
import efs.task.todoapp.repository.UserEntity;
import efs.task.todoapp.repository.UserRepository;
import efs.task.todoapp.service.exceptions.*;

import java.util.*;
import java.util.logging.Logger;

public class ToDoService {
    private static final Logger LOGGER = Logger.getLogger(ToDoService.class.getName());

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    private final Map<UUID, String> taskUserMap;

    public ToDoService() {
        this.userRepository = new UserRepository();
        this.taskRepository = new TaskRepository();
        this.taskUserMap = new HashMap<>();
    }

    public void AddUser(UserEntity userEntity) throws Conflict, BadRequest {
        if (userRepository.query(userEntity.getUsername()) == null) {
            String addedUser = userRepository.save(userEntity);
            if (!addedUser.equals(userEntity.getUsername()))
                throw new BadRequest("Saving user failed");
        } else {
            throw new Conflict("User with given login exists");
        }
    }


    public String authenticate(String token) throws BadRequest, Unauthorized {

        UserEntity.checkToken(token);
        List<UserEntity> foundUsers = userRepository.query(ue -> ue.encode().equals(token));

        if (foundUsers.size() > 0) {
            LOGGER.info("Authenticating FAILED TOKEN:" + token);
            return foundUsers.get(0).getUsername();
        } else {
            LOGGER.info("Authenticating FAILED token:" + token);
            throw new Unauthorized(token);
        }


    }

    public void addUserTask(String username, TaskEntity newTask) throws BadRequest {
        newTask.assignUUID();
        LOGGER.info("Trying add task:\n for: " + username + "task: " + newTask);

        if (taskRepository.save(newTask) == null)
            throw new BadRequest("Adding task failed");

        taskUserMap.put(newTask.getId(), username);

    }

    public List<TaskEntity> getUserTasks(String username) {
        List<TaskEntity> userTasks = new ArrayList<>();

        taskUserMap.forEach((uuid, s) -> {
            if (s.equals(username)) userTasks.add(taskRepository.query(uuid));
        });

        LOGGER.info("Getting tasks:\n for: " + username + "\n Tasks:" +
                Arrays.toString(userTasks.toArray()));
        return userTasks;

    }

    public boolean isTaskExists(UUID uuid) {
        boolean check = taskRepository.query(uuid) != null;
        LOGGER.info("Checking if task: { " + uuid + " } exists ?" + check);
        return taskRepository.query(uuid) != null;
    }

    public boolean isTaskBelongsToUser(String username, UUID uuid) {
        boolean check = taskUserMap.get(uuid).equals(username);
        LOGGER.info("Checking if task: { " + uuid + " } belongs to user: {" + username + "} ?" + check);
        return check;
    }

    public TaskEntity getTask(UUID uuid) {
        TaskEntity task = taskRepository.query(uuid);
        LOGGER.info("Getting task: " + task.toString());
        return task;
    }

    public void removeUserTask(String username, UUID uuid) throws NotFound, Forbidden {

        if (!isTaskExists(uuid))
            throw new NotFound("Task with given uuid does not exists");

        if (!isTaskBelongsToUser(username, uuid)) {
            throw new Forbidden("Task belongs to other user");
        }

        taskRepository.delete(uuid);
        LOGGER.info("Removing task {" + uuid + "}");
        taskUserMap.remove(uuid);

    }

    public TaskEntity updateUserTask(String username, TaskEntity updateTask) throws NotFound, Forbidden {
        if (!isTaskExists(updateTask.getId()))
            throw new NotFound("Task with given uuid does not exists");

        LOGGER.info("Updating task\n FROM: {" + taskRepository.query(updateTask.getId()).toString() + "\n} TO: {" + updateTask + "}");

        if (!isTaskBelongsToUser(username, updateTask.getId()))
            throw new Forbidden("Task belongs to other user");

        taskRepository.update(updateTask.getId(),updateTask);

        return taskRepository.query(updateTask.getId());
    }
}