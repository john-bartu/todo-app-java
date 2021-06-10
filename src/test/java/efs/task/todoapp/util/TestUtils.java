package efs.task.todoapp.util;

import efs.task.todoapp.repository.TaskEntity;

import java.util.Base64;
import java.util.UUID;

public class TestUtils {
    public static String createToken(String username, String password) {
        return Base64.getEncoder().encodeToString(username.getBytes()) +
                ":" +
                Base64.getEncoder().encodeToString(password.getBytes());
    }

    public static String randomLogin() {
        return UUID.randomUUID().toString().split("-")[0];
    }


    public static String creteRandomToken() {
        return createToken(randomLogin(), randomLogin());
    }


    public static class TestResponse {
        public int code;

        public TestResponse(int code) {
            this.code = code;
        }
    }

    public static class TestTaskResponse extends TestResponse {
        public TaskEntity taskEntity;

        public TestTaskResponse(int code, TaskEntity taskEntity) {
            super(code);
            this.taskEntity = taskEntity;
        }
    }
}
