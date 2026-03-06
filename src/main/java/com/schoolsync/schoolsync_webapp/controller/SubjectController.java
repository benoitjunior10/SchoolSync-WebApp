package com.schoolsync.schoolsync_webapp;

import com.schoolsync.schoolsync_webapp.model.Subject;
import com.schoolsync.schoolsync_webapp.service.SubjectService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/subjects")
public class SubjectController {

    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("subjects", subjectService.getAll());
        return "subjects/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("subject", new Subject());
        return "subjects/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("subject") Subject subject,
                         BindingResult br,
                         RedirectAttributes ra) {
        if (br.hasErrors()) return "subjects/form";
        subjectService.create(subject);
        ra.addFlashAttribute("success", "Matière créée.");
        return "redirect:/subjects";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("subject", subjectService.getById(id));
        return "subjects/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("subject") Subject subject,
                         BindingResult br,
                         RedirectAttributes ra) {
        if (br.hasErrors()) return "subjects/form";
        subjectService.update(id, subject);
        ra.addFlashAttribute("success", "Matière mise à jour.");
        return "redirect:/subjects";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        subjectService.delete(id);
        ra.addFlashAttribute("success", "Matière supprimée.");
        return "redirect:/subjects";
    }
}
