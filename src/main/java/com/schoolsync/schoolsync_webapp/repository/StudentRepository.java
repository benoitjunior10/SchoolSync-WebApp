package com.schoolsync.schoolsync_webapp.repository;

import com.schoolsync.schoolsync_webapp.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByMatricule(String matricule);
    Optional<Student> findByEmail(String email);

    Optional<Student> findByUser_Id(Long userId);

    List<Student> findByClassGroup_Id(Long classGroupId);
}
