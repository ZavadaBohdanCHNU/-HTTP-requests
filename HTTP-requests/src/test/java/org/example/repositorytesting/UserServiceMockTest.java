package org.example.repositorytesting;

import org.example.repositorytesting.model.User;
import org.example.repositorytesting.repository.UserRepository;
import org.example.repositorytesting.service.UserService;
import org.example.repositorytesting.service.UserCreateRequest;
import org.example.repositorytesting.service.UserUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

/**
 *   @author Bohdan 
 *   @project repositorytesting
 *   @class UserServiceMockTest.java
 *   @version 1.0
 *   @since 5/16/2025 15:45
 */

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class UserServiceMockTest {

    @Mock
    private UserRepository mockRepository;

    private UserService underTest;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private User testUser;
    private List<User> testUsers;

    @BeforeEach
    void setUp() {
        // Create service with mocked repository
        underTest = new UserService(mockRepository);

        // Prepare test data
        testUser = User.builder()
                .id("1")
                .name("David Gilmour")
                .code("Pink Floyd")
                .description("guitar, vocal")
                .createDate(LocalDateTime.now())
                .updateDates(new ArrayList<>())
                .build();

        testUsers = Arrays.asList(
                testUser,
                User.builder()
                        .id("2")
                        .name("Roger Waters")
                        .code("Pink Floyd")
                        .description("bass, vocal")
                        .createDate(LocalDateTime.now())
                        .updateDates(new ArrayList<>())
                        .build()
        );
    }

    @Test
    @DisplayName("Should create new user when valid data provided")
    void whenCreateUser_thenSuccess() {
        // given
        given(mockRepository.save(any(User.class))).willReturn(testUser);

        // when
        User created = underTest.create(testUser);

        // then
        then(mockRepository).should().save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertThat(created).isNotNull();
        assertThat(created).isEqualTo(testUser);
        assertThat(capturedUser.getName()).isEqualTo(testUser.getName());
        assertThat(capturedUser.getCreateDate()).isNotNull();
        assertThat(capturedUser.getUpdateDates()).isEmpty();
    }

    @Test
    @DisplayName("Should create user from request object")
    void whenCreateUserFromRequest_thenSuccess() {
        // given
        UserCreateRequest request = new UserCreateRequest("John Lennon", "Beatles", "rhythm guitar, vocal");
        given(mockRepository.save(any(User.class))).willReturn(testUser);

        // when
        User created = underTest.create(request);

        // then
        then(mockRepository).should().save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertThat(created).isNotNull();
        assertThat(capturedUser.getName()).isEqualTo(request.name());
        assertThat(capturedUser.getCode()).isEqualTo(request.code());
        assertThat(capturedUser.getDescription()).isEqualTo(request.description());
        assertThat(capturedUser.getCreateDate()).isNotNull();
    }

    @Test
    @DisplayName("Should handle update with request object")
    void whenUpdateUserFromRequest_thenSuccess() {
        // given
        UserUpdateRequest request = new UserUpdateRequest("1", "John Lennon", "Beatles", "updated description");
        User existingUser = testUser;
        given(mockRepository.findById(request.id())).willReturn(Optional.of(existingUser));
        given(mockRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        User updated = underTest.update(request);

        // then
        then(mockRepository).should().save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertThat(updated).isNotNull();
        assertThat(capturedUser.getDescription()).isEqualTo(request.description());
        assertThat(capturedUser.getUpdateDates()).isNotEmpty();
        assertThat(capturedUser.getCreateDate()).isEqualTo(existingUser.getCreateDate());
    }

    @Test
    @DisplayName("Should create user with current date when no date provided")
    void whenCreateUserWithoutDate_thenUseCurrentDate() {
        // given
        User userWithoutDate = User.builder()
                .id("3")
                .name("New User")
                .code("New Band")
                .description("new instrument, new vocal")
                .build();
        given(mockRepository.save(any(User.class))).willReturn(userWithoutDate);        // when
        underTest.create(userWithoutDate);

        // then
        then(mockRepository).should().save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getCreateDate()).isNotNull();
        assertThat(capturedUser.getUpdateDates()).isEmpty();
    }

    @Test
    @DisplayName("Should find user by exact name")
    void whenFindByExactName_thenSuccess() {
        // given
        String name = "David Gilmour";
        given(mockRepository.findByName(name)).willReturn(Arrays.asList(testUser));

        // when
        List<User> found = underTest.findByName(name);

        // then
        then(mockRepository).should().findByName(name);
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getName()).isEqualTo(name);
    }

    @Test
    @DisplayName("Should update user and track modification date")
    void whenUpdateUser_thenTrackModificationDate() {        // given
        given(mockRepository.findById(testUser.getId())).willReturn(Optional.of(testUser));
        given(mockRepository.save(any(User.class))).willReturn(testUser);

        // when
        underTest.update(testUser);

        // then
        then(mockRepository).should().save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getUpdateDates()).isNotEmpty();
    }

    @Test
    @DisplayName("Should preserve creation date when updating")
    void whenUpdateUser_thenPreserveCreationDate() {        // given
        LocalDateTime originalCreateDate = testUser.getCreateDate();
        given(mockRepository.findById(testUser.getId())).willReturn(Optional.of(testUser));
        given(mockRepository.save(any(User.class))).willReturn(testUser);

        // when
        underTest.update(testUser);

        // then
        then(mockRepository).should().save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getCreateDate()).isEqualTo(originalCreateDate);
    }

    @Test
    @DisplayName("Should handle multiple updates and track all modifications")
    void whenUpdateMultipleTimes_thenTrackAllModifications() {
        // given
        given(mockRepository.findById(testUser.getId())).willReturn(Optional.of(testUser));
        given(mockRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        User firstUpdate = underTest.update(testUser);
        User secondUpdate = underTest.update(firstUpdate);

        // then
        verify(mockRepository, times(2)).save(any(User.class));
        assertThat(secondUpdate.getUpdateDates()).hasSize(2);
    }

    @Test
    @DisplayName("Should handle non-existent user update")
    void whenUpdateNonExistentUser_thenReturnNull() {
        // given
        given(mockRepository.findById(any())).willReturn(Optional.empty());

        // when
        User result = underTest.update(testUser);

        // then
        assertThat(result).isNull();
        verify(mockRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should handle batch deletions")
    void whenDeleteMultipleUsers_thenSuccess() {
        // given
        doNothing().when(mockRepository).deleteById(any());

        // when
        testUsers.forEach(user -> underTest.delete(user.getId()));

        // then
        verify(mockRepository, times(testUsers.size())).deleteById(any());
    }

    @Test
    @DisplayName("Should return empty list for name not found")
    void whenFindByNonExistentName_thenEmptyList() {
        // given
        String nonExistentName = "Non Existent";
        given(mockRepository.findByName(nonExistentName)).willReturn(new ArrayList<>());

        // when
        List<User> result = underTest.findByName(nonExistentName);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should handle special characters in name search")
    void whenSearchWithSpecialCharacters_thenSuccess() {
        // given
        String nameWithSpecialChars = "O'Connor";
        User specialUser = User.builder()
                .name(nameWithSpecialChars)
                .code("Solo")
                .build();
        given(mockRepository.findByName(nameWithSpecialChars)).willReturn(Arrays.asList(specialUser));

        // when
        List<User> found = underTest.findByName(nameWithSpecialChars);

        // then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getName()).isEqualTo(nameWithSpecialChars);
    }

    @Test
    @DisplayName("Should return all users when requested")
    void whenGetAll_thenReturnAllUsers() {
        // given
        given(mockRepository.findAll()).willReturn(testUsers);

        // when
        List<User> result = underTest.getAll();

        // then
        assertThat(result).hasSize(testUsers.size());
        verify(mockRepository).findAll();
    }
}
