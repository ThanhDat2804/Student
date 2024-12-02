package com.example.student.repository;

import com.example.student.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByStudentNumber(String studentNumber);
    boolean existsByStudentNumber(String studentNumber);
    List<Student> findByFullNameContainingIgnoreCaseOrStudentNumber(String fullName, String studentNumber);
}
