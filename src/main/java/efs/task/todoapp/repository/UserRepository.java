package efs.task.todoapp.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class UserRepository implements Repository<String, UserEntity> {
    Map<String, UserEntity> userEntityMap = new HashMap<>();

    @Override
    public String save(UserEntity userEntity) {
        userEntityMap.put(userEntity.username, userEntity);

        if (userEntityMap.get(userEntity.username).equals(userEntity))
            return userEntity.username;
        else
            return null;

    }

    @Override
    public UserEntity query(String s) {
        return userEntityMap.getOrDefault(s, null);
    }

    @Override
    public List<UserEntity> query(Predicate<UserEntity> condition) {
        return userEntityMap.values().stream().filter(condition).collect(Collectors.toList());
    }

    @Override
    public UserEntity update(String s, UserEntity userEntity) {
        if (userEntityMap.containsKey(s)) {
            userEntityMap.replace(s, userEntity);
            return userEntity;
        } else {
            return null;
        }
    }

    @Override
    public boolean delete(String s) {
        return (userEntityMap.remove(s) != null);
    }
}
