/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.schoolsync.schoolsync_webapp.service;

/**
 *
 * @author AQUARIAN
 */
import com.schoolsync.schoolsync_webapp.model.Evaluation;
import com.schoolsync.schoolsync_webapp.model.AppUser;
import com.schoolsync.schoolsync_webapp.model.Teacher;
import com.schoolsync.schoolsync_webapp.model.Semester;
import com.schoolsync.schoolsync_webapp.model.TeachingAssignment;
import com.schoolsync.schoolsync_webapp.repository.EvaluationRepository;
import com.schoolsync.schoolsync_webapp.repository.UserRepository;
import com.schoolsync.schoolsync_webapp.repository.TeacherRepository;
import com.schoolsync.schoolsync_webapp.repository.TeachingAssignmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.security.core.userdetails.UserDetails;

@Service
public class EvaluationService {

    private final EvaluationRepository repo;
    private final TeachingAssignmentRepository assignmentRepo;
    private final UserRepository userRepo;
    private final TeacherRepository teacherRepo;

    public EvaluationService(EvaluationRepository repo, TeachingAssignmentRepository assignmentRepo,
                           UserRepository userRepo, TeacherRepository teacherRepo) {
        this.repo = repo;
        this.assignmentRepo = assignmentRepo;
        this.userRepo = userRepo;
        this.teacherRepo = teacherRepo;
    }


    public Evaluation create(Evaluation e, org.springframework.security.core.userdetails.UserDetails user) {

        TeachingAssignment a = assignmentRepo.findById(e.getAssignment().getId())
        .orElseThrow(() -> new IllegalArgumentException("Assignment not found: " + e.getAssignment().getId()));

        enforceTeacherOwnership(user, a);

        // --- cohérence période (assignment -> evaluation) ---
        // Si le client n'envoie pas year/semester, on les copie depuis l'assignment.
        if (e.getAcademicYear() == null || e.getAcademicYear().isBlank()) {
            e.setAcademicYear(a.getAcademicYear());
        }
        if (e.getSemester() == null) {
            e.setSemester(a.getSemester());
        }

        // On interdit toute incohérence (évite les cas "evaluation S2" sur un assignment S1)
        if (!a.getAcademicYear().equals(e.getAcademicYear()) || a.getSemester() != e.getSemester()) {
            throw new IllegalArgumentException("Evaluation year/semester must match assignment year/semester");
        }
        
        // --- validation weight ---
        if (e.getWeight() == null || e.getWeight() < 0 || e.getWeight() > 100)
            throw new IllegalArgumentException("weight must be between 0 and 100");

        // --- règle : somme des weights <= 100 (par assignment + year + semester)
        double currentSum = repo.sumWeights(a.getId(), e.getAcademicYear(), e.getSemester());
        double newSum = currentSum + e.getWeight();

        if (newSum > 100.0) {
            throw new IllegalArgumentException("Total weights exceed 100 for this assignment/year/semester. Current="
                    + currentSum + ", adding=" + e.getWeight() + ", would become=" + newSum);
        }


        e.setAssignment(a);
        return repo.save(e);

    }
    

    public List<Evaluation> getAll() { return repo.findAll(); }
    
    public List<Evaluation> getByAssignmentAndPeriod(Long assignmentId, String year, Semester semester) {
        return repo.findByAssignment_IdAndAcademicYearAndSemester(assignmentId, year, semester);
    }


    public Evaluation getById(Long id) {
        return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Evaluation not found: " + id));
    }

    public List<Evaluation> getByAssignment(Long assignmentId) {
        return repo.findByAssignment_Id(assignmentId);
    }

    public void delete(Long id, org.springframework.security.core.userdetails.UserDetails user) {
        Evaluation ev = getById(id);
        enforceTeacherOwnership(user, ev.getAssignment());
        repo.delete(ev);
    }


    private void enforceTeacherOwnership(UserDetails user,
                                 TeachingAssignment assignment) {

    boolean isAdmin = user.getAuthorities().stream()
            .map(org.springframework.security.core.GrantedAuthority::getAuthority)
            .anyMatch(a -> a.equals("ROLE_ADMIN"));
    if (isAdmin) return;

    boolean isTeacher = user.getAuthorities().stream()
            .map(org.springframework.security.core.GrantedAuthority::getAuthority)
            .anyMatch(a -> a.equals("ROLE_TEACHER"));

    if (isTeacher) {
        String username = user.getUsername();
        AppUser u = userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        Teacher teacher = teacherRepo.findByUser_Id(u.getId())
                .orElseThrow(() -> new IllegalArgumentException("Teacher profile not linked to this account"));

        Long ownerTeacherId = assignment.getTeacher() != null ? assignment.getTeacher().getId() : null;
        if (ownerTeacherId == null || !ownerTeacherId.equals(teacher.getId())) {
            throw new IllegalArgumentException("Forbidden: not your assignment");
        }
    }
}

public Evaluation update(Long id, Evaluation input, UserDetails user) {
        Evaluation ex = getById(id);
        TeachingAssignment a = ex.getAssignment();

        enforceTeacherOwnership(user, a);

        // période: on garde strictement la période de l'assignment
        // (ça évite d'introduire des incohérences via un UPDATE)
        String year = (input.getAcademicYear() == null || input.getAcademicYear().isBlank())
                ? a.getAcademicYear()
                : input.getAcademicYear();
        Semester sem = (input.getSemester() == null) ? a.getSemester() : input.getSemester();

        if (!a.getAcademicYear().equals(year) || a.getSemester() != sem) {
            throw new IllegalArgumentException("Evaluation year/semester must match assignment year/semester");
        }

        if (input.getWeight() == null || input.getWeight() < 0 || input.getWeight() > 100)
            throw new IllegalArgumentException("weight must be between 0 and 100");

        // somme weights en excluant cette éval
        double sumOthers = repo.sumWeightsExcluding(a.getId(), year, sem, ex.getId());
        double newSum = sumOthers + input.getWeight();

        if (newSum > 100.0) {
            throw new IllegalArgumentException("Total weights exceed 100 for this assignment/year/semester. Others="
                    + sumOthers + ", new=" + input.getWeight() + ", total=" + newSum);
        }

        ex.setTitle(input.getTitle());
        ex.setDate(input.getDate());
        ex.setWeight(input.getWeight());
        ex.setMaxScore(input.getMaxScore());
        ex.setAcademicYear(year);
        ex.setSemester(sem);

        return repo.save(ex);
    }

    
}

