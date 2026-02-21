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
import com.example.webapp.dto.TeacherDTO;
import com.example.webapp.service.CustomUserDetailsService;
import com.example.webapp.service.TeacherService;

import tools.jackson.databind.ObjectMapper;

/**
 * Unit (slice) tests for {@link TeacherController}.
 * SecurityConfig is imported explicitly so that the real SecurityFilterChain
 * (URL-level role rules) and @EnableMethodSecurity are present in the slice.
 */
@WebMvcTest(TeacherController.class)
@Import(SecurityConfig.class)
class TeacherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TeacherService teacherService;

    @MockitoBean
    @SuppressWarnings("unused") // Used by Spring Security via @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    // ─── GET /api/teachers ───────────────────────────────────────────────────

    @Test
    void getAllTeachers_returns200_forStudent() throws Exception {
        when(teacherService.getAllTeachers()).thenReturn(
                List.of(new TeacherDTO(1L, "Dr. Smith", "smith@example.com", "01700000001", "Math"))
        );

        mockMvc.perform(get("/api/teachers").with(user("student").roles("STUDENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Dr. Smith"))
                .andExpect(jsonPath("$[0].subject").value("Math"));
    }

    @Test
    void getAllTeachers_returns401_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/teachers"))
                .andExpect(status().isUnauthorized());
    }

    // ─── GET /api/teachers/{id} ──────────────────────────────────────────────

    @Test
    void getTeacherById_returns200_withCorrectData() throws Exception {
        TeacherDTO teacher = new TeacherDTO(1L, "Dr. Smith", "smith@example.com", "01700000001", "Physics");
        when(teacherService.getTeacherById(1L)).thenReturn(teacher);

        mockMvc.perform(get("/api/teachers/1").with(user("teacher").roles("TEACHER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Dr. Smith"))
                .andExpect(jsonPath("$.subject").value("Physics"));
    }

    // ─── POST /api/teachers ──────────────────────────────────────────────────

    @Test
    void createTeacher_returns201_forTeacher() throws Exception {
        TeacherDTO input = new TeacherDTO(null, "Dr. Jones", "jones@example.com", "01733333333", "Chemistry");
        TeacherDTO saved = new TeacherDTO(2L, "Dr. Jones", "jones@example.com", "01733333333", "Chemistry");
        when(teacherService.createTeacher(any(TeacherDTO.class))).thenReturn(saved);

        mockMvc.perform(post("/api/teachers")
                        .with(user("teacher").roles("TEACHER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("Dr. Jones"));
    }

    @Test
    void createTeacher_returns403_forStudent() throws Exception {
        TeacherDTO input = new TeacherDTO(null, "Dr. Jones", "jones@example.com", "01733333333", "Chemistry");

        mockMvc.perform(post("/api/teachers")
                        .with(user("student").roles("STUDENT"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isForbidden());
    }

    // ─── PUT /api/teachers/{id} ──────────────────────────────────────────────

    @Test
    void updateTeacher_returns200_withUpdatedData() throws Exception {
        TeacherDTO input = new TeacherDTO(null, "Updated Name", "updated@example.com", "01744444444", "Biology");
        TeacherDTO updated = new TeacherDTO(1L, "Updated Name", "updated@example.com", "01744444444", "Biology");
        when(teacherService.updateTeacher(eq(1L), any(TeacherDTO.class))).thenReturn(updated);

        mockMvc.perform(put("/api/teachers/1")
                        .with(user("teacher").roles("TEACHER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.subject").value("Biology"));
    }

    // ─── DELETE /api/teachers/{id} ───────────────────────────────────────────

    @Test
    void deleteTeacher_returns204_forTeacher() throws Exception {
        doNothing().when(teacherService).deleteTeacher(1L);

        mockMvc.perform(delete("/api/teachers/1").with(user("teacher").roles("TEACHER")).with(csrf()))
                .andExpect(status().isNoContent());

        verify(teacherService).deleteTeacher(1L);
    }

    @Test
    void deleteTeacher_returns403_forStudent() throws Exception {
        mockMvc.perform(delete("/api/teachers/1").with(user("student").roles("STUDENT")).with(csrf()))
                .andExpect(status().isForbidden());
    }
}
