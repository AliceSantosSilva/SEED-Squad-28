package com.projeto.sistema_escolar.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth

                // Recursos estáticos públicos
                .requestMatchers(
                    "/css/**", "/js/**", "/img/**",
                    "/favicon.ico"
                ).permitAll()

                // Páginas públicas de autenticação
                .requestMatchers(
                    "/", "/index.html",
                    "/login.html", "/cadastro.html",
                    "/trocar-senha.html"
                ).permitAll()

                // APIs públicas
                .requestMatchers(
                    "/api/login",
                    "/api/logout",
                    "/api/cadastro",
                    "/api/trocar-senha"   // ← ESSENCIAL: permite trocar senha no primeiro acesso
                ).permitAll()

                // Páginas protegidas por perfil
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/professor/**").hasRole("PROFESSOR")
                .requestMatchers("/aluno/**").hasRole("ALUNO")
                .requestMatchers("/coordenacao/**").hasRole("COORDENADOR")

                // APIs protegidas por perfil
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/professor/**").hasRole("PROFESSOR")
                .requestMatchers("/api/aluno/**").hasRole("ALUNO")
                .requestMatchers("/api/coordenador/**").hasRole("COORDENADOR")
                .requestMatchers("/api/coordenacao/**").hasRole("COORDENADOR")

                // Aplicação de prova — qualquer autenticado
                .requestMatchers("/api/aplicacao/**").authenticated()

                // APIs gerais autenticadas
                .requestMatchers(
                    "/api/usuario/logado",
                    "/api/usuarios/**",
                    "/api/perfis/**",
                    "/api/escolas/**",
                    "/api/turmas/**",
                    "/api/disciplinas/**",
                    "/api/series/**",
                    "/api/questoes/**",
                    "/api/alternativas/**",
                    "/api/provas/**",
                    "/api/respostas/**",
                    "/api/alunos/**",
                    "/api/professores/**",
                    "/api/coordenadores/**"
                ).authenticated()

                .anyRequest().authenticated()
            )

            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .sessionFixation().migrateSession()
                .maximumSessions(1)
                    .maxSessionsPreventsLogin(false)
            )

            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    String uri = request.getRequestURI();
                    if (uri.startsWith("/api/")) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter()
                            .write("{\"erro\": \"Não autenticado\", \"redirect\": \"/login.html\"}");
                    } else {
                        response.sendRedirect("/login.html");
                    }
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    String uri = request.getRequestURI();
                    if (uri.startsWith("/api/")) {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"erro\": \"Acesso negado\"}");
                    } else {
                        response.sendRedirect("/login.html?erro=acesso-negado");
                    }
                })
            )

            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(form -> form.disable());

        return http.build();
    }
}