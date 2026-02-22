package com.example.webapp.repository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import com.example.webapp.entity.Student;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class StudentRepositoryTest {

    @Autowired
    private StudentRepository studentRepository;

    @Test
    void save_persistsStudent_andAssignsId() {
        Student student = new Student();
        student.setName("Alice");
        student.setRoll("CS001");
        student.setEmail("alice@example.com");
        student.setPhone("01700000000");

        Student saved = studentRepository.save(student);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Alice");
        assertThat(saved.getRoll()).isEqualTo("CS001");
    }

    @Test
    void findById_returnsStudent_whenExists() {
        Student student = new Student();
        student.setName("Bob");
        student.setRoll("CS002");
        student.setEmail("bob@example.com");
        student.setPhone("01700000001");
        Student saved = studentRepository.save(student);

        Optional<Student> found = studentRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getRoll()).isEqualTo("CS002");
    }

    @Test
    void findAll_returnsAllPersistedStudents() {
        Student student1 = new Student();
        student1.setName("Charlie");
        student1.setRoll("CS003");
        student1.setEmail("charlie@example.com");
        student1.setPhone("01700000002");
        studentRepository.save(student1);

        Student student2 = new Student();
        student2.setName("Diana");
        student2.setRoll("CS004");
        student2.setEmail("diana@example.com");
        student2.setPhone("01700000003");
        studentRepository.save(student2);

        List<Student> students = studentRepository.findAll();

        assertThat(students).hasSizeGreaterThanOrEqualTo(2);
        assertThat(students).extracting(Student::getRoll)
                .contains("CS003", "CS004");
    }

    @Test
    void deleteById_removesStudent() {
        Student student = new Student();
        student.setName("Eve");
        student.setRoll("CS005");
        student.setEmail("eve@example.com");
        student.setPhone("01700000004");
        Student saved = studentRepository.save(student);
        Long id = saved.getId();

        studentRepository.deleteById(id);

        assertThat(studentRepository.findById(id)).isEmpty();
    }
}
