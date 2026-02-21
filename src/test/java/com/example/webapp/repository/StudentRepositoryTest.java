package com.example.webapp.repository;

import java.util.List;
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

import com.example.webapp.entity.Student;

/**
 * Integration tests for {@link StudentRepository}.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class StudentRepositoryTest {

    @Container
    @ServiceConnection
    @SuppressWarnings("unused") // Used by Testcontainers framework
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @Autowired
    private StudentRepository studentRepository;

    private Student buildStudent(String name, String roll, String email) {
        Student s = new Student();
        s.setName(name);
        s.setRoll(roll);
        s.setEmail(email);
        s.setPhone("01700000000");
        return s;
    }

    // ─── save ─────────────────────────────────────────────────────────────────

    @Test
    void save_persistsStudent_andAssignsId() {
        Student saved = studentRepository.save(buildStudent("Alice", "CS001", "alice@example.com"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Alice");
        assertThat(saved.getRoll()).isEqualTo("CS001");
    }

    // ─── findById ────────────────────────────────────────────────────────────

    @Test
    void findById_returnsStudent_whenExists() {
        Student saved = studentRepository.save(buildStudent("Bob", "CS002", "bob@example.com"));

        Optional<Student> found = studentRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getRoll()).isEqualTo("CS002");
    }

    @Test
    void findById_returnsEmpty_whenNotExists() {
        Optional<Student> found = studentRepository.findById(999L);

        assertThat(found).isEmpty();
    }

    // ─── findAll ─────────────────────────────────────────────────────────────

    @Test
    void findAll_returnsAllPersistedStudents() {
        studentRepository.save(buildStudent("Charlie", "CS003", "charlie@example.com"));
        studentRepository.save(buildStudent("Diana", "CS004", "diana@example.com"));

        List<Student> students = studentRepository.findAll();

        assertThat(students).hasSizeGreaterThanOrEqualTo(2);
    }

    // ─── delete ──────────────────────────────────────────────────────────────

    @Test
    void deleteById_removesStudent() {
        Student saved = studentRepository.save(buildStudent("Eve", "CS005", "eve@example.com"));
        Long id = saved.getId();

        studentRepository.deleteById(id);

        assertThat(studentRepository.findById(id)).isEmpty();
    }

    // ─── unique roll constraint ───────────────────────────────────────────────

    @Test
    void existsById_returnsTrue_afterSave() {
        Student saved = studentRepository.save(buildStudent("Frank", "CS006", "frank@example.com"));

        assertThat(studentRepository.existsById(saved.getId())).isTrue();
    }
}
