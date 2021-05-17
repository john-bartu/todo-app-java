package efs.task.todoapp.repository;

import java.util.List;
import java.util.function.Predicate;

public class UserRepository implements Repository<String, UserEntity> {
    @Override
    public String save(UserEntity userEntity) {
        return null;
    }

    @Override
    public UserEntity query(String s) {
        return null;
    }

    @Override
    public List<UserEntity> query(Predicate<UserEntity> condition) {
        return null;
    }

    @Override
    public UserEntity update(String s, UserEntity userEntity) {
        return null;
    }

    @Override
    public boolean delete(String s) {
        return false;
    }
}
