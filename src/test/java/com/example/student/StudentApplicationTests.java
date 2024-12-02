package com.example.student;

import com.example.student.entity.Student;
import com.example.student.exception.ApplicationException;
import com.example.student.repository.StudentRepository;
import com.example.student.service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class StudentServiceTest {

	@Mock
	private StudentRepository studentRepository;

	@InjectMocks
	private StudentService studentService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void testGetAllStudents_databaseError() {
		when(studentRepository.findAll()).thenThrow(new RuntimeException("Database error"));

		ApplicationException exception = assertThrows(
				ApplicationException.class,
				() -> studentService.getAllStudents()
		);

		assert exception.getErrorType() == ApplicationException.ErrorType.DATABASE_ERROR;
	}

	@Test
	void testAddStudent_duplicateError() {
		Student student = new Student();
		student.setStudentNumber("12345");

		when(studentRepository.existsByStudentNumber("12345")).thenReturn(true);

		ApplicationException exception = assertThrows(
				ApplicationException.class,
				() -> studentService.addStudent(student)
		);

		assert exception.getErrorType() == ApplicationException.ErrorType.DUPLICATE;
	}

	@Test
	void testGetStudentById_notFoundError() {
		when(studentRepository.findByStudentNumber("12345")).thenReturn(Optional.empty());

		ApplicationException exception = assertThrows(
				ApplicationException.class,
				() -> studentService.getStudentById("12345")
		);

		assert exception.getErrorType() == ApplicationException.ErrorType.NOT_FOUND;
	}

	@Test
	void testUpdateStudent_databaseError() {
		Student student = new Student();
		student.setStudentNumber("12345");
		student.setFullName("Test Name");
		student.setMark(95.0);

		when(studentRepository.findByStudentNumber("12345"))
				.thenReturn(Optional.of(student));
		when(studentRepository.save(any(Student.class)))
				.thenThrow(new RuntimeException("Database error"));

		ApplicationException exception = assertThrows(
				ApplicationException.class,
				() -> studentService.updateStudent("12345", student)
		);

		assert exception.getErrorType() == ApplicationException.ErrorType.DATABASE_ERROR;
	}

	@Test
	void testDeleteStudent_notFoundError() {
		when(studentRepository.findByStudentNumber("12345")).thenReturn(Optional.empty());

		ApplicationException exception = assertThrows(
				ApplicationException.class,
				() -> studentService.deleteStudent("12345")
		);

		assert exception.getErrorType() == ApplicationException.ErrorType.NOT_FOUND;
	}
}
