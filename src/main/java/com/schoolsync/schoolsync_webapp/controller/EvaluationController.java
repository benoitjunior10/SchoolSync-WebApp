package com.schoolsync.schoolsync_webapp.controller;

import com.schoolsync.schoolsync_webapp.model.*;
import com.schoolsync.schoolsync_webapp.repository.*;
import com.schoolsync.schoolsync_webapp.service.EvaluationService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/evaluations")
public class EvaluationController {

    private final EvaluationService service;
    private final EvaluationRepository evalRepo;
    private final TeachingAssignmentRepository assignmentRepo;
    private final TeacherRepository teacherRepo;
    private final UserRepository userRepo;

    public EvaluationController(EvaluationService service,
                                  EvaluationRepository evalRepo,
                                  TeachingAssignmentRepository assignmentRepo,
                                  TeacherRepository teacherRepo,
                                  UserRepository userRepo) {
        this.service = service;
        this.evalRepo = evalRepo;
        this.assignmentRepo = assignmentRepo;
        this.teacherRepo = teacherRepo;
        this.userRepo = userRepo;
    }

    @GetMapping
    public String list(@AuthenticationPrincipal UserDetails user,
                       @RequestParam(required = false) Long assignmentId,
                       Model model) {

        List<TeachingAssignment> visibleAssignments;
        boolean isTeacher = user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER"));
        if (isTeacher) {
            AppUser u = userRepo.findByUsername(user.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + user.getUsername()));
            Teacher t = teacherRepo.findByUser_Id(u.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Profil professeur non lié à ce compte."));
            visibleAssignments = assignmentRepo.findByTeacher_Id(t.getId());
        } else {
            visibleAssignments = assignmentRepo.findAll();
        }

        List<Evaluation> evaluations;
        if (assignmentId != null) {
            evaluations = evalRepo.findByAssignment_Id(assignmentId);
        } else {
            List<Long> ids = visibleAssignments.stream().map(TeachingAssignment::getId).toList();
            evaluations = ids.isEmpty() ? List.of() : evalRepo.findByAssignment_IdIn(ids);
        }

        model.addAttribute("evaluations", evaluations);
        model.addAttribute("assignments", visibleAssignments);
        model.addAttribute("selectedAssignmentId", assignmentId);
        return "evaluations/list";
    }

    @GetMapping("/new")
    public String createForm(@AuthenticationPrincipal UserDetails user, Model model) {
        model.addAttribute("evaluation", new Evaluation());
        model.addAttribute("assignments", visibleAssignments(user));
        model.addAttribute("semesters", Semester.values());
        return "evaluations/form";
    }

    @PostMapping
    public String create(@AuthenticationPrincipal UserDetails user,
                         @Valid @ModelAttribute("evaluation") Evaluation evaluation,
                         BindingResult br,
                         @RequestParam Long assignmentId,
                         Model model,
                         RedirectAttributes ra) {
        if (br.hasErrors()) {
            model.addAttribute("assignments", visibleAssignments(user));
            model.addAttribute("semesters", Semester.values());
            return "evaluations/form";
        }
        evaluation.setAssignment(TeachingAssignment.builder().id(assignmentId).build());
        service.create(evaluation, user);
        ra.addFlashAttribute("success", "Évaluation créée.");
        return "redirect:/evaluations";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id,
                           @AuthenticationPrincipal UserDetails user,
                           Model model) {
        Evaluation ev = service.getById(id);
        model.addAttribute("evaluation", ev);
        model.addAttribute("assignments", visibleAssignments(user));
        model.addAttribute("semesters", Semester.values());
        model.addAttribute("selectedAssignmentId", ev.getAssignment() != null ? ev.getAssignment().getId() : null);
        return "evaluations/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails user,
                         @Valid @ModelAttribute("evaluation") Evaluation evaluation,
                         BindingResult br,
                         @RequestParam Long assignmentId,
                         Model model,
                         RedirectAttributes ra) {
        if (br.hasErrors()) {
            model.addAttribute("assignments", visibleAssignments(user));
            model.addAttribute("semesters", Semester.values());
            model.addAttribute("selectedAssignmentId", assignmentId);
            return "evaluations/form";
        }
        // assignmentId non modifiable via le service (il garde l'assignment existant); on l'ignore.
        service.update(id, evaluation, user);
        ra.addFlashAttribute("success", "Évaluation mise à jour.");
        return "redirect:/evaluations";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails user,
                         RedirectAttributes ra) {
        service.delete(id, user);
        ra.addFlashAttribute("success", "Évaluation supprimée.");
        return "redirect:/evaluations";
    }

    private List<TeachingAssignment> visibleAssignments(UserDetails user) {
        boolean isTeacher = user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER"));
        if (!isTeacher) return assignmentRepo.findAll();

        AppUser u = userRepo.findByUsername(user.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + user.getUsername()));
        Teacher t = teacherRepo.findByUser_Id(u.getId())
                .orElseThrow(() -> new IllegalArgumentException("Profil professeur non lié à ce compte."));
        return assignmentRepo.findByTeacher_Id(t.getId());
    }
}
