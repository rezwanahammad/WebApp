package com.example.webapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

import com.example.webapp.service.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public DaoAuthenticationProvider authenticationProvider(CustomUserDetailsService userDetailsService) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/auth/register")
            )
            .authorizeHttpRequests(auth -> auth
                // Public access - no authentication required
                .requestMatchers("/", "/students", "/courses", "/departments", "/auth/**", "/login", "/error").permitAll()
                
                // Students management - only teachers
                .requestMatchers("/students/add", "/students/edit/**", "/students/delete/**", "/students/store").hasRole("TEACHER")
                
                // Courses management - only teachers
                .requestMatchers("/courses/add", "/courses/edit/**", "/courses/delete/**", "/courses/store").hasRole("TEACHER")
                
                // Departments management - only teachers
                .requestMatchers("/departments/add", "/departments/edit/**", "/departments/delete/**", "/departments/store").hasRole("TEACHER")
                
                // API endpoints with role-based access
                .requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole("STUDENT", "TEACHER")
                .requestMatchers(HttpMethod.POST, "/api/**").hasRole("TEACHER")
                .requestMatchers(HttpMethod.PUT, "/api/**").hasRole("TEACHER")
                .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("TEACHER")
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .exceptionHandling(exception -> exception
                .defaultAuthenticationEntryPointFor(
                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                    request -> request.getServletPath().startsWith("/api/")
                )
            )
            .httpBasic(basic -> {})
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/students", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll());
        
        return http.build();
    }
}
