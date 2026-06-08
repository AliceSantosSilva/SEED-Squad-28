package com.projeto.sistema_escolar.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Utilitário JWT — gera e valida tokens.
 * O segredo vem do application.properties (jwt.secret).
 * Expiração padrão: 8 horas.
 */
@Component
public class JwtUtil {

    private static final long EXPIRACAO_MS = 8 * 60 * 60 * 1000L;

    private final SecretKey chave;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        this.chave = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /** Gera um token com email, perfil e id do usuário. */
    public String gerarToken(String email, String perfil, Integer usuarioId) {
        return Jwts.builder()
                .subject(email)
                .claim("perfil",    perfil)
                .claim("usuarioId", usuarioId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRACAO_MS))
                .signWith(chave)
                .compact();
    }

    public String extrairEmail(String token) {
        return extrairClaims(token).getSubject();
    }

    public String extrairPerfil(String token) {
        return extrairClaims(token).get("perfil", String.class);
    }

    public Integer extrairUsuarioId(String token) {
        return extrairClaims(token).get("usuarioId", Integer.class);
    }

    public boolean isTokenValido(String token) {
        try {
            extrairClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims extrairClaims(String token) {
        return Jwts.parser()
                .verifyWith(chave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}