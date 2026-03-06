/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.schoolsync.schoolsync_webapp.service;

/**
 *
 * @author AQUARIAN
 */
import com.schoolsync.schoolsync_webapp.model.Teacher;
import com.schoolsync.schoolsync_webapp.repository.TeacherRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeacherService {

    private final TeacherRepository repo;

    public TeacherService(TeacherRepository repo) { this.repo = repo; }

    public Teacher create(Teacher t) { return repo.save(t); }

    public List<Teacher> getAll() { return repo.findAll(); }

    public Teacher getById(Long id) {
        return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Teacher not found: " + id));
    }

    public Teacher update(Long id, Teacher t) {
        Teacher ex = getById(id);
        ex.setMatricule(t.getMatricule());
        ex.setFirstName(t.getFirstName());
        ex.setLastName(t.getLastName());
        ex.setEmail(t.getEmail());
        ex.setPhone(t.getPhone());
        return repo.save(ex);
    }

    public void delete(Long id) { repo.delete(getById(id)); }
}

