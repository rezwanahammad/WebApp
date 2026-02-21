package com.example.webapp.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.example.webapp.entity.Role;
import com.example.webapp.entity.User;
import com.example.webapp.repository.UserRepository;

/**
 * Integration tests for security configuration:
 * - Public endpoints are accessible without authentication.
 * - Protected endpoints return 401/403 when accessed without the correct role.
 * - Form login succeeds with valid credentials.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class SecurityConfigTest {

    @Container
    @ServiceConnection
    @SuppressWarnings("unused") // Used by Testcontainers framework
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    @SuppressWarnings("unused") // Invoked by JUnit via @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    // ─── Public endpoints ────────────────────────────────────────────────────

    @Test
    void loginPage_isPublic() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
    }

    @Test
    void registerPage_isPublic() throws Exception {
        mockMvc.perform(get("/auth/register"))
                .andExpect(status().isOk());
    }

    @Test
    void studentsListPage_isPublic() throws Exception {
        mockMvc.perform(get("/students"))
                .andExpect(status().isOk());
    }

    // ─── Unauthenticated access to protected pages redirects to login ─────────

    @Test
    void addStudentPage_redirectsToLogin_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/students/add"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    void apiStudentsGet_returns401_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/students"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void apiStudentsPost_returns401_whenUnauthenticated() throws Exception {
        mockMvc.perform(post("/api/students")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    // ─── Role-based access ───────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "STUDENT")
    void apiStudentsGet_returns200_forStudent() throws Exception {
        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void addStudentPage_returns403_forStudent() throws Exception {
        mockMvc.perform(get("/students/add"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void addStudentPage_returns200_forTeacher() throws Exception {
        mockMvc.perform(get("/students/add"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void apiStudentsDelete_returns403_forStudent() throws Exception {
        mockMvc.perform(delete("/api/students/1"))
                .andExpect(status().isForbidden());
    }

    // ─── Form login ──────────────────────────────────────────────────────────

    @Test
    void formLogin_succeeds_withValidCredentials() throws Exception {
        User user = new User();
        user.setUsername("teacher1");
        user.setPassword(passwordEncoder.encode("password"));
        user.setRole(Role.TEACHER);
        userRepository.save(user);

        mockMvc.perform(formLogin("/login").user("teacher1").password("password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/students"));
    }

    @Test
    void formLogin_fails_withInvalidCredentials() throws Exception {
        mockMvc.perform(formLogin("/login").user("nobody").password("wrong"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }
}
