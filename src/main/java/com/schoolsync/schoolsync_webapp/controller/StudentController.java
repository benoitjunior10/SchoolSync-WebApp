package com.schoolsync.schoolsync_webapp.controller;

import com.schoolsync.schoolsync_webapp.model.*;
import com.schoolsync.schoolsync_webapp.repository.ClassGroupRepository;
import com.schoolsync.schoolsync_webapp.service.ReportService;
import com.schoolsync.schoolsync_webapp.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/students")
public class StudentController {

    private final StudentService studentService;
    private final ClassGroupRepository classGroupRepo;
    private final ReportService reportService;

    public StudentController(StudentService studentService, ClassGroupRepository classGroupRepo, ReportService reportService) {
        this.studentService = studentService;
        this.classGroupRepo = classGroupRepo;
        this.reportService = reportService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("students", studentService.getAll());
        return "students/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("student", new Student());
        model.addAttribute("classGroups", classGroupRepo.findAll());
        return "students/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("student") Student student,
                         BindingResult br,
                         @RequestParam("classGroupId") Long classGroupId,
                         Model model,
                         RedirectAttributes ra) {
        if (br.hasErrors()) {
            model.addAttribute("classGroups", classGroupRepo.findAll());
            return "students/form";
        }
        student.setClassGroup(ClassGroup.builder().id(classGroupId).build());
        studentService.create(student);
        ra.addFlashAttribute("success", "Étudiant créé.");
        return "redirect:/students";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Student st = studentService.getById(id);
        model.addAttribute("student", st);
        model.addAttribute("classGroups", classGroupRepo.findAll());
        model.addAttribute("selectedClassGroupId", st.getClassGroup() != null ? st.getClassGroup().getId() : null);
        return "students/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("student") Student student,
                         BindingResult br,
                         @RequestParam("classGroupId") Long classGroupId,
                         Model model,
                         RedirectAttributes ra) {
        if (br.hasErrors()) {
            model.addAttribute("classGroups", classGroupRepo.findAll());
            model.addAttribute("selectedClassGroupId", classGroupId);
            return "students/form";
        }
        student.setClassGroup(ClassGroup.builder().id(classGroupId).build());
        studentService.update(id, student);
        ra.addFlashAttribute("success", "Étudiant mis à jour.");
        return "redirect:/students";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        studentService.delete(id);
        ra.addFlashAttribute("success", "Étudiant supprimé.");
        return "redirect:/students";
    }

    @GetMapping("/{id}/report")
    public String report(@PathVariable Long id,
                         @RequestParam(required = false) String year,
                         @RequestParam(required = false) Semester semester,
                         Model model) {
        ReportService.StudentReport report;
        if (year == null && semester == null) report = reportService.getStudentReport(id);
        else report = reportService.getStudentReport(id, year, semester);

        model.addAttribute("report", report);
        model.addAttribute("year", year);
        model.addAttribute("semester", semester);
        return "students/report";
    }
}
