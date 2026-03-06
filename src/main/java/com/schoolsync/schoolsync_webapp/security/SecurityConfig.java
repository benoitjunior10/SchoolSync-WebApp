package com.schoolsync.schoolsync_webapp.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // CSRF activé par défaut (recommandé pour applis web)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/login",
                                "/signup/**",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/webjars/**",
                                "/error"
                        ).permitAll()

                        // Étudiant: accès à son bulletin
                        .requestMatchers("/report/me/**").hasRole("STUDENT")

                        // Admin
                        .requestMatchers(
                                "/students/**",
                                "/teachers/**",
                                "/subjects/**",
                                "/class-groups/**"
                        ).hasRole("ADMIN")

                        // Affectations: création/suppression réservées à l'admin
                        .requestMatchers(HttpMethod.GET, "/assignments/new").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/assignments").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/assignments/*/delete").hasRole("ADMIN")

                        // Admin + Teacher
                        .requestMatchers(
                                "/assignments/**",
                                "/evaluations/**",
                                "/grades/**"
                        ).hasAnyRole("ADMIN", "TEACHER")

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
