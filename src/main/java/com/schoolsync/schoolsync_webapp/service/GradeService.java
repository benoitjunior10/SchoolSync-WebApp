/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.schoolsync.schoolsync_webapp.service;

/**
 *
 * @author AQUARIAN
 */

import com.schoolsync.schoolsync_webapp.model.*;
import com.schoolsync.schoolsync_webapp.model.AppUser;
import com.schoolsync.schoolsync_webapp.repository.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Service
public class GradeService {

    public record GradeCreateRequest(Long studentId, Long evaluationId, Double score) {}

    private final GradeRepository gradeRepo;
    private final StudentRepository studentRepo;
    private final EvaluationRepository evalRepo;
    private final UserRepository userRepo;
    private final TeacherRepository teacherRepo;

    public GradeService(GradeRepository gradeRepo, StudentRepository studentRepo, EvaluationRepository evalRepo,
                       UserRepository userRepo, TeacherRepository teacherRepo) {
        this.gradeRepo = gradeRepo;
        this.studentRepo = studentRepo;
        this.evalRepo = evalRepo;
        this.userRepo = userRepo;
        this.teacherRepo = teacherRepo;
    }

    public Grade upsert(Long studentId, Long evaluationId, Double score, UserDetails user) {

        Student st = studentRepo.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));
        Evaluation ev = evalRepo.findById(evaluationId)
        .orElseThrow(() -> new IllegalArgumentException("Evaluation not found: " + evaluationId));

        enforceTeacherOwnership(user, ev);

        // Cohérence : un prof ne doit pas saisir une note pour un étudiant hors de la classe de l'évaluation
        Long evClassId = ev.getAssignment().getClassGroup().getId();
        Long stClassId = st.getClassGroup() != null ? st.getClassGroup().getId() : null;
        if (stClassId == null || !stClassId.equals(evClassId)) {
            throw new IllegalArgumentException("Student does not belong to the class group of this evaluation");
        }

        
        double max = ev.getMaxScore();
        if (score < 0 || score > max) {
            throw new IllegalArgumentException("Score must be between 0 and " + max);
        }

        Grade g = gradeRepo.findByStudent_IdAndEvaluation_Id(studentId, evaluationId)
                .orElse(Grade.builder().student(st).evaluation(ev).build());

        g.setScore(score);
        return gradeRepo.save(g);
    }

    public List<Grade> bulkUpsert(List<GradeCreateRequest> reqs, UserDetails user) {
        List<Grade> saved = new ArrayList<>();
        for (GradeCreateRequest r : reqs) {
            saved.add(upsert(r.studentId(), r.evaluationId(), r.score(), user));
        }
        return saved;
    }


    public List<Grade> getByEvaluation(Long evaluationId) {
        return gradeRepo.findByEvaluation_Id(evaluationId);
    }

    public List<Grade> getByStudent(Long studentId) {
        return gradeRepo.findByStudent_Id(studentId);
    }
    

    private void enforceTeacherOwnership(UserDetails user, Evaluation ev) {
        boolean isAdmin = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN"));
        if (isAdmin) return;

        boolean isTeacher = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_TEACHER"));

        if (isTeacher) {
            String username = user.getUsername();
            AppUser u = userRepo.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

            Teacher teacher = teacherRepo.findByUser_Id(u.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Teacher profile not linked to this account"));

            Long ownerTeacherId = ev.getAssignment().getTeacher() != null ? ev.getAssignment().getTeacher().getId() : null;
            if (ownerTeacherId == null || !ownerTeacherId.equals(teacher.getId())) {
                throw new IllegalArgumentException("Forbidden: not your evaluation");
            }
        }
    }

}


