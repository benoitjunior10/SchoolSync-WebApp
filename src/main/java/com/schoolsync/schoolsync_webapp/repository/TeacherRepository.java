/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.schoolsync.schoolsync_webapp.repository;

/**
 *
 * @author AQUARIAN
 */
import com.schoolsync.schoolsync_webapp.model.Teacher;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    Optional<Teacher> findByMatricule(String matricule);

    Optional<Teacher> findByUser_Id(Long userId);
}