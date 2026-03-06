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

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    public record SubjectReport(
            Long assignmentId,
            String subjectCode,
            String subjectName,
            Double coefficient,
            Double average
    ) {}

    public record SemesterReport(
            Semester semester,
            List<SubjectReport> subjects,
            Double generalAverage
    ) {}

    public record StudentReport(
            Long studentId,
            String matricule,
            String fullName,
            String classGroup,
            String academicYear,
            SemesterReport s1,
            SemesterReport s2,
            Double annualAverage
    ) {}

    private final StudentRepository studentRepo;
    private final TeachingAssignmentRepository assignmentRepo;
    private final EvaluationRepository evalRepo;
    private final GradeRepository gradeRepo;

    public ReportService(StudentRepository studentRepo,
                         TeachingAssignmentRepository assignmentRepo,
                         EvaluationRepository evalRepo,
                         GradeRepository gradeRepo) {
        this.studentRepo = studentRepo;
        this.assignmentRepo = assignmentRepo;
        this.evalRepo = evalRepo;
        this.gradeRepo = gradeRepo;
    }

    // Sans param -> S1 + S2 + annuel
    public StudentReport getStudentReport(Long studentId) {
        return getStudentReport(studentId, null, null);
    }

    public StudentReport getStudentReport(Long studentId, String year, Semester semester) {

        Student st = studentRepo.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        Long classGroupId = st.getClassGroup().getId();
        String academicYear = (year != null && !year.isBlank()) ? year : st.getClassGroup().getAcademicYear();

        SemesterReport s1 = null;
        SemesterReport s2 = null;

        if (semester == null) {
            s1 = buildSemesterReport(studentId, classGroupId, academicYear, Semester.S1);
            s2 = buildSemesterReport(studentId, classGroupId, academicYear, Semester.S2);
        } else if (semester == Semester.S1) {
            s1 = buildSemesterReport(studentId, classGroupId, academicYear, Semester.S1);
        } else {
            s2 = buildSemesterReport(studentId, classGroupId, academicYear, Semester.S2);
        }

        Double annualAvg = computeAnnualAverage(s1, s2);

        return new StudentReport(
                st.getId(),
                st.getMatricule(),
                st.getFirstName() + " " + st.getLastName(),
                st.getClassGroup().getName(),
                academicYear,
                s1,
                s2,
                annualAvg
        );
    }

    /**
     * Version optimisée (Batch Fetching)
     * Réduit le nombre de requêtes SQL à 3 par semestre, quel que soit le nombre de matières.
     */
    private SemesterReport buildSemesterReport(Long studentId, Long classGroupId, String year, Semester semester) {

        // 1. Récupérer toutes les matières (Assignments) du semestre
        List<TeachingAssignment> assignments =
                assignmentRepo.findByClassGroup_IdAndAcademicYearAndSemester(classGroupId, year, semester);

        if (assignments.isEmpty()) {
            return new SemesterReport(semester, List.of(), null);
        }

        // 2. Extraire les IDs des matières
        List<Long> assignmentIds = assignments.stream().map(TeachingAssignment::getId).toList();

        // 3. Récupérer TOUTES les évaluations liées à ces matières en UNE SEULE requête
        List<Evaluation> allEvals = evalRepo.findByAssignment_IdInAndAcademicYearAndSemester(assignmentIds, year, semester);

        // 4. Grouper les évaluations par matière (Map<AssignmentID, List<Evaluation>>)
        Map<Long, List<Evaluation>> evalsByAssignment = allEvals.stream()
                .collect(Collectors.groupingBy(e -> e.getAssignment().getId()));

        // 5. Récupérer TOUTES les notes de l'étudiant pour ces évaluations en UNE SEULE requête
        List<Long> allEvalIds = allEvals.stream().map(Evaluation::getId).toList();
        
        List<Grade> studentGrades;
        if (allEvalIds.isEmpty()) {
            studentGrades = new ArrayList<>();
        } else {
            studentGrades = gradeRepo.findByStudent_IdAndEvaluation_IdIn(studentId, allEvalIds);
        }

        // 6. Indexer les notes par ID d'évaluation pour accès rapide (Map<EvaluationID, Grade>)
        Map<Long, Grade> gradesByEvalId = studentGrades.stream()
                .collect(Collectors.toMap(g -> g.getEvaluation().getId(), g -> g, (existing, replacement) -> existing));

        // 7. Construire le rapport en mémoire
        List<SubjectReport> subjectReports = new ArrayList<>();
        double sumWeighted = 0.0;
        double sumCoeff = 0.0;

        for (TeachingAssignment a : assignments) {
            // Récupérer la liste des évals depuis la Map (pas de requête SQL ici)
            List<Evaluation> evals = evalsByAssignment.getOrDefault(a.getId(), List.of());

            // Calculer la moyenne
            Double subjectAverage = computeSubjectAverageInMemory(evals, gradesByEvalId);
            
            double coeff = a.getSubject().getCoefficient() != null ? a.getSubject().getCoefficient() : 1.0;

            subjectReports.add(new SubjectReport(
                    a.getId(),
                    a.getSubject().getCode(),
                    a.getSubject().getName(),
                    coeff,
                    subjectAverage
            ));

            if (subjectAverage != null) {
                sumWeighted += subjectAverage * coeff;
                sumCoeff += coeff;
            }
        }

        Double generalAvg = (sumCoeff > 0) ? (sumWeighted / sumCoeff) : null;
        return new SemesterReport(semester, subjectReports, generalAvg);
    }

    private Double computeSubjectAverageInMemory(List<Evaluation> evals, Map<Long, Grade> gradesMap) {
        double totalWeight = 0.0;
        double sum = 0.0;
        boolean hasAtLeastOneGrade = false;

        for (Evaluation ev : evals) {
            // Recherche dans la Map (O(1)) au lieu de la BDD
            Grade g = gradesMap.get(ev.getId());
            
            if (g == null) continue; // Pas de note pour cette évaluation

            hasAtLeastOneGrade = true;
            double score = g.getScore();
            double max = ev.getMaxScore();
            double percent = (max > 0) ? (score / max) * 100.0 : 0.0;

            double w = ev.getWeight();
            sum += percent * w;
            totalWeight += w;
        }

        if (!hasAtLeastOneGrade || totalWeight == 0.0) return null;
        return sum / totalWeight;
    }

    // Le calcul annuel reste le même (il travaille sur les résultats déjà calculés)
    private Double computeAnnualAverage(SemesterReport s1, SemesterReport s2) {
        double sumWeighted = 0.0;
        double sumCoeff = 0.0;

        if (s1 != null && s1.subjects() != null) {
            for (SubjectReport sr : s1.subjects()) {
                if (sr.average() != null) {
                    sumWeighted += sr.average() * sr.coefficient();
                    sumCoeff += sr.coefficient();
                }
            }
        }
        if (s2 != null && s2.subjects() != null) {
            for (SubjectReport sr : s2.subjects()) {
                if (sr.average() != null) {
                    sumWeighted += sr.average() * sr.coefficient();
                    sumCoeff += sr.coefficient();
                }
            }
        }

        return (sumCoeff > 0) ? (sumWeighted / sumCoeff) : null;
    }
}

