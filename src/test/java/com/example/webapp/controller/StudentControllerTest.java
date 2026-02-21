package com.example.webapp.controller;

import java.util.List;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.webapp.config.SecurityConfig;
import com.example.webapp.dto.StudentDTO;
import com.example.webapp.service.CustomUserDetailsService;
import com.example.webapp.service.StudentService;

import tools.jackson.databind.ObjectMapper;

/**
 * Unit (slice) tests for {@link StudentRestController}.
 * SecurityConfig is imported explicitly so that the real SecurityFilterChain
 * (URL-level role rules) and @EnableMethodSecurity are present in the slice.
 * CSRF protection is active, so mutation requests that should succeed carry
 * .with(csrf()); requests that should be rejected (wrong role) are blocked
 * either by the role check or by the CSRF check — both produce 403.
 */
@WebMvcTest(StudentRestController.class)
@Import(SecurityConfig.class)
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private StudentService studentService;

    @MockitoBean
    @SuppressWarnings("unused") // Used by Spring Security via @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    // ─── GET /api/students ───────────────────────────────────────────────────

    @Test
    void getAllStudents_returns200_forStudent() throws Exception {
        when(studentService.getAllStudents()).thenReturn(
                List.of(new StudentDTO(1L, "Alice", "CS001", "alice@example.com", "01700000000", 1L))
        );

        mockMvc.perform(get("/api/students").with(user("student").roles("STUDENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Alice"))
                .andExpect(jsonPath("$[0].roll").value("CS001"));
    }

    @Test
    void getAllStudents_returns200_forTeacher() throws Exception {
        when(studentService.getAllStudents()).thenReturn(List.of());
        mockMvc.perform(get("/api/students").with(user("teacher").roles("TEACHER")))
                .andExpect(status().isOk());
    }

    @Test
    void getAllStudents_returns401_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/students"))
                .andExpect(status().isUnauthorized());
    }

    // ─── GET /api/students/{id} ──────────────────────────────────────────────

    @Test
    void getStudentById_returns200_withCorrectData() throws Exception {
        StudentDTO student = new StudentDTO(1L, "Bob", "CS002", "bob@example.com", "01711111111", 2L);
        when(studentService.getStudentById(1L)).thenReturn(student);

        mockMvc.perform(get("/api/students/1").with(user("student").roles("STUDENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Bob"))
                .andExpect(jsonPath("$.roll").value("CS002"));
    }

    // ─── POST /api/students ──────────────────────────────────────────────────

    @Test
    void createStudent_returns201_forTeacher() throws Exception {
        StudentDTO input = new StudentDTO(null, "Charlie", "CS003", "charlie@example.com", "01722222222", 1L);
        StudentDTO saved = new StudentDTO(3L, "Charlie", "CS003", "charlie@example.com", "01722222222", 1L);
        when(studentService.saveStudent(any(StudentDTO.class))).thenReturn(saved);

        mockMvc.perform(post("/api/students")
                        .with(user("teacher").roles("TEACHER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("Charlie"));
    }

    @Test
    void createStudent_returns403_forStudent() throws Exception {
        StudentDTO input = new StudentDTO(null, "Charlie", "CS003", "charlie@example.com", "01722222222", 1L);

        mockMvc.perform(post("/api/students")
                        .with(user("student").roles("STUDENT"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isForbidden());
    }

    // ─── PUT /api/students/{id} ──────────────────────────────────────────────

    @Test
    void updateStudent_returns200_withUpdatedData() throws Exception {
        StudentDTO input = new StudentDTO(null, "Updated", "CS001", "u@example.com", "012", 1L);
        StudentDTO updated = new StudentDTO(1L, "Updated", "CS001", "u@example.com", "012", 1L);
        when(studentService.updateStudent(eq(1L), any(StudentDTO.class))).thenReturn(updated);

        mockMvc.perform(put("/api/students/1")
                        .with(user("teacher").roles("TEACHER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    // ─── DELETE /api/students/{id} ───────────────────────────────────────────

    @Test
    void deleteStudent_returns204_forTeacher() throws Exception {
        doNothing().when(studentService).deleteStudent(1L);

        mockMvc.perform(delete("/api/students/1").with(user("teacher").roles("TEACHER")).with(csrf()))
                .andExpect(status().isNoContent());

        verify(studentService).deleteStudent(1L);
    }

    @Test
    void deleteStudent_returns403_forStudent() throws Exception {
        mockMvc.perform(delete("/api/students/1").with(user("student").roles("STUDENT")).with(csrf()))
                .andExpect(status().isForbidden());
    }
}
