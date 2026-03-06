/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.schoolsync.schoolsync_webapp.service;

/**
 *
 * @author AQUARIAN
 */

import com.schoolsync.schoolsync_webapp.model.ClassGroup;
import com.schoolsync.schoolsync_webapp.repository.ClassGroupRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClassGroupService {

    private final ClassGroupRepository repo;

    public ClassGroupService(ClassGroupRepository repo) { this.repo = repo; }

    public ClassGroup create(ClassGroup cg) { return repo.save(cg); }

    public List<ClassGroup> getAll() { return repo.findAll(); }

    public ClassGroup getById(Long id) {
        return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("ClassGroup not found: " + id));
    }

    public ClassGroup update(Long id, ClassGroup cg) {
        ClassGroup ex = getById(id);
        ex.setName(cg.getName());
        ex.setLevel(cg.getLevel());
        ex.setProgram(cg.getProgram());
        ex.setAcademicYear(cg.getAcademicYear());
        return repo.save(ex);
    }

    public void delete(Long id) { repo.delete(getById(id)); }
}
