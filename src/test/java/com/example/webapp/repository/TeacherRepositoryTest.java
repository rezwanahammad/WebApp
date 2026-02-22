package com.example.webapp.repository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import com.example.webapp.entity.Teacher;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class TeacherRepositoryTest {

    @Autowired
    private TeacherRepository teacherRepository;

    @Test
    void save_persistsTeacher_andAssignsId() {
        Teacher teacher = new Teacher();
        teacher.setName("Dr. Smith");
        teacher.setEmail("smith@example.com");
        teacher.setPhone("01700000001");
        teacher.setSubject("Math");

        Teacher saved = teacherRepository.save(teacher);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Dr. Smith");
    }

    @Test
    void findByEmail_returnsTeacher_whenExists() {
        Teacher teacher = new Teacher();
        teacher.setName("Dr. Jones");
        teacher.setEmail("jones@example.com");
        teacher.setPhone("01700000002");
        teacher.setSubject("Physics");
        teacherRepository.save(teacher);

        Optional<Teacher> found = teacherRepository.findByEmail("jones@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getSubject()).isEqualTo("Physics");
    }

    @Test
    void existsByEmail_returnsTrue_whenTeacherExists() {
        Teacher teacher = new Teacher();
        teacher.setName("Dr. Brown");
        teacher.setEmail("brown@example.com");
        teacher.setPhone("01700000003");
        teacher.setSubject("Chemistry");
        teacherRepository.save(teacher);

        assertThat(teacherRepository.existsByEmail("brown@example.com")).isTrue();
    }

    @Test
    void deleteById_removesTeacher() {
        Teacher teacher = new Teacher();
        teacher.setName("Dr. Green");
        teacher.setEmail("green@example.com");
        teacher.setPhone("01700000004");
        teacher.setSubject("History");
        Teacher saved = teacherRepository.save(teacher);
        Long id = saved.getId();

        teacherRepository.deleteById(id);

        assertThat(teacherRepository.findById(id)).isEmpty();
    }
}
