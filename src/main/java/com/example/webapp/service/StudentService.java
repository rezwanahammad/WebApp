package com.example.webapp.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.webapp.dto.StudentDTO;
import com.example.webapp.entity.Department;
import com.example.webapp.entity.Student;
import com.example.webapp.repository.DepartmentRepository;
import com.example.webapp.repository.StudentRepository;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final DepartmentRepository departmentRepository;

    public StudentService(StudentRepository studentRepository, DepartmentRepository departmentRepository) {
        this.studentRepository = studentRepository;
        this.departmentRepository = departmentRepository;
    }

    public List<StudentDTO> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public StudentDTO getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + id));
        return convertToDTO(student);
    }

    public StudentDTO saveStudent(StudentDTO studentDTO) {
        Student student = new Student();
        student.setName(studentDTO.getName());
        student.setRoll(studentDTO.getRoll());
        student.setEmail(studentDTO.getEmail());
        student.setPhone(studentDTO.getPhone());
        
        if (studentDTO.getDepartmentId() != null) {
            Department department = departmentRepository.findById(studentDTO.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found with id: " + studentDTO.getDepartmentId()));
            student.setDepartment(department);
        }
        
        Student savedStudent = studentRepository.save(student);
        return convertToDTO(savedStudent);
    }
    
    public StudentDTO updateStudent(Long id, StudentDTO studentDTO) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + id));
        
        student.setName(studentDTO.getName());
        student.setRoll(studentDTO.getRoll());
        student.setEmail(studentDTO.getEmail());
        student.setPhone(studentDTO.getPhone());
        
        if (studentDTO.getDepartmentId() != null) {
            Department department = departmentRepository.findById(studentDTO.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found with id: " + studentDTO.getDepartmentId()));
            student.setDepartment(department);
        }
        
        Student updatedStudent = studentRepository.save(student);
        return convertToDTO(updatedStudent);
    }
    
    public void deleteStudent(Long id) {
        if (!studentRepository.existsById(id)) {
            throw new RuntimeException("Student not found with id: " + id);
        }
        studentRepository.deleteById(id);
    }
    
    private StudentDTO convertToDTO(Student student) {
        StudentDTO dto = new StudentDTO();
        dto.setId(student.getId());
        dto.setName(student.getName());
        dto.setRoll(student.getRoll());
        dto.setEmail(student.getEmail());
        dto.setPhone(student.getPhone());
        dto.setDepartmentId(student.getDepartment() != null ? student.getDepartment().getId() : null);
        return dto;
    }
}
