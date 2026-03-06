package com.schoolsync.schoolsync_webapp.controller;

import com.schoolsync.schoolsync_webapp.model.*;
import com.schoolsync.schoolsync_webapp.repository.StudentRepository;
import com.schoolsync.schoolsync_webapp.repository.UserRepository;
import com.schoolsync.schoolsync_webapp.service.ReportService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/report")
public class ReportController {

    private final ReportService reportService;
    private final UserRepository userRepo;
    private final StudentRepository studentRepo;

    public ReportController(ReportService reportService, UserRepository userRepo, StudentRepository studentRepo) {
        this.reportService = reportService;
        this.userRepo = userRepo;
        this.studentRepo = studentRepo;
    }

    @GetMapping("/me")
    public String myReport(@AuthenticationPrincipal UserDetails user,
                           @RequestParam(required = false) String year,
                           @RequestParam(required = false) Semester semester,
                           Model model) {
        String username = user.getUsername();
        AppUser u = userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        Student st = studentRepo.findByUser_Id(u.getId())
                .orElseThrow(() -> new IllegalArgumentException("Profil étudiant non lié à ce compte."));

        ReportService.StudentReport report;
        if (year == null && semester == null) report = reportService.getStudentReport(st.getId());
        else report = reportService.getStudentReport(st.getId(), year, semester);

        model.addAttribute("report", report);
        model.addAttribute("year", year);
        model.addAttribute("semester", semester);
        return "students/my-report";
    }
}
