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

    public boolean AddUser(UserEntity userEntity) {
        if (userRepository.query(userEntity.getUsername()) == null) {
            String addedUser = userRepository.save(userEntity);
            return (addedUser.equals(userEntity.getUsername()));
        } else {
            return false;
        }
    }


    public String Authenticate(String token) throws BadRequest, Unauthorized {
        LOGGER.info("Authenticating:\n TOKEN:" + token);

        if (ValidateToken(token)) {

            List<UserEntity> foundUsers = userRepository.query(ue -> ue.encode().equals(token));

            if (foundUsers.size() > 0) {
                return foundUsers.get(0).getUsername();
            } else {
                throw new Unauthorized(token);
            }
        }
        return null;

    }

    private boolean ValidateToken(String token) throws BadRequest {


        if (token == null || token.equals(""))
            throw new BadRequest("Auth token is null or empty");

        if (token.split("[\\s:]+").length != 2)
            throw new BadRequest("Auth token has no properly format XX:XX");

        String un1 = token.split("[\\s:]+")[0];
        if (un1 == null || un1.equals(""))
            throw new BadRequest("Auth token username is empty or null");

        String un2 = token.split("[\\s:]+")[1];
        if (un2 == null || un2.equals(""))
            throw new BadRequest("Auth token password is empty or null");

        return true;
    }

    public boolean AddTask(String username, TaskEntity newTask) {
        newTask.assignUUID();
        LOGGER.info("Trying add task:\n for: " + username + "task: " + newTask.toString());

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
        ;
        LOGGER.info("Checking if task: { " + uuid + " } belongs to user: {" + username + "} ?" + check);
        return check;
    }

    public TaskEntity GetTask(UUID uuid) {
        TaskEntity task = taskRepository.query(uuid);
        LOGGER.info("Getting task: " + task.toString());
        return task;
    }

    public void removeTask(UUID uuid) {
        taskRepository.delete(uuid);
        LOGGER.info("Removing task {" + uuid + "}");
        taskUserMap.remove(uuid);

    }

    public boolean UpdateTask(TaskEntity newTask) {
        LOGGER.info("Updating task\n FROM: {" + taskRepository.query(newTask.getId()).toString() + "\n} TO: {" + newTask.toString() + "}");
        taskRepository.query(newTask.getId()).setDescription(newTask.getDescription());
        taskRepository.query(newTask.getId()).setDue(newTask.getDue());
        return true;
    }
}