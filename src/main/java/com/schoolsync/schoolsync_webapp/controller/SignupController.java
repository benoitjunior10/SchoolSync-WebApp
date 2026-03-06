package com.schoolsync.schoolsync_webapp;

import com.schoolsync.schoolsync_webapp.model.*;
import com.schoolsync.schoolsync_webapp.repository.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/signup")
public class SignupController {

    private final UserRepository userRepo;
    private final StudentRepository studentRepo;
    private final TeacherRepository teacherRepo;
    private final PasswordEncoder encoder;

    public SignupController(UserRepository userRepo, StudentRepository studentRepo, TeacherRepository teacherRepo, PasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.studentRepo = studentRepo;
        this.teacherRepo = teacherRepo;
        this.encoder = encoder;
    }

    public static class SignupForm {
        @NotBlank
        private String matricule;
        @NotBlank
        @Size(min = 6, message = "Mot de passe: 6 caractères minimum")
        private String password;

        public String getMatricule() { return matricule; }
        public void setMatricule(String matricule) { this.matricule = matricule; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    @GetMapping("/student")
    public String studentForm(Model model) {
        model.addAttribute("form", new SignupForm());
        return "auth/signup-student";
    }

    @PostMapping("/student")
    public String studentSubmit(@Valid @ModelAttribute("form") SignupForm form,
                                BindingResult br,
                                RedirectAttributes ra) {
        if (br.hasErrors()) return "auth/signup-student";

        Student st = studentRepo.findByMatricule(form.getMatricule())
                .orElseThrow(() -> new IllegalArgumentException("Étudiant introuvable pour le matricule: " + form.getMatricule()));

        if (st.getUser() != null) {
            br.reject("exists", "Ce profil étudiant a déjà un compte.");
            return "auth/signup-student";
        }

        String username = form.getMatricule();
        if (userRepo.existsByUsername(username)) {
            br.reject("exists", "Ce matricule a déjà un compte.");
            return "auth/signup-student";
        }

        AppUser u = AppUser.builder()
                .username(username)
                .password(encoder.encode(form.getPassword()))
                .role(Role.STUDENT)
                .enabled(true)
                .build();

        u = userRepo.save(u);
        st.setUser(u);
        studentRepo.save(st);

        ra.addFlashAttribute("success", "Compte étudiant créé. Vous pouvez maintenant vous connecter.");
        return "redirect:/login";
    }

    @GetMapping("/teacher")
    public String teacherForm(Model model) {
        model.addAttribute("form", new SignupForm());
        return "auth/signup-teacher";
    }

    @PostMapping("/teacher")
    public String teacherSubmit(@Valid @ModelAttribute("form") SignupForm form,
                                BindingResult br,
                                RedirectAttributes ra) {
        if (br.hasErrors()) return "auth/signup-teacher";

        Teacher t = teacherRepo.findByMatricule(form.getMatricule())
                .orElseThrow(() -> new IllegalArgumentException("Professeur introuvable pour le matricule: " + form.getMatricule()));

        if (t.getUser() != null) {
            br.reject("exists", "Ce profil professeur a déjà un compte.");
            return "auth/signup-teacher";
        }

        String username = form.getMatricule();
        if (userRepo.existsByUsername(username)) {
            br.reject("exists", "Ce matricule a déjà un compte.");
            return "auth/signup-teacher";
        }

        AppUser u = AppUser.builder()
                .username(username)
                .password(encoder.encode(form.getPassword()))
                .role(Role.TEACHER)
                .enabled(true)
                .build();

        u = userRepo.save(u);
        t.setUser(u);
        teacherRepo.save(t);

        ra.addFlashAttribute("success", "Compte professeur créé. Vous pouvez maintenant vous connecter.");
        return "redirect:/login";
    }
}
