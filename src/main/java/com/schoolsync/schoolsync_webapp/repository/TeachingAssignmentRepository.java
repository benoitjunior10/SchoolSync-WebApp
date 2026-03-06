package com.schoolsync.schoolsync_webapp.repository;

import com.schoolsync.schoolsync_webapp.model.Semester;
import com.schoolsync.schoolsync_webapp.model.TeachingAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeachingAssignmentRepository extends JpaRepository<TeachingAssignment, Long> {

    List<TeachingAssignment> findByClassGroup_Id(Long classGroupId);

    List<TeachingAssignment> findByClassGroup_IdAndAcademicYear(Long classGroupId, String academicYear);

    List<TeachingAssignment> findByClassGroup_IdAndAcademicYearAndSemester(Long classGroupId, String academicYear, Semester semester);

    List<TeachingAssignment> findByTeacher_Id(Long teacherId);
}
