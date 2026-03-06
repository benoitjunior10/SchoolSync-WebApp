package com.schoolsync.schoolsync_webapp.repository;

import com.schoolsync.schoolsync_webapp.model.Evaluation;
import com.schoolsync.schoolsync_webapp.model.Semester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {

    List<Evaluation> findByAssignment_Id(Long assignmentId);

    List<Evaluation> findByAssignment_IdIn(List<Long> assignmentIds);

    List<Evaluation> findByAssignment_IdAndAcademicYearAndSemester(Long assignmentId, String academicYear, Semester semester);

    // Optimisation pour le bulletin
    List<Evaluation> findByAssignment_IdInAndAcademicYearAndSemester(List<Long> assignmentIds, String academicYear, Semester semester);

    @Query("select coalesce(sum(e.weight), 0) from Evaluation e " +
           "where e.assignment.id = :assignmentId and e.academicYear = :year and e.semester = :semester")
    double sumWeights(@Param("assignmentId") Long assignmentId,
                      @Param("year") String academicYear,
                      @Param("semester") Semester semester);

    @Query("select coalesce(sum(e.weight), 0) from Evaluation e " +
           "where e.assignment.id = :assignmentId and e.academicYear = :year and e.semester = :semester and e.id <> :excludeId")
    double sumWeightsExcluding(@Param("assignmentId") Long assignmentId,
                               @Param("year") String academicYear,
                               @Param("semester") Semester semester,
                               @Param("excludeId") Long excludeId);
}
