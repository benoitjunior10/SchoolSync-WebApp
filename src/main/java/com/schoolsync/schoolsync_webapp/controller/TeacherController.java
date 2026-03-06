package com.schoolsync.schoolsync_webapp.controller;

import com.schoolsync.schoolsync_webapp.model.Teacher;
import com.schoolsync.schoolsync_webapp.service.TeacherService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/teachers")
public class TeacherController {

    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("teachers", teacherService.getAll());
        return "teachers/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("teacher", new Teacher());
        return "teachers/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("teacher") Teacher teacher,
                         BindingResult br,
                         RedirectAttributes ra) {
        if (br.hasErrors()) return "teachers/form";
        teacherService.create(teacher);
        ra.addFlashAttribute("success", "Professeur créé.");
        return "redirect:/teachers";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("teacher", teacherService.getById(id));
        return "teachers/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("teacher") Teacher teacher,
                         BindingResult br,
                         RedirectAttributes ra) {
        if (br.hasErrors()) return "teachers/form";
        teacherService.update(id, teacher);
        ra.addFlashAttribute("success", "Professeur mis à jour.");
        return "redirect:/teachers";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        teacherService.delete(id);
        ra.addFlashAttribute("success", "Professeur supprimé.");
        return "redirect:/teachers";
    }
}
