package com.example.student.controller;

import com.example.student.entity.Student;
import com.example.student.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/students")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @GetMapping
    public String listStudents(Model model) {
        model.addAttribute("students", studentService.getAllStudents());
        return "students/list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("student", new Student());
        return "students/form";
    }

    @PostMapping("/add")
    public String addStudent(@Valid @ModelAttribute("student") Student student,
                             BindingResult result,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "students/form";
        }

        try {
            studentService.addStudent(student);
            redirectAttributes.addFlashAttribute("success", "Student added successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/students";
    }

    @GetMapping("/edit/{studentNumber}")
    public String showEditForm(@PathVariable String studentNumber, Model model) {
        studentService.getStudentById(studentNumber)
                .ifPresent(student -> model.addAttribute("student", student));
        return "students/form";
    }

    @PostMapping("/edit/{studentNumber}")
    public String updateStudent(@PathVariable String studentNumber,
                                @Valid @ModelAttribute("student") Student student,
                                BindingResult result,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "students/form";
        }

        studentService.updateStudent(studentNumber, student);
        redirectAttributes.addFlashAttribute("success", "Student updated successfully!");
        return "redirect:/students";
    }

    @GetMapping("/delete/{studentNumber}")
    public String deleteStudent(@PathVariable String studentNumber,
                                RedirectAttributes redirectAttributes) {
        studentService.deleteStudent(studentNumber);
        redirectAttributes.addFlashAttribute("success", "Student deleted successfully!");
        return "redirect:/students";
    }

    @GetMapping("/sorted/mark")
    public String listStudentsByMark(Model model) {
        model.addAttribute("students", studentService.getAllStudentsSortedByMark());
        return "students/list";
    }

    @GetMapping("/sorted/name")
    public String listStudentsByName(Model model) {
        model.addAttribute("students", studentService.getAllStudentsSortedByName());
        return "students/list";
    }

    // New method for searching by name or student number
    @GetMapping("/search")
    public String searchStudent(@RequestParam("query") String query, Model model) {
        List<Student> students = studentService.getStudentsByNameOrStudentNumber(query);
        model.addAttribute("students", students);
        return "students/list";
    }

}
