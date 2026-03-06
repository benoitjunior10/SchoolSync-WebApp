package com.schoolsync.schoolsync_webapp;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Exposes commonly used request-derived attributes to all Thymeleaf templates.
 *
 * Thymeleaf 3.1 (Spring 6) no longer exposes #request/#session by default.
 * Instead of using expression utility objects, we expose the current request
 * path explicitly as a model attribute.
 */
@ControllerAdvice
public class GlobalModelAttributes {

    @ModelAttribute("currentPath")
    public String currentPath(HttpServletRequest request) {
        // getRequestURI() is stable for menu highlighting and excludes scheme/host.
        return request.getRequestURI();
    }
}
