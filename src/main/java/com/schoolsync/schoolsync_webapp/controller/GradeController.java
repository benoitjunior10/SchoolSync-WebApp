package com.schoolsync.schoolsync_webapp.controller;

import com.schoolsync.schoolsync_webapp.model.*;
import com.schoolsync.schoolsync_webapp.repository.*;
import com.schoolsync.schoolsync_webapp.service.GradeService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequestMapping("/grades")
public class GradeController {

    private final GradeService gradeService;
    private final EvaluationRepository evalRepo;
    private final GradeRepository gradeRepo;
    private final StudentRepository studentRepo;
    private final TeachingAssignmentRepository assignmentRepo;
    private final TeacherRepository teacherRepo;
    private final UserRepository userRepo;

    public GradeController(GradeService gradeService,
                             EvaluationRepository evalRepo,
                             GradeRepository gradeRepo,
                             StudentRepository studentRepo,
                             TeachingAssignmentRepository assignmentRepo,
                             TeacherRepository teacherRepo,
                             UserRepository userRepo) {
        this.gradeService = gradeService;
        this.evalRepo = evalRepo;
        this.gradeRepo = gradeRepo;
        this.studentRepo = studentRepo;
        this.assignmentRepo = assignmentRepo;
        this.teacherRepo = teacherRepo;
        this.userRepo = userRepo;
    }

    @GetMapping
    public String pickEvaluation(@AuthenticationPrincipal UserDetails user,
                                 @RequestParam(required = false) Long evaluationId,
                                 Model model) {
        List<TeachingAssignment> visibleAssignments = visibleAssignments(user);
        List<Long> aIds = visibleAssignments.stream().map(TeachingAssignment::getId).toList();
        List<Evaluation> visibleEvaluations = aIds.isEmpty() ? List.of() : evalRepo.findByAssignment_IdIn(aIds);

        model.addAttribute("evaluations", visibleEvaluations);
        model.addAttribute("selectedEvaluationId", evaluationId);

        if (evaluationId == null) {
            return "grades/pick";
        }

        Evaluation ev = evalRepo.findById(evaluationId)
                .orElseThrow(() -> new IllegalArgumentException("Evaluation not found: " + evaluationId));

        Long classGroupId = ev.getAssignment().getClassGroup().getId();
        List<Student> students = studentRepo.findByClassGroup_Id(classGroupId);
        List<Grade> existing = gradeRepo.findByEvaluation_Id(evaluationId);

        Map<Long, Double> scoreByStudentId = new HashMap<>();
        for (Grade g : existing) scoreByStudentId.put(g.getStudent().getId(), g.getScore());

        model.addAttribute("evaluation", ev);
        model.addAttribute("students", students);
        model.addAttribute("scoreByStudentId", scoreByStudentId);

        return "grades/edit";
    }

    @PostMapping("/save")
    public String save(@AuthenticationPrincipal UserDetails user,
                       @RequestParam Long evaluationId,
                       @RequestParam(name = "studentId") List<Long> studentIds,
                       @RequestParam(name = "score") List<String> rawScores,
                       RedirectAttributes ra) {

        if (studentIds.size() != rawScores.size()) {
            throw new IllegalArgumentException("Paramètres invalides: studentId/score.");
        }

        List<GradeService.GradeCreateRequest> reqs = new ArrayList<>();
        for (int i = 0; i < studentIds.size(); i++) {
            String raw = rawScores.get(i);
            if (raw == null || raw.isBlank()) continue; // champ vide = pas de note
            Double sc;
            try {
                sc = Double.parseDouble(raw.trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Score invalide : \"" + raw + "\"");
            }
            reqs.add(new GradeService.GradeCreateRequest(studentIds.get(i), evaluationId, sc));
        }

        gradeService.bulkUpsert(reqs, user);
        ra.addFlashAttribute("success", "Notes enregistrées.");
        return "redirect:/grades?evaluationId=" + evaluationId;
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
