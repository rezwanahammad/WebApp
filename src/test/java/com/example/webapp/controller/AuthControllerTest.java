package com.example.webapp.controller;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.example.webapp.config.SecurityConfig;
import com.example.webapp.entity.Role;
import com.example.webapp.entity.User;
import com.example.webapp.repository.UserRepository;
import com.example.webapp.service.CustomUserDetailsService;

/**
 * Unit (slice) tests for {@link AuthController}.
 * Uses @WebMvcTest so only the web layer is loaded; all other beans are mocked.
 * SecurityConfig is explicitly imported so that PasswordEncoder and the
 * SecurityFilterChain are available in the slice context.
 */
@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Required by SecurityConfig's DaoAuthenticationProvider
    @MockitoBean
    @SuppressWarnings("unused") // Used by Spring Security via @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private UserRepository userRepository;

    // ─── GET /auth/register ──────────────────────────────────────────────────

    @Test
    void showRegisterForm_returns200_withRegisterView() throws Exception {
        mockMvc.perform(get("/auth/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("user"));
    }

    // ─── GET /login ──────────────────────────────────────────────────────────

    @Test
    void showLoginForm_returns200_withLoginView() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    // ─── POST /auth/register ─────────────────────────────────────────────────

    @Test
    void registerUser_redirectsToLogin_whenUsernameIsNew() throws Exception {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);

        mockMvc.perform(post("/auth/register")
                        .param("username", "newuser")
                        .param("password", "secret")
                        .param("role", Role.STUDENT.name()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));

        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_returnsRegisterView_withError_whenUsernameExists() throws Exception {
        when(userRepository.existsByUsername("existing")).thenReturn(true);

        mockMvc.perform(post("/auth/register")
                        .param("username", "existing")
                        .param("password", "secret")
                        .param("role", Role.STUDENT.name()))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("error"));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_savesUser_withEncodedPassword() throws Exception {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);

        mockMvc.perform(post("/auth/register")
                        .param("username", "teacher1")
                        .param("password", "plaintext")
                        .param("role", Role.TEACHER.name()))
                .andExpect(status().is3xxRedirection());

        verify(userRepository).save(any(User.class));
    }
}
