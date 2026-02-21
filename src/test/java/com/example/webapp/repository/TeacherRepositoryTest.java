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

import com.example.webapp.entity.Teacher;

/**
 * Integration tests for {@link TeacherRepository}.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class TeacherRepositoryTest {

    @Container
    @ServiceConnection
    @SuppressWarnings("unused") // Used by Testcontainers framework
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @Autowired
    private TeacherRepository teacherRepository;

    private Teacher buildTeacher(String name, String email, String subject) {
        Teacher t = new Teacher();
        t.setName(name);
        t.setEmail(email);
        t.setPhone("01700000001");
        t.setSubject(subject);
        return t;
    }

    // ─── save ────────────────────────────────────────────────────────────────

    @Test
    void save_persistsTeacher_andAssignsId() {
        Teacher saved = teacherRepository.save(buildTeacher("Dr. Smith", "smith@example.com", "Math"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Dr. Smith");
    }

    // ─── findByEmail ─────────────────────────────────────────────────────────

    @Test
    void findByEmail_returnsTeacher_whenExists() {
        teacherRepository.save(buildTeacher("Dr. Jones", "jones@example.com", "Physics"));

        Optional<Teacher> found = teacherRepository.findByEmail("jones@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getSubject()).isEqualTo("Physics");
    }

    @Test
    void findByEmail_returnsEmpty_whenNotExists() {
        Optional<Teacher> found = teacherRepository.findByEmail("ghost@example.com");

        assertThat(found).isEmpty();
    }

    // ─── existsByEmail ────────────────────────────────────────────────────────

    @Test
    void existsByEmail_returnsTrue_whenTeacherExists() {
        teacherRepository.save(buildTeacher("Dr. Brown", "brown@example.com", "Chemistry"));

        assertThat(teacherRepository.existsByEmail("brown@example.com")).isTrue();
    }

    @Test
    void existsByEmail_returnsFalse_whenTeacherDoesNotExist() {
        assertThat(teacherRepository.existsByEmail("nobody@example.com")).isFalse();
    }

    // ─── findById & delete ────────────────────────────────────────────────────

    @Test
    void findById_returnsTeacher_whenExists() {
        Teacher saved = teacherRepository.save(buildTeacher("Dr. White", "white@example.com", "Biology"));

        assertThat(teacherRepository.findById(saved.getId())).isPresent();
    }

    @Test
    void deleteById_removesTeacher() {
        Teacher saved = teacherRepository.save(buildTeacher("Dr. Green", "green@example.com", "History"));
        Long id = saved.getId();

        teacherRepository.deleteById(id);

        assertThat(teacherRepository.findById(id)).isEmpty();
    }
}
