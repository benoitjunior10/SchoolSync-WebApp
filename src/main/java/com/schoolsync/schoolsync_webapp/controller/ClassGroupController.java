package com.schoolsync.schoolsync_webapp.controller;

import com.schoolsync.schoolsync_webapp.model.ClassGroup;
import com.schoolsync.schoolsync_webapp.service.ClassGroupService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/class-groups")
public class ClassGroupController {

    private final ClassGroupService classGroupService;

    public ClassGroupController(ClassGroupService classGroupService) {
        this.classGroupService = classGroupService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("classGroups", classGroupService.getAll());
        return "classgroups/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("classGroup", new ClassGroup());
        return "classgroups/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("classGroup") ClassGroup classGroup,
                         BindingResult br,
                         RedirectAttributes ra) {
        if (br.hasErrors()) return "classgroups/form";
        classGroupService.create(classGroup);
        ra.addFlashAttribute("success", "Classe créée.");
        return "redirect:/class-groups";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("classGroup", classGroupService.getById(id));
        return "classgroups/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("classGroup") ClassGroup classGroup,
                         BindingResult br,
                         RedirectAttributes ra) {
        if (br.hasErrors()) return "classgroups/form";
        classGroupService.update(id, classGroup);
        ra.addFlashAttribute("success", "Classe mise à jour.");
        return "redirect:/class-groups";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        classGroupService.delete(id);
        ra.addFlashAttribute("success", "Classe supprimée.");
        return "redirect:/class-groups";
    }
}
