package com.projeto.sistema_escolar.controller;

import com.projeto.sistema_escolar.dto.LoginRequestDTO;
import com.projeto.sistema_escolar.dto.LoginResponseDTO;
import com.projeto.sistema_escolar.dto.TrocarSenhaRequestDTO;
import com.projeto.sistema_escolar.model.Usuario;
import com.projeto.sistema_escolar.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class LoginController {

    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;
    private final SecurityContextRepository securityContextRepository;

    public LoginController(UsuarioService usuarioService,
                           PasswordEncoder passwordEncoder,
                           SecurityContextRepository securityContextRepository) {
        this.usuarioService = usuarioService;
        this.passwordEncoder = passwordEncoder;
        this.securityContextRepository = securityContextRepository;
    }

    // ── LOGIN ─────────────────────────────────────────────────────────────────

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO request,
                                   HttpServletRequest httpRequest,
                                   HttpServletResponse httpResponse) {

        // 1. Busca o usuário pelo e-mail
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorEmail(request.getEmail());

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new LoginResponseDTO("E-mail ou senha incorretos"));
        }

        Usuario usuario = usuarioOpt.get();

        // 2. Verifica a senha
        if (!passwordEncoder.matches(request.getSenha(), usuario.getSenha())) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new LoginResponseDTO("E-mail ou senha incorretos"));
        }

        // 3. Verifica se a conta está ativa
        if (usuario.getAtivo() == null || !usuario.getAtivo()) {
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new LoginResponseDTO("Conta desativada. Entre em contato com o administrador."));
        }

        // 4. Verifica se a senha está expirada (primeiro acesso)
        if (Boolean.TRUE.equals(usuario.getSenhaExpirada())) {
            // Cria sessão temporária para permitir a troca de senha
            HttpSession session = httpRequest.getSession(true);
            session.setAttribute("usuarioIdTrocarSenha", usuario.getId());

            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new LoginResponseDTO(
                    usuario.getNome(),
                    usuario.getPerfil() != null ? usuario.getPerfil().getNome() : "USER",
                    "Você precisa trocar sua senha no primeiro acesso.",
                    true
                ));
        }

        // 5. Monta a autenticação com o perfil correto
        String nomePerfil = usuario.getPerfil() != null ? usuario.getPerfil().getNome() : "USER";
        String role = "ROLE_" + nomePerfil.toUpperCase();

        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));

        UsernamePasswordAuthenticationToken authToken =
            new UsernamePasswordAuthenticationToken(usuario.getEmail(), null, authorities);

        // 6. PASSO CRÍTICO: salva o contexto de autenticação na sessão HTTP
        //    Sem isso o contexto é perdido na próxima requisição
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authToken);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        // 7. Salva dados úteis na sessão para uso posterior
        HttpSession session = httpRequest.getSession(true);
        session.setAttribute("usuarioId", usuario.getId());
        session.setAttribute("perfil", nomePerfil);

        // 8. Determina para qual página redirecionar
        String destino = resolverDestino(nomePerfil);

        return ResponseEntity.ok(new LoginResponseDTO(
            usuario.getNome(),
            nomePerfil,
            "Login realizado com sucesso",
            true,
            false,
            destino
        ));
    }

    // ── LOGOUT ────────────────────────────────────────────────────────────────

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext();

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        return ResponseEntity.ok().build();
    }

    // ── TROCAR SENHA ──────────────────────────────────────────────────────────

    @PostMapping("/trocar-senha")
    public ResponseEntity<?> trocarSenha(@RequestBody TrocarSenhaRequestDTO request,
                                          HttpServletRequest httpRequest,
                                          HttpServletResponse httpResponse) {

        HttpSession session = httpRequest.getSession(false);

        // Aceita tanto sessão de usuário logado quanto sessão temporária de primeiro acesso
        Integer usuarioId = null;
        if (session != null) {
            usuarioId = (Integer) session.getAttribute("usuarioId");
            if (usuarioId == null) {
                usuarioId = (Integer) session.getAttribute("usuarioIdTrocarSenha");
            }
        }

        if (usuarioId == null) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("erro", "Sessão inválida. Faça login novamente."));
        }

        Optional<Usuario> usuarioOpt = usuarioService.buscarPorId(usuarioId);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("erro", "Usuário não encontrado."));
        }

        Usuario usuario = usuarioOpt.get();

        if (!passwordEncoder.matches(request.getSenhaAtual(), usuario.getSenha())) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("erro", "Senha atual incorreta."));
        }

        if (request.getNovaSenha() == null || request.getNovaSenha().length() < 6) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("erro", "A nova senha deve ter pelo menos 6 caracteres."));
        }

        // Salva nova senha e marca como não expirada
        usuario.setSenha(request.getNovaSenha());
        usuario.setSenhaExpirada(false);
        usuarioService.salvar(usuario);

        // Autentica o usuário após troca de senha
        String nomePerfil = usuario.getPerfil() != null ? usuario.getPerfil().getNome() : "USER";
        String role = "ROLE_" + nomePerfil.toUpperCase();
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));

        UsernamePasswordAuthenticationToken authToken =
            new UsernamePasswordAuthenticationToken(usuario.getEmail(), null, authorities);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authToken);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        session.setAttribute("usuarioId", usuario.getId());
        session.setAttribute("perfil", nomePerfil);
        session.removeAttribute("usuarioIdTrocarSenha");

        return ResponseEntity.ok(Map.of(
            "mensagem", "Senha alterada com sucesso.",
            "redirect", resolverDestino(nomePerfil)
        ));
    }

    // ── USUÁRIO LOGADO ────────────────────────────────────────────────────────

    @GetMapping("/usuario/logado")
    public ResponseEntity<?> usuarioLogado(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("usuarioId") == null) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("erro", "Não autenticado"));
        }

        Integer usuarioId = (Integer) session.getAttribute("usuarioId");
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorId(usuarioId);

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("erro", "Usuário não encontrado"));
        }

        Usuario usuario = usuarioOpt.get();
        String nomePerfil = usuario.getPerfil() != null ? usuario.getPerfil().getNome() : "USER";

        return ResponseEntity.ok(Map.of(
            "id",           usuario.getId(),
            "nome",         usuario.getNome(),
            "email",        usuario.getEmail(),
            "perfil",       nomePerfil,
            "redirect",     resolverDestino(nomePerfil)
        ));
    }

    // ── HELPER PRIVADO ────────────────────────────────────────────────────────

    /**
     * Mapeia o nome do perfil para o caminho da página HTML correspondente.
     * Centralizado aqui para manter consistência entre login, trocar-senha
     * e qualquer outro fluxo que precise redirecionar.
     */
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