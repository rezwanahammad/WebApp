package com.example.webapp.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.webapp.dto.StudentDTO;
import com.example.webapp.service.StudentService;

@Controller
@RequestMapping("/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public String getStudents(Model model) {
        model.addAttribute("students", studentService.getAllStudents());
        return "students";
    }

    @GetMapping("/add")
    public String addStudent(Model model) {
        model.addAttribute("student", new StudentDTO());
        return "student-form";
    }

    @PostMapping("/store")
    @PreAuthorize("hasRole('TEACHER')")
    public String storeStudent(@ModelAttribute("student") StudentDTO studentDTO, Model model) {
        studentService.saveStudent(studentDTO);
        return "redirect:/students";
    }
    
    @GetMapping("/delete/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public String deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return "redirect:/students";
    }
}

