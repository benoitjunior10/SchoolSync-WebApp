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
import com.schoolsync.schoolsync_webapp.model.Student;
import com.schoolsync.schoolsync_webapp.repository.ClassGroupRepository;
import com.schoolsync.schoolsync_webapp.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService {

    private final StudentRepository studentRepo;
    private final ClassGroupRepository classGroupRepo;

    public StudentService(StudentRepository studentRepo, ClassGroupRepository classGroupRepo) {
        this.studentRepo = studentRepo;
        this.classGroupRepo = classGroupRepo;
    }

    public Student create(Student s) {
        Long cgId = s.getClassGroup().getId();
        ClassGroup cg = classGroupRepo.findById(cgId)
                .orElseThrow(() -> new IllegalArgumentException("ClassGroup not found: " + cgId));
        s.setClassGroup(cg);
        return studentRepo.save(s);
    }

    public List<Student> getAll() { return studentRepo.findAll(); }

    public Student getById(Long id) {
        return studentRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + id));
    }

    public Student update(Long id, Student s) {
        Student existing = getById(id);

        existing.setMatricule(s.getMatricule());
        existing.setFirstName(s.getFirstName());
        existing.setLastName(s.getLastName());
        existing.setEmail(s.getEmail());
        existing.setPhone(s.getPhone());
        existing.setBirthDate(s.getBirthDate());

        if (s.getClassGroup() != null && s.getClassGroup().getId() != null) {
            ClassGroup cg = classGroupRepo.findById(s.getClassGroup().getId())
                    .orElseThrow(() -> new IllegalArgumentException("ClassGroup not found: " + s.getClassGroup().getId()));
            existing.setClassGroup(cg);
        }

        return studentRepo.save(existing);
    }

    public void delete(Long id) {
        studentRepo.delete(getById(id));
    }
}
