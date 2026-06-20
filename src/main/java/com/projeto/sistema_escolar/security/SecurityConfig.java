package com.projeto.sistema_escolar.security;

import org.springframework.http.HttpMethod;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())

            // JWT é stateless — sem sessão HTTP
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .authorizeHttpRequests(auth -> auth

                // Recursos estáticos — TODOS públicos (incluindo favicon)
                .requestMatchers(
                    "/css/**", "/js/**", "/img/**", "/favicon.ico",
                    "/admin/**", "/professor/**", "/aluno/**", "/coordenacao/**"
                ).permitAll()

                // Páginas HTML públicas
                .requestMatchers(
                    "/", "/index.html",
                    "/login.html", "/cadastro.html", "/trocar-senha.html", "/configuracoes.html"
                ).permitAll()

                // APIs públicas
                .requestMatchers(
                    "/api/login",
                    "/api/logout",
                    "/api/cadastro",
                    "/api/trocar-senha"
                ).permitAll()

                // Swagger público
                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**"
                ).permitAll()

                // APIs protegidas por perfil
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/professor/**").hasRole("PROFESSOR")
                .requestMatchers("/api/aluno/**").hasRole("ALUNO")
                .requestMatchers("/api/coordenador/**").hasRole("COORDENADOR")
                .requestMatchers("/api/coordenacao/**").hasRole("COORDENADOR")

                // regras de provas
                .requestMatchers(HttpMethod.POST,   "/api/provas/**").hasAnyRole("ADMIN", "PROFESSOR")
                .requestMatchers(HttpMethod.PUT,    "/api/provas/**").hasAnyRole("ADMIN", "PROFESSOR")
                .requestMatchers(HttpMethod.DELETE, "/api/provas/**").hasAnyRole("ADMIN", "PROFESSOR")
                .requestMatchers(HttpMethod.GET,    "/api/provas/**").hasAnyRole("ADMIN", "PROFESSOR", "COORDENADOR")

                // Qualquer outra API precisa de autenticação
                .anyRequest().authenticated()
            )

            // Erros retornam JSON para APIs, sem redirecionar para HTML
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, e) -> {
                    // Se for requisição de página HTML, redireciona para login
                    String accept = request.getHeader("Accept");
                    if (accept != null && accept.contains("text/html")) {
                        response.sendRedirect("/login.html");
                    } else {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter()
                            .write("{\"erro\":\"Não autenticado\",\"redirect\":\"/login.html\"}");
                    }
                })
                .accessDeniedHandler((request, response, e) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"erro\":\"Acesso negado\"}");
                })
            )

            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .httpBasic(h -> h.disable())
            .formLogin(f -> f.disable());

        return http.build();
    }
}