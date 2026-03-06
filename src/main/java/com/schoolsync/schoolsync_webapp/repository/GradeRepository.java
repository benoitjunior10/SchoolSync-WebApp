/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.schoolsync.schoolsync_webapp.repository;

/**
 *
 * @author AQUARIAN
 */

import com.schoolsync.schoolsync_webapp.model.Grade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GradeRepository extends JpaRepository<Grade, Long> {
    
    Optional<Grade> findByStudent_IdAndEvaluation_Id(Long studentId, Long evaluationId);
    
    List<Grade> findByEvaluation_Id(Long evaluationId);
    
    List<Grade> findByStudent_Id(Long studentId);

    // --- NOUVELLE MÉTHODE OPTIMISÉE ---
    // Récupère toutes les notes d'un élève pour une LISTE d'évaluations
    List<Grade> findByStudent_IdAndEvaluation_IdIn(Long studentId, List<Long> evaluationIds);
}

