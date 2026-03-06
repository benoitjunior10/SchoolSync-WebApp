package com.schoolsync.schoolsync_webapp.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalErrorHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req, Model model) {
        model.addAttribute("path", req.getRequestURI());
        model.addAttribute("message", ex.getMessage());
        return "error/400";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneric(Exception ex, HttpServletRequest req, Model model) {
        model.addAttribute("path", req.getRequestURI());
        model.addAttribute("message", ex.getMessage());
        return "error/500";
    }
}
