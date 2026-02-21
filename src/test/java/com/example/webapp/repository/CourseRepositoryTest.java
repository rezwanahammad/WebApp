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

import com.example.webapp.entity.Course;

/**
 * Integration tests for {@link CourseRepository}.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class CourseRepositoryTest {

    @Container
    @ServiceConnection
    @SuppressWarnings("unused") // Used by Testcontainers framework
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @Autowired
    private CourseRepository courseRepository;

    private Course buildCourse(String name, String code, int credits) {
        Course c = new Course();
        c.setName(name);
        c.setCourseCode(code);
        c.setCredits(credits);
        c.setDescription("A course about " + name);
        return c;
    }

    // ─── save ────────────────────────────────────────────────────────────────

    @Test
    void save_persistsCourse_andAssignsId() {
        Course saved = courseRepository.save(buildCourse("Data Structures", "CSE101", 3));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCourseCode()).isEqualTo("CSE101");
    }

    // ─── findByCourseCode ─────────────────────────────────────────────────────

    @Test
    void findByCourseCode_returnsCourse_whenExists() {
        courseRepository.save(buildCourse("Algorithms", "CSE102", 3));

        Optional<Course> found = courseRepository.findByCourseCode("CSE102");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Algorithms");
        assertThat(found.get().getCredits()).isEqualTo(3);
    }

    @Test
    void findByCourseCode_returnsEmpty_whenNotExists() {
        Optional<Course> found = courseRepository.findByCourseCode("UNKNOWN999");

        assertThat(found).isEmpty();
    }

    // ─── existsByCourseCode ───────────────────────────────────────────────────

    @Test
    void existsByCourseCode_returnsTrue_whenCourseExists() {
        courseRepository.save(buildCourse("Operating Systems", "CSE201", 4));

        assertThat(courseRepository.existsByCourseCode("CSE201")).isTrue();
    }

    @Test
    void existsByCourseCode_returnsFalse_whenCourseDoesNotExist() {
        assertThat(courseRepository.existsByCourseCode("GHOST000")).isFalse();
    }

    // ─── findAll ─────────────────────────────────────────────────────────────

    @Test
    void findAll_returnsAllPersistedCourses() {
        courseRepository.save(buildCourse("Networking", "CSE301", 3));
        courseRepository.save(buildCourse("Databases", "CSE302", 3));

        List<Course> courses = courseRepository.findAll();

        assertThat(courses).hasSizeGreaterThanOrEqualTo(2);
    }

    // ─── delete ──────────────────────────────────────────────────────────────

    @Test
    void deleteById_removesCourse() {
        Course saved = courseRepository.save(buildCourse("Compilers", "CSE401", 3));
        Long id = saved.getId();

        courseRepository.deleteById(id);

        assertThat(courseRepository.findById(id)).isEmpty();
    }
}
