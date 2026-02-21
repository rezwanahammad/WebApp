package com.example.webapp.service;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.webapp.entity.Role;
import com.example.webapp.entity.User;
import com.example.webapp.repository.UserRepository;

/**
 * Pure unit tests for {@link CustomUserDetailsService}.
 * No Spring context — uses Mockito only.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    private User studentUser;
    private User teacherUser;

    @BeforeEach
    @SuppressWarnings("unused") // Invoked by JUnit via @BeforeEach
    void setUp() {
        studentUser = new User(1L, "alice", "hashed_password", Role.STUDENT);
        teacherUser = new User(2L, "bob", "hashed_password", Role.TEACHER);
    }

    // ─── loadUserByUsername ──────────────────────────────────────────────────

    @Test
    void loadUserByUsername_returnsUserDetails_whenUserExists() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(studentUser));

        UserDetails details = userDetailsService.loadUserByUsername("alice");

        assertThat(details.getUsername()).isEqualTo("alice");
        assertThat(details.getPassword()).isEqualTo("hashed_password");
        assertThat(details.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_STUDENT");
    }

    @Test
    void loadUserByUsername_assignsCorrectRole_forTeacher() {
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(teacherUser));

        UserDetails details = userDetailsService.loadUserByUsername("bob");

        assertThat(details.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_TEACHER");
    }

    @Test
    void loadUserByUsername_throwsUsernameNotFoundException_whenUserNotFound() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("ghost"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("ghost");
    }

    // ─── createUser ──────────────────────────────────────────────────────────

    @Test
    void createUser_encodesPasswordBeforeSaving() {
        User raw = new User(null, "charlie", "plaintext", Role.STUDENT);
        User saved = new User(3L, "charlie", "encoded", Role.STUDENT);

        when(passwordEncoder.encode("plaintext")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(saved);

        User result = userDetailsService.createUser(raw);

        verify(passwordEncoder).encode("plaintext");
        verify(userRepository).save(raw);
        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getPassword()).isEqualTo("encoded");
    }

    @Test
    void createUser_savesToRepository() {
        User raw = new User(null, "diana", "pass", Role.TEACHER);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(raw);

        userDetailsService.createUser(raw);

        verify(userRepository).save(raw);
    }
}
