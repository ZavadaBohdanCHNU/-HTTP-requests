package org.example.repositorytesting.service;

import org.example.repositorytesting.model.User;
import org.example.repositorytesting.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;

    private List<User> initialUsers = new ArrayList<>();
    {
        initialUsers.add(User.builder()
                .name("Freddie Mercury")
                .code("Queen")
                .description("vocal, piano")
                .createDate(LocalDateTime.now())
                .updateDates(new ArrayList<>())
                .build());
        
        initialUsers.add(User.builder()
                .name("Paul McCartney") 
                .code("Beatles")
                .description("bass, vocal")
                .createDate(LocalDateTime.now())
                .updateDates(new ArrayList<>())
                .build());
                
        initialUsers.add(User.builder()
                .name("Mick Jagger")
                .code("Rolling Stones") 
                .description("vocal")
                .createDate(LocalDateTime.now())
                .updateDates(new ArrayList<>())
                .build());
    }

    @PostConstruct
    void init() {
        repository.deleteAll();
        repository.saveAll(initialUsers);
    }

    public List<User> getAll() {
        return repository.findAll();
    }

    public User getById(String id) {
        return repository.findById(id).orElse(null);
    }

    public User create(User user) {
        user.setCreateDate(LocalDateTime.now());
        user.setUpdateDates(new ArrayList<>());
        return repository.save(user);
    }

    public User create(UserCreateRequest request) {
        User user = mapToUser(request);
        user.setCreateDate(LocalDateTime.now());
        user.setUpdateDates(new ArrayList<>());
        return repository.save(user);
    }

    public User update(User user) {
        User existingUser = repository.findById(user.getId()).orElse(null);
        if (existingUser != null) {
            List<LocalDateTime> updateDates = existingUser.getUpdateDates();
            updateDates.add(LocalDateTime.now());
            user.setUpdateDates(updateDates);
            user.setCreateDate(existingUser.getCreateDate());
            return repository.save(user);
        }
        return null;
    }

    public User update(UserUpdateRequest request) {
        User existingUser = repository.findById(request.id()).orElse(null);
        if (existingUser != null) {
            List<LocalDateTime> updateDates = existingUser.getUpdateDates();
            updateDates.add(LocalDateTime.now());
            
            User userToUpdate = User.builder()
                    .id(request.id())
                    .name(request.name())
                    .code(request.code())
                    .description(request.description())
                    .createDate(existingUser.getCreateDate())
                    .updateDates(updateDates)
                    .build();
                    
            return repository.save(userToUpdate);
        }
        return null;
    }

    public void delete(String id) {
        repository.deleteById(id);
    }

    public List<User> findByName(String name) {
        return repository.findByName(name);
    }

    public List<User> findByCode(String code) {
        return repository.findByCode(code);
    }

    private User mapToUser(UserCreateRequest request) {
        return User.builder()
                .name(request.name())
                .code(request.code())
                .description(request.description())
                .build();
    }
}
