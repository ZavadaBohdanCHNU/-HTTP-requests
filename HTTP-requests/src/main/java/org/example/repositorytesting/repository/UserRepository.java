package org.example.repositorytesting.repository;

import org.example.repositorytesting.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface UserRepository extends MongoRepository<User, String> {
    List<User> findByName(String name);
    List<User> findByCode(String code);
    List<User> findByDescriptionContaining(String description);
}
