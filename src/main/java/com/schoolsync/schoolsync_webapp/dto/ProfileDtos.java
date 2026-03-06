/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.schoolsync.schoolsync_webapp.dto;

/**
 *
 * @author AQUARIAN
 */

import com.schoolsync.schoolsync_webapp.model.Role;
import com.schoolsync.schoolsync_webapp.model.Semester;

public class ProfileDtos {

    public record BasicUserProfile(
            Long userId,
            String username,
            Role role,
            boolean enabled
    ) {}

    public record StudentProfile(
            BasicUserProfile user,
            Long studentId,
            String matricule,
            String firstName,
            String lastName,
            String email,
            String phone,
            String birthDate,
            Long classGroupId,
            String classGroupName,
            String academicYear
    ) {}

    public record TeacherProfile(
            BasicUserProfile user,
            Long teacherId,
            String matricule,
            String firstName,
            String lastName,
            String email,
            String phone
    ) {}

    public record AdminProfile(
            BasicUserProfile user
    ) {}
}

