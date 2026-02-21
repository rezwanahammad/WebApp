package com.example.webapp.repository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.example.webapp.entity.Role;
import com.example.webapp.entity.User;

/**
 * Integration tests for {@link UserRepository}.
 * Uses a real PostgreSQL database via Testcontainers.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class UserRepositoryTest {

    @Container
    @ServiceConnection
    @SuppressWarnings("unused") // Used by Testcontainers framework
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @Autowired
    private UserRepository userRepository;

    private User buildUser(String username, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("encoded_password");
        user.setRole(role);
        return user;
    }

    // ─── save & findByUsername ───────────────────────────────────────────────

    @Test
    void findByUsername_returnsUser_whenExists() {
        userRepository.save(buildUser("alice", Role.STUDENT));

        Optional<User> found = userRepository.findByUsername("alice");

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("alice");
        assertThat(found.get().getRole()).isEqualTo(Role.STUDENT);
    }

    @Test
    void findByUsername_returnsEmpty_whenNotExists() {
        Optional<User> found = userRepository.findByUsername("ghost");

        assertThat(found).isEmpty();
    }

    // ─── existsByUsername ────────────────────────────────────────────────────

    @Test
    void existsByUsername_returnsTrue_whenUserExists() {
        userRepository.save(buildUser("bob", Role.TEACHER));

        assertThat(userRepository.existsByUsername("bob")).isTrue();
    }

    @Test
    void existsByUsername_returnsFalse_whenUserDoesNotExist() {
        assertThat(userRepository.existsByUsername("nobody")).isFalse();
    }

    // ─── save & findById ─────────────────────────────────────────────────────

    @Test
    void save_persistsUser_andAssignsId() {
        User saved = userRepository.save(buildUser("charlie", Role.TEACHER));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUsername()).isEqualTo("charlie");
    }

    // ─── delete ──────────────────────────────────────────────────────────────

    @Test
    void delete_removesUser_fromDatabase() {
        User saved = userRepository.save(buildUser("diana", Role.STUDENT));
        Long id = saved.getId();

        userRepository.deleteById(id);

        assertThat(userRepository.findById(id)).isEmpty();
    }
}
