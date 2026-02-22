package com.example.webapp.repository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import com.example.webapp.entity.Course;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class CourseRepositoryTest {

    @Autowired
    private CourseRepository courseRepository;

    @Test
    void save_persistsCourse_andAssignsId() {
        Course course = new Course();
        course.setName("Data Structures");
        course.setCourseCode("CSE101");
        course.setCredits(3);
        
        Course saved = courseRepository.save(course);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCourseCode()).isEqualTo("CSE101");
    }

    @Test
    void findByCourseCode_returnsCourse_whenExists() {
        Course course = new Course();
        course.setName("Algorithms");
        course.setCourseCode("CSE102");
        course.setCredits(3);
        courseRepository.save(course);

        Optional<Course> found = courseRepository.findByCourseCode("CSE102");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Algorithms");
        assertThat(found.get().getCredits()).isEqualTo(3);
    }

    @Test
    void existsByCourseCode_returnsTrue_whenCourseExists() {
        Course course = new Course();
        course.setName("Operating Systems");
        course.setCourseCode("CSE201");
        course.setCredits(4);
        courseRepository.save(course);

        assertThat(courseRepository.existsByCourseCode("CSE201")).isTrue();
    }

    @Test
    void findAll_returnsAllPersistedCourses() {
        Course course1 = new Course();
        course1.setName("Networking");
        course1.setCourseCode("CSE301");
        course1.setCredits(3);
        courseRepository.save(course1);

        Course course2 = new Course();
        course2.setName("Databases");
        course2.setCourseCode("CSE302");
        course2.setCredits(3);
        courseRepository.save(course2);

        List<Course> courses = courseRepository.findAll();

        assertThat(courses).hasSizeGreaterThanOrEqualTo(2);
        assertThat(courses).extracting(Course::getCourseCode)
                .contains("CSE301", "CSE302");
    }
}
