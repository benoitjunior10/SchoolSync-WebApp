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
import com.schoolsync.schoolsync_webapp.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeachingAssignmentService {

    private final TeachingAssignmentRepository repo;
    private final TeacherRepository teacherRepo;
    private final SubjectRepository subjectRepo;
    private final ClassGroupRepository classGroupRepo;

    public TeachingAssignmentService(TeachingAssignmentRepository repo,
                                    TeacherRepository teacherRepo,
                                    SubjectRepository subjectRepo,
                                    ClassGroupRepository classGroupRepo) {
        this.repo = repo;
        this.teacherRepo = teacherRepo;
        this.subjectRepo = subjectRepo;
        this.classGroupRepo = classGroupRepo;
    }

    public TeachingAssignment create(TeachingAssignment a) {

        if (a.getAcademicYear() == null || a.getAcademicYear().isBlank()) {
            throw new IllegalArgumentException("academicYear is required (ex: 2025-2026)");
        }
        if (a.getSemester() == null) {
            throw new IllegalArgumentException("semester is required (S1 or S2)");
        }

        Teacher t = teacherRepo.findById(a.getTeacher().getId())
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found: " + a.getTeacher().getId()));
        Subject s = subjectRepo.findById(a.getSubject().getId())
                .orElseThrow(() -> new IllegalArgumentException("Subject not found: " + a.getSubject().getId()));
        ClassGroup cg = classGroupRepo.findById(a.getClassGroup().getId())
                .orElseThrow(() -> new IllegalArgumentException("ClassGroup not found: " + a.getClassGroup().getId()));

        a.setTeacher(t);
        a.setSubject(s);
        a.setClassGroup(cg);

        return repo.save(a);
    }

    public TeachingAssignment update(Long id, TeachingAssignment patch) {
        TeachingAssignment existing = getById(id);

        existing.setAcademicYear(patch.getAcademicYear());
        existing.setSemester(patch.getSemester());
        existing.setTeacher(patch.getTeacher());
        existing.setSubject(patch.getSubject());
        existing.setClassGroup(patch.getClassGroup());

        // Réutilise la logique de validation + résolution des relations
        return create(existing);
    }


    public List<TeachingAssignment> getAll() { return repo.findAll(); }

    public TeachingAssignment getById(Long id) {
        return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Assignment not found: " + id));
    }

    public List<TeachingAssignment> getByClassGroup(Long classGroupId) {
        return repo.findByClassGroup_Id(classGroupId);
    }

    public void delete(Long id) { repo.delete(getById(id)); }
    
    public List<TeachingAssignment> getByClassGroupYear(Long classGroupId, String year) {
        return repo.findByClassGroup_IdAndAcademicYear(classGroupId, year);
    }

    public List<TeachingAssignment> getByClassGroupYearSemester(Long classGroupId, String year, Semester semester) {
        return repo.findByClassGroup_IdAndAcademicYearAndSemester(classGroupId, year, semester);
    }

}

