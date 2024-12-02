package com.example.student.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "student")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(unique = true, nullable = false)
    @NotBlank(message = "Student number is required")
    private String studentNumber;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @Min(value = 0, message = "Mark must be at least 0")
    @Max(value = 10, message = "Mark must not exceed 10")
    private double mark;

    // Default constructor
    public Student() {}

    // Parameterized constructor
    public Student(Long id, String studentNumber, String fullName, double mark) {
        this.id = id;
        this.studentNumber = studentNumber;
        this.fullName = fullName;
        this.mark = mark;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public void setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public double getMark() {
        return mark;
    }

    public void setMark(double mark) {
        this.mark = mark;
    }

    // Custom getter for rank that calculates on-the-fly
    @Transient
    public String getRank() {
        if (mark >= 0 && mark < 5) {
            return "Fail";
        } else if (mark >= 5 && mark < 6.5) {
            return "Medium";
        } else if (mark >= 6.5 && mark < 7.5) {
            return "Good";
        } else if (mark >= 7.5 && mark < 9) {
            return "Very Good";
        } else if (mark >= 9 && mark <= 10) {
            return "Excellent";
        }
        return "Invalid";
    }
}
