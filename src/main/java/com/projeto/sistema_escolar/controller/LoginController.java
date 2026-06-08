package com.projeto.sistema_escolar.controller;

import com.projeto.sistema_escolar.dto.LoginRequestDTO;
import com.projeto.sistema_escolar.dto.LoginResponseDTO;
import com.projeto.sistema_escolar.dto.TrocarSenhaRequestDTO;
import com.projeto.sistema_escolar.model.Usuario;
import com.projeto.sistema_escolar.security.JwtUtil;
import com.projeto.sistema_escolar.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@Tag(name = "Autenticação", description = "Login, logout e troca de senha")
public class LoginController {

    private final UsuarioService  usuarioService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil         jwtUtil;

    public LoginController(UsuarioService usuarioService,
                           PasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil) {
        this.usuarioService  = usuarioService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil         = jwtUtil;
    }

    // ── LOGIN ─────────────────────────────────────────────────────────────────

    @Operation(summary = "Realiza login e retorna JWT")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO request) {

        Optional<Usuario> usuarioOpt = usuarioService.buscarPorEmail(request.getEmail());

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new LoginResponseDTO("E-mail ou senha incorretos"));
        }

        Usuario usuario = usuarioOpt.get();

        if (!passwordEncoder.matches(request.getSenha(), usuario.getSenha())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new LoginResponseDTO("E-mail ou senha incorretos"));
        }

        if (usuario.getAtivo() == null || !usuario.getAtivo()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new LoginResponseDTO("Conta desativada. Entre em contato com o administrador."));
        }

        // Primeiro acesso — precisa trocar a senha antes de receber o token
        if (Boolean.TRUE.equals(usuario.getSenhaExpirada())) {
            // Gera um token temporário de escopo reduzido só para trocar senha
            String tokenTemp = jwtUtil.gerarToken(
                usuario.getEmail(), "TROCAR_SENHA", usuario.getId()
            );
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new LoginResponseDTO(
                    usuario.getNome(),
                    usuario.getPerfil() != null ? usuario.getPerfil().getNome() : "USER",
                    "Você precisa trocar sua senha no primeiro acesso.",
                    true,
                    tokenTemp
                ));
        }

        String nomePerfil = usuario.getPerfil() != null
            ? usuario.getPerfil().getNome() : "USER";

        String token = jwtUtil.gerarToken(usuario.getEmail(), nomePerfil, usuario.getId());

        return ResponseEntity.ok(new LoginResponseDTO(
            usuario.getNome(),
            nomePerfil,
            "Login realizado com sucesso",
            true,
            false,
            resolverDestino(nomePerfil),
            token
        ));
    }

    // ── LOGOUT ────────────────────────────────────────────────────────────────
    // Com JWT stateless o logout é feito no frontend descartando o token.
    // Este endpoint existe para manter compatibilidade com o frontend atual.

    @Operation(summary = "Logout (descarta sessão no frontend)")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.ok().build();
    }

    // ── TROCAR SENHA ──────────────────────────────────────────────────────────

    @Operation(summary = "Troca senha (primeiro acesso ou autenticado)")
    @PostMapping("/trocar-senha")
    public ResponseEntity<?> trocarSenha(@RequestBody TrocarSenhaRequestDTO request,
                                          HttpServletRequest httpRequest) {

        // Lê o usuarioId do token JWT (colocado pelo JwtFilter em auth.getDetails())
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("erro", "Token não fornecido."));
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.isTokenValido(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("erro", "Token inválido ou expirado."));
        }

        Integer usuarioId = jwtUtil.extrairUsuarioId(token);
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorId(usuarioId);

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("erro", "Usuário não encontrado."));
        }

        Usuario usuario = usuarioOpt.get();

        if (!passwordEncoder.matches(request.getSenhaAtual(), usuario.getSenha())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("erro", "Senha atual incorreta."));
        }

        if (request.getNovaSenha() == null || request.getNovaSenha().length() < 6) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("erro", "A nova senha deve ter pelo menos 6 caracteres."));
        }

        usuario.setSenha(request.getNovaSenha());
        usuario.setSenhaExpirada(false);
        usuarioService.salvar(usuario);

        // Gera token definitivo com o perfil real
        String nomePerfil = usuario.getPerfil() != null
            ? usuario.getPerfil().getNome() : "USER";
        String novoToken = jwtUtil.gerarToken(usuario.getEmail(), nomePerfil, usuario.getId());

        return ResponseEntity.ok(Map.of(
            "mensagem", "Senha alterada com sucesso.",
            "redirect",  resolverDestino(nomePerfil),
            "token",     novoToken
        ));
    }

    // ── USUÁRIO LOGADO ────────────────────────────────────────────────────────

    @Operation(summary = "Retorna dados do usuário autenticado pelo token")
    @GetMapping("/usuario/logado")
    public ResponseEntity<?> usuarioLogado(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("erro", "Não autenticado"));
        }

        // usuarioId vem do detalhe setado pelo JwtFilter
        Integer usuarioId = (Integer) authentication.getDetails();
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorId(usuarioId);

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("erro", "Usuário não encontrado"));
        }

        Usuario usuario    = usuarioOpt.get();
        String nomePerfil  = usuario.getPerfil() != null
            ? usuario.getPerfil().getNome() : "USER";

        return ResponseEntity.ok(Map.of(
            "id",       usuario.getId(),
            "nome",     usuario.getNome(),
            "email",    usuario.getEmail(),
            "perfil",   nomePerfil,
            "redirect", resolverDestino(nomePerfil)
        ));
    }

    // ── HELPER ────────────────────────────────────────────────────────────────

    private String resolverDestino(String perfil) {
        return switch (perfil.toUpperCase()) {
            case "ADMIN"       -> "/admin/adm.html";
            case "PROFESSOR"   -> "/professor/prof.html";
            case "ALUNO"       -> "/aluno/aluno.html";
            case "COORDENADOR" -> "/coordenacao/cord.html";
            default            -> "/login.html";
        };
    }
}