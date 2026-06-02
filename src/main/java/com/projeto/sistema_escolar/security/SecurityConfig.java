package com.projeto.sistema_escolar.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // 📌 Rotas públicas (não precisam de autenticação)
                .requestMatchers(
                    "/api/login",
                    "/api/logout",
                    "/api/cadastro",
                    "/css/**",
                    "/js/**",
                    "/",
                    "/index.html",
                    "/login.html",
                    "/cadastro.html",
                    "/img/**"
                ).permitAll()
                
                // 📌 Rotas protegidas por perfil
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/professor/**").hasRole("PROFESSOR")
                .requestMatchers("/api/aluno/**").hasRole("ALUNO")
                .requestMatchers("/api/coordenador/**").hasRole("COORDENADOR")
                
                // 📌 Rotas que qualquer usuário logado pode acessar
                .requestMatchers("/api/usuarios/**").authenticated()
                .requestMatchers("/api/perfis/**").authenticated()
                .requestMatchers("/api/escolas/**").authenticated()
                .requestMatchers("/api/turmas/**").authenticated()
                .requestMatchers("/api/disciplinas/**").authenticated()
                .requestMatchers("/api/series/**").authenticated()
                .requestMatchers("/api/questoes/**").authenticated()
                .requestMatchers("/api/alternativas/**").authenticated()
                .requestMatchers("/api/provas/**").authenticated()
                .requestMatchers("/api/respostas/**").authenticated()
                
                // Qualquer outra requisição precisa estar autenticada
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .sessionFixation().migrateSession()
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
            )
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(form -> form.disable());
        
        return http.build();
    }
}