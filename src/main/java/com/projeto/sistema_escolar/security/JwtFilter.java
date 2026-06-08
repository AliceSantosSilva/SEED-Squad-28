package com.projeto.sistema_escolar.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro JWT — roda uma vez por requisição.
 * Lê o header "Authorization: Bearer <token>",
 * valida e popula o contexto de segurança do Spring.
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // Se não tem token, deixa passar (o SecurityConfig decide se a rota é pública)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.isTokenValido(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Token válido — monta a autenticação e coloca no contexto
        String email   = jwtUtil.extrairEmail(token);
        String perfil  = jwtUtil.extrairPerfil(token);
        String role    = "ROLE_" + perfil.toUpperCase();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        List.of(new SimpleGrantedAuthority(role))
                );

        // Armazena o usuarioId como detalhe para controllers usarem sem ir ao banco
        auth.setDetails(jwtUtil.extrairUsuarioId(token));

        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }
}