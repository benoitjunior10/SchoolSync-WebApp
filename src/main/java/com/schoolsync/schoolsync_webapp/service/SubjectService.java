/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.schoolsync.schoolsync_webapp.service;

/**
 *
 * @author AQUARIAN
 */
import com.schoolsync.schoolsync_webapp.model.Subject;
import com.schoolsync.schoolsync_webapp.repository.SubjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubjectService {

    private final SubjectRepository repo;

    public SubjectService(SubjectRepository repo) { this.repo = repo; }

    public Subject create(Subject s) { return repo.save(s); }

    public List<Subject> getAll() { return repo.findAll(); }

    public Subject getById(Long id) {
        return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Subject not found: " + id));
    }

    public Subject update(Long id, Subject s) {
        Subject ex = getById(id);
        ex.setCode(s.getCode());
        ex.setName(s.getName());
        ex.setCoefficient(s.getCoefficient());
        return repo.save(ex);
    }

    public void delete(Long id) { repo.delete(getById(id)); }
}

