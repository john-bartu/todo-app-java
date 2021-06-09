package efs.task.todoapp.service;

import efs.task.todoapp.repository.TaskEntity;
import efs.task.todoapp.repository.TaskRepository;
import efs.task.todoapp.repository.UserEntity;
import efs.task.todoapp.repository.UserRepository;

import java.util.*;
import java.util.logging.Logger;

public class ToDoService {
    private static final Logger LOGGER = Logger.getLogger(ToDoService.class.getName());

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

    public boolean AddUser(UserEntity userEntity) throws Conflict {
        if (userRepository.query(userEntity.getUsername()) == null) {
            String addedUser = userRepository.save(userEntity);
            return (addedUser.equals(userEntity.getUsername()));
        } else {
            throw new Conflict("User with given login exists");
        }
    }

    void CheckToken(String token) throws BadRequest {


        List<String> test = Arrays.asList(token.split("[\\s:]+"));

        try {
            Base64.getDecoder().decode(test.get(0));
            Base64.getDecoder().decode(test.get(1));

        } catch (IllegalArgumentException e) {
            throw new BadRequest("Token is not a token");
        }

    }


    public String Authenticate(String token) throws BadRequest, Unauthorized {

        LOGGER.info("Authenticating:\n TOKEN:" + token);

        CheckToken(token);
        List<UserEntity> foundUsers = userRepository.query(ue -> ue.encode().equals(token));

        if (foundUsers.size() > 0) {
            LOGGER.info("Authenticated TOKEN:" + token);
            return foundUsers.get(0).getUsername();
        } else {
            LOGGER.info("Unauthenticated TOKEN:" + token);
            throw new Unauthorized(token);
        }


    }

    public void AddTask(String username, TaskEntity newTask) throws BadRequest {
        newTask.assignUUID();
        LOGGER.info("Trying add task:\n for: " + username + "task: " + newTask.toString());

        if (taskRepository.save(newTask) == null)
            throw new BadRequest("Adding task failed");

        taskUserMap.put(newTask.getId(), username);

    }

    public List<TaskEntity> GetTasks(String username) {
        List<TaskEntity> userTasks = new ArrayList<>();

        taskUserMap.forEach((uuid, s) -> {
            if (s.equals(username)) userTasks.add(taskRepository.query(uuid));
        });

        LOGGER.info("Getting tasks:\n for: " + username + "\n Tasks:" +
                Arrays.toString(userTasks.toArray()));
        return userTasks;

    }

    public boolean TaskExists(UUID uuid) {
        boolean check = taskRepository.query(uuid) != null;
        LOGGER.info("Checking if task: { " + uuid + " } exists ?" + check);
        return taskRepository.query(uuid) != null;
    }

    public boolean TaskBelongsToUser(String username, UUID uuid) {
        boolean check = taskUserMap.get(uuid).equals(username);
        LOGGER.info("Checking if task: { " + uuid + " } belongs to user: {" + username + "} ?" + check);
        return check;
    }

    public TaskEntity GetTask(UUID uuid) {
        TaskEntity task = taskRepository.query(uuid);
        LOGGER.info("Getting task: " + task.toString());
        return task;
    }

    public void removeTask(String username, UUID uuid) throws NotFound, Forbidden {

        if (!TaskExists(uuid))
            throw new NotFound("Task with given uuid does not exists");

        if (!TaskBelongsToUser(username, uuid)) {
            throw new Forbidden("Task belongs to other user");
        }

        taskRepository.delete(uuid);
        LOGGER.info("Removing task {" + uuid + "}");
        taskUserMap.remove(uuid);

    }

    public TaskEntity UpdateTask(String username, TaskEntity updateTask) throws NotFound, Forbidden {
        LOGGER.info("Updating task\n FROM: {" + taskRepository.query(updateTask.getId()).toString() + "\n} TO: {" + updateTask + "}");

        if (!TaskExists(updateTask.getId()))
            throw new NotFound("Task with given uuid does not exists");

        if (!TaskBelongsToUser(username, updateTask.getId()))
            throw new Forbidden("Task belongs to other user");


        taskRepository.query(updateTask.getId()).setDescription(updateTask.getDescription());
        taskRepository.query(updateTask.getId()).setDue(updateTask.getDue());
        return taskRepository.query(updateTask.getId());
    }
}