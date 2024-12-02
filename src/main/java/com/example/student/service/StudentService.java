package com.example.student.service;

import com.example.student.entity.Student;
import com.example.student.exception.ApplicationException;
import com.example.student.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class StudentService {
    private static final Logger logger = LoggerFactory.getLogger(StudentService.class);

    @Autowired
    private StudentRepository studentRepository;

    public List<Student> getAllStudents() {
        try {
            return studentRepository.findAll();
        } catch (Exception e) {
            logger.error("Error fetching students: {}", e.getMessage());
            throw ApplicationException.databaseError("Error fetching all students", e);
        }
    }


    // New linear search method
    public Student binarySearchByName(String name) {
        List<Student> sortedStudents = getAllStudentsSortedByName();
        int left = 0;
        int right = sortedStudents.size() - 1;

        while (left <= right) {
            int mid = left + (right - left) / 2;
            Student midStudent = sortedStudents.get(mid);

            int comparison = midStudent.getFullName().compareToIgnoreCase(name);
            if (comparison == 0) {
                return midStudent; // Student found
            } else if (comparison < 0) {
                left = mid + 1; // Move right
            } else {
                right = mid - 1; // Move left
            }
        }
        return null; // Student not found
    }

    public Optional<Student> linearSearchByStudentId(String studentNumber) {
        List<Student> allStudents = studentRepository.findAll();
        for (Student student : allStudents) {
            if (student.getStudentNumber().equalsIgnoreCase(studentNumber)) {
                return Optional.of(student); // Student found
            }
        }
        return Optional.empty(); // Student not found
    }

    public Student addStudent(Student student) {
        // Validate student data before processing
        validateStudentData(student);

        // Check for duplicate student number
        if (studentRepository.existsByStudentNumber(student.getStudentNumber())) {
            throw ApplicationException.duplicate("Student number already exists");
        }

        try {
            Student savedStudent = studentRepository.save(student);

            // Sort students after adding
            LinkedList<Student> students = new LinkedList<>(studentRepository.findAll());
            bubbleSortByMark(students);
            insertionSortByName(students);

            logger.info("Student added successfully: {}", savedStudent.getStudentNumber());
            return savedStudent;
        } catch (DataIntegrityViolationException e) {
            logger.error("Database constraint violation: {}", e.getMessage());
            throw ApplicationException.databaseError("Error adding new student", e);
        } catch (Exception e) {
            logger.error("Unexpected error adding student: {}", e.getMessage());
            throw ApplicationException.databaseError("Unexpected error adding student", e);
        }
    }

    private void validateStudentData(Student student) {
        // Validate student number
        if (student.getStudentNumber() == null || student.getStudentNumber().trim().isEmpty()) {
            throw ApplicationException.validation("Student number cannot be empty");
        }

        // Validate full name - no numbers or special characters
        validateFullName(student.getFullName());

        // Validate mark range
        if (student.getMark() < 0 || student.getMark() > 10) {
            throw ApplicationException.validation("Mark must be between 0 and 10");
        }
    }

    private void validateFullName(String fullName) {
        // Trim the full name to remove leading and trailing whitespaces
        String trimmedName = fullName.trim();

        // Check for null or empty after trimming
        if (trimmedName.isEmpty()) {
            throw ApplicationException.validation("Full name cannot be empty");
        }

        // Check minimum length AFTER trimming
        if (trimmedName.length() < 2) {
            throw ApplicationException.validation("Full name must be at least 2 characters");
        }

        // Validate no numbers or special characters
        // Uses a regex that allows only letters, spaces, and accented characters
        String nameValidationRegex = "^[\\p{L}\\s]+$";
        if (!trimmedName.matches(nameValidationRegex)) {
            throw ApplicationException.validation("Full name must contain only letters and spaces");
        }

        // Prevent excessive whitespace
        if (trimmedName.contains("  ")) {
            throw ApplicationException.validation("Full name cannot contain consecutive spaces");
        }

        // Optional: Check for minimum words
        String[] nameParts = trimmedName.split("\\s+");
        if (nameParts.length < 1) {
            throw ApplicationException.validation("Full name must contain at least one word");
        }
    }

    public Optional<Student> getStudentById(String studentNumber) {
        return studentRepository.findByStudentNumber(studentNumber)
                .or(() -> {
                    logger.warn("Student not found: {}", studentNumber);
                    throw ApplicationException.notFound("Student not found with number: " + studentNumber);
                });
    }

    public List<Student> getStudentsByNameOrStudentNumber(String query) {
        try {
            List<Student> results = studentRepository.findByFullNameContainingIgnoreCaseOrStudentNumber(query, query);

            if (results.isEmpty()) {
                logger.info("No students found for query: {}", query);
                // Return an empty list instead of throwing an exception
                return Collections.emptyList();
            }

            return results;
        } catch (Exception e) {
            logger.error("Search failed for query: {}", query);
            throw ApplicationException.databaseError("Error during student search", e);
        }
    }

    public Student updateStudent(String studentNumber, Student studentDetails) {
        try {
            // Find existing student or throw not found exception
            Student existingStudent = studentRepository.findByStudentNumber(studentNumber)
                    .orElseThrow(() -> ApplicationException.notFound(
                            "Student not found with number: " + studentNumber));

            // Validate and update details
            validateStudentData(studentDetails);

            existingStudent.setFullName(studentDetails.getFullName());
            existingStudent.setMark(studentDetails.getMark());

            logger.info("Student updated: {}", studentNumber);
            return studentRepository.save(existingStudent);
        } catch (DataIntegrityViolationException e) {
            logger.error("Update failed: {}", e.getMessage());
            throw ApplicationException.databaseError("Failed to update student", e);
        }
    }

    public void deleteStudent(String studentNumber) {
        try {
            // Find student or throw not found exception
            Student student = studentRepository.findByStudentNumber(studentNumber)
                    .orElseThrow(() -> ApplicationException.notFound(
                            "Student not found with number: " + studentNumber));

            // Attempt deletion
            studentRepository.delete(student);
            logger.info("Student deleted: {}", studentNumber);
        } catch (EmptyResultDataAccessException e) {
            logger.error("Delete failed - no such student: {}", studentNumber);
            throw ApplicationException.notFound("No student found to delete");
        } catch (DataIntegrityViolationException e) {
            logger.error("Delete prevented by database constraints: {}", studentNumber);
            throw ApplicationException.databaseError("Cannot delete student due to dependencies", e);
        }
    }

    public LinkedList<Student> getAllStudentsSortedByMark() {
        LinkedList<Student> students = new LinkedList<>(studentRepository.findAll());
        bubbleSortByMark(students);
        return students;
    }

    public LinkedList<Student> getAllStudentsSortedByName() {
        LinkedList<Student> students = new LinkedList<>(studentRepository.findAll());
        Collections.sort(students, (s1, s2) ->
                s1.getFullName().compareToIgnoreCase(s2.getFullName()));
        return students;
    }

    private void bubbleSortByMark(LinkedList<Student> students) {
        int n = students.size();
        for (int i = 0; i < n - 1; i++) {
            boolean swapped = false;
            for (int j = 0; j < n - i - 1; j++) {
                if (students.get(j).getMark() > students.get(j + 1).getMark()) {
                    Collections.swap(students, j, j + 1);
                    swapped = true;
                }
            }
            if (!swapped) {
                break;
            }
        }
    }

    private void insertionSortByName(LinkedList<Student> students) {
        for (int i = 1; i < students.size(); i++) {
            Student current = students.get(i);
            int j = i - 1;

            while (j >= 0 && students.get(j).getFullName().compareToIgnoreCase(current.getFullName()) > 0) {
                students.set(j + 1, students.get(j));
                j--;
            }
            students.set(j + 1, current);
        }
    }
}