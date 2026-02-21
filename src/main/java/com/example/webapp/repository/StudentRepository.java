package com.example.webapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.webapp.entity.Student;

public interface StudentRepository extends JpaRepository<Student, Long> {
    // All CRUD methods are inherited from JpaRepository
}
