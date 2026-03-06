package com.schoolsync.schoolsync_webapp.controller;

import com.schoolsync.schoolsync_webapp.model.*;
import com.schoolsync.schoolsync_webapp.repository.*;
import com.schoolsync.schoolsync_webapp.service.TeachingAssignmentService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/assignments")
public class TeachingAssignmentController {

    private final TeachingAssignmentService service;
    private final TeachingAssignmentRepository assignmentRepo;
    private final TeacherRepository teacherRepo;
    private final SubjectRepository subjectRepo;
    private final ClassGroupRepository classGroupRepo;
    private final UserRepository userRepo;

    public TeachingAssignmentController(TeachingAssignmentService service,
                                          TeachingAssignmentRepository assignmentRepo,
                                          TeacherRepository teacherRepo,
                                          SubjectRepository subjectRepo,
                                          ClassGroupRepository classGroupRepo,
                                          UserRepository userRepo) {
        this.service = service;
        this.assignmentRepo = assignmentRepo;
        this.teacherRepo = teacherRepo;
        this.subjectRepo = subjectRepo;
        this.classGroupRepo = classGroupRepo;
        this.userRepo = userRepo;
    }

    @GetMapping
    public String list(@AuthenticationPrincipal UserDetails user, Model model) {
        List<TeachingAssignment> assignments;

        boolean isTeacher = user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER"));
        boolean isAdmin = user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isTeacher) {
            AppUser u = userRepo.findByUsername(user.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + user.getUsername()));
            Teacher t = teacherRepo.findByUser_Id(u.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Profil professeur non lié à ce compte."));
            assignments = assignmentRepo.findByTeacher_Id(t.getId());
            model.addAttribute("currentTeacherId", t.getId());
        } else {
            assignments = service.getAll();
        }

        model.addAttribute("assignments", assignments);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isTeacher", isTeacher);
        return "assignments/list";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("assignment", new TeachingAssignment());
        model.addAttribute("teachers", teacherRepo.findAll());
        model.addAttribute("subjects", subjectRepo.findAll());
        model.addAttribute("classGroups", classGroupRepo.findAll());
        model.addAttribute("semesters", Semester.values());
        model.addAttribute("pageTitle", "Nouvelle affectation");
        model.addAttribute("formTitle", "Créer une affectation");
        model.addAttribute("actionUrl", "/assignments");
        return "assignments/form";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public String create(@Valid @ModelAttribute("assignment") TeachingAssignment assignment,
                         BindingResult br,
                         @RequestParam Long teacherId,
                         @RequestParam Long subjectId,
                         @RequestParam Long classGroupId,
                         @RequestParam Semester semester,
                         Model model,
                         RedirectAttributes ra) {
        if (br.hasErrors()) {
            model.addAttribute("teachers", teacherRepo.findAll());
            model.addAttribute("subjects", subjectRepo.findAll());
            model.addAttribute("classGroups", classGroupRepo.findAll());
            model.addAttribute("semesters", Semester.values());
            model.addAttribute("pageTitle", "Nouvelle affectation");
            model.addAttribute("formTitle", "Créer une affectation");
            model.addAttribute("actionUrl", "/assignments");
            return "assignments/form";
        }

        assignment.setTeacher(Teacher.builder().id(teacherId).build());
        assignment.setSubject(Subject.builder().id(subjectId).build());
        assignment.setClassGroup(ClassGroup.builder().id(classGroupId).build());
        assignment.setSemester(semester);

        service.create(assignment);
        ra.addFlashAttribute("success", "Affectation créée.");
        return "redirect:/assignments";
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id,
                           @AuthenticationPrincipal UserDetails user,
                           Model model) {
        TeachingAssignment assignment = service.getById(id);
        assertAdminOrOwner(user, assignment);

        model.addAttribute("assignment", assignment);
        model.addAttribute("teachers", teacherRepo.findAll());
        model.addAttribute("subjects", subjectRepo.findAll());
        model.addAttribute("classGroups", classGroupRepo.findAll());
        model.addAttribute("semesters", Semester.values());
        model.addAttribute("pageTitle", "Modifier affectation");
        model.addAttribute("formTitle", "Modifier l'affectation");
        model.addAttribute("actionUrl", "/assignments/" + id);
        return "assignments/form";
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("assignment") TeachingAssignment assignment,
                         BindingResult br,
                         @RequestParam Long teacherId,
                         @RequestParam Long subjectId,
                         @RequestParam Long classGroupId,
                         @RequestParam Semester semester,
                         @AuthenticationPrincipal UserDetails user,
                         Model model,
                         RedirectAttributes ra) {
        TeachingAssignment existing = service.getById(id);
        assertAdminOrOwner(user, existing);

        if (br.hasErrors()) {
            model.addAttribute("teachers", teacherRepo.findAll());
            model.addAttribute("subjects", subjectRepo.findAll());
            model.addAttribute("classGroups", classGroupRepo.findAll());
            model.addAttribute("semesters", Semester.values());
            model.addAttribute("pageTitle", "Modifier affectation");
            model.addAttribute("formTitle", "Modifier l'affectation");
            model.addAttribute("actionUrl", "/assignments/" + id);
            return "assignments/form";
        }

        // Un professeur ne peut pas changer le prof de l'affectation
        boolean isTeacher = user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER"));
        if (isTeacher) {
            teacherId = existing.getTeacher().getId();
        }

        assignment.setTeacher(Teacher.builder().id(teacherId).build());
        assignment.setSubject(Subject.builder().id(subjectId).build());
        assignment.setClassGroup(ClassGroup.builder().id(classGroupId).build());
        assignment.setSemester(semester);

        service.update(id, assignment);
        ra.addFlashAttribute("success", "Affectation modifiée.");
        return "redirect:/assignments";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        service.delete(id);
        ra.addFlashAttribute("success", "Affectation supprimée.");
        return "redirect:/assignments";
    }

    private void assertAdminOrOwner(UserDetails user, TeachingAssignment assignment) {
        boolean isAdmin = user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) return;

        boolean isTeacher = user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER"));
        if (!isTeacher) {
            throw new IllegalArgumentException("Accès refusé.");
        }

        AppUser u = userRepo.findByUsername(user.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + user.getUsername()));
        Teacher t = teacherRepo.findByUser_Id(u.getId())
                .orElseThrow(() -> new IllegalArgumentException("Profil professeur non lié à ce compte."));

        if (assignment.getTeacher() == null || assignment.getTeacher().getId() == null || !assignment.getTeacher().getId().equals(t.getId())) {
            throw new IllegalArgumentException("Accès refusé : cette affectation ne vous appartient pas.");
        }
    }
}
