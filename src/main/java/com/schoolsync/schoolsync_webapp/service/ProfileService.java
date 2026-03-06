/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.schoolsync.schoolsync_webapp.service;

/**
 *
 * @author AQUARIAN
 */

import com.schoolsync.schoolsync_webapp.dto.ProfileDtos;
import com.schoolsync.schoolsync_webapp.model.AppUser;
import com.schoolsync.schoolsync_webapp.model.Role;
import com.schoolsync.schoolsync_webapp.repository.StudentRepository;
import com.schoolsync.schoolsync_webapp.repository.TeacherRepository;
import com.schoolsync.schoolsync_webapp.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

    private final UserRepository userRepo;
    private final StudentRepository studentRepo;
    private final TeacherRepository teacherRepo;

    public ProfileService(UserRepository userRepo, StudentRepository studentRepo, TeacherRepository teacherRepo) {
        this.userRepo = userRepo;
        this.studentRepo = studentRepo;
        this.teacherRepo = teacherRepo;
    }

    public Object myProfile(UserDetails principal) {

        String username = principal.getUsername();

        AppUser user = userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        var basic = new ProfileDtos.BasicUserProfile(
                user.getId(), user.getUsername(), user.getRole(), user.isEnabled()
        );

        if (user.getRole() == Role.STUDENT) {
                        var st = studentRepo.findByUser_Id(user.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Student profile not linked to this account"));

            return new ProfileDtos.StudentProfile(
                    basic,
                    st.getId(),
                    st.getMatricule(),
                    st.getFirstName(),
                    st.getLastName(),
                    st.getEmail(),
                    st.getPhone(),
                    st.getBirthDate() != null ? st.getBirthDate().toString() : null,
                    st.getClassGroup() != null ? st.getClassGroup().getId() : null,
                    st.getClassGroup() != null ? st.getClassGroup().getName() : null,
                    st.getClassGroup() != null ? st.getClassGroup().getAcademicYear() : null
            );
        }

        if (user.getRole() == Role.TEACHER) {
                        var t = teacherRepo.findByUser_Id(user.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Teacher profile not linked to this account"));

            return new ProfileDtos.TeacherProfile(
                    basic,
                    t.getId(),
                    t.getMatricule(),
                    t.getFirstName(),
                    t.getLastName(),
                    t.getEmail(),
                    t.getPhone()
            );
        }

        // ADMIN
        return new ProfileDtos.AdminProfile(basic);
    }
}


