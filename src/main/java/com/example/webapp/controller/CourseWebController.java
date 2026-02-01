package com.example.webapp.controller;

import com.example.webapp.dto.CourseDTO;
import com.example.webapp.service.CourseService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/courses")
public class CourseWebController {

    private final CourseService courseService;

    public CourseWebController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public String getCourses(Model model) {
        model.addAttribute("courses", courseService.getAllCourses());
        return "courses";
    }

    @GetMapping("/add")
    @PreAuthorize("hasRole('TEACHER')")
    public String addCourse(Model model) {
        model.addAttribute("course", new CourseDTO());
        return "course-form";
    }

    @PostMapping("/store")
    @PreAuthorize("hasRole('TEACHER')")
    public String storeCourse(@ModelAttribute("course") CourseDTO courseDTO) {
        courseService.createCourse(courseDTO);
        return "redirect:/courses";
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public String deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return "redirect:/courses";
    }
}
