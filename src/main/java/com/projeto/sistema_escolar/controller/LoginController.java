package com.projeto.sistema_escolar.controller;

import com.projeto.sistema_escolar.dto.LoginRequest;
import com.projeto.sistema_escolar.dto.LoginResponse;
import com.projeto.sistema_escolar.dto.TrocarSenhaRequest;
import com.projeto.sistema_escolar.model.Usuario;
import com.projeto.sistema_escolar.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class LoginController {

    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;

    public LoginController(UsuarioService usuarioService, PasswordEncoder passwordEncoder) {
        this.usuarioService = usuarioService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpSession session) {
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorEmail(request.getEmail());
        
        if (!usuarioOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new LoginResponse("Usuário não encontrado"));
        }
        
        Usuario usuario = usuarioOpt.get();
        
        if (!passwordEncoder.matches(request.getSenha(), usuario.getSenha())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new LoginResponse("Senha incorreta"));
        }
        
        if (usuario.getSenhaExpirada() != null && usuario.getSenhaExpirada()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new LoginResponse(
                    usuario.getNome(),
                    usuario.getPerfil() != null ? usuario.getPerfil().getNome() : "USER",
                    "Você precisa trocar sua senha no primeiro acesso",
                    true
                ));
        }
        
        session.setAttribute("usuarioId", usuario.getId());
        session.setAttribute("perfil", usuario.getPerfil() != null ? usuario.getPerfil().getNome() : "USER");
        
        String role = "ROLE_" + (usuario.getPerfil() != null ? usuario.getPerfil().getNome() : "USER");
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));
        
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            usuario.getEmail(), null, authorities
        );
        
        SecurityContextHolder.getContext().setAuthentication(auth);
        
        return ResponseEntity.ok(new LoginResponse(
            usuario.getNome(),
            usuario.getPerfil() != null ? usuario.getPerfil().getNome() : "USER",
            "Login realizado com sucesso",
            true,
            false
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        SecurityContextHolder.clearContext();
        session.invalidate();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/trocar-senha")
    public ResponseEntity<?> trocarSenha(@RequestBody TrocarSenhaRequest request, HttpSession session) {
        Integer usuarioId = (Integer) session.getAttribute("usuarioId");
        
        if (usuarioId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("erro", "Usuário não está autenticado"));
        }
        
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorId(usuarioId);
        
        if (!usuarioOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("erro", "Usuário não encontrado"));
        }
        
        Usuario usuario = usuarioOpt.get();
        
        if (!passwordEncoder.matches(request.getSenhaAtual(), usuario.getSenha())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("erro", "Senha atual incorreta"));
        }
        
        if (request.getNovaSenha() == null || request.getNovaSenha().length() < 6) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("erro", "A nova senha deve ter pelo menos 6 caracteres"));
        }
        
        usuario.setSenha(request.getNovaSenha());
        usuario.setSenhaExpirada(false);
        usuarioService.salvar(usuario);
        
        String role = "ROLE_" + (usuario.getPerfil() != null ? usuario.getPerfil().getNome() : "USER");
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            usuario.getEmail(), null, authorities
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
        
        return ResponseEntity.ok(Map.of("mensagem", "Senha alterada com sucesso"));
    }

    @GetMapping("/usuario/logado")
    public ResponseEntity<?> usuarioLogado(HttpSession session) {
        Integer usuarioId = (Integer) session.getAttribute("usuarioId");
        
        if (usuarioId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("erro", "Não autenticado"));
        }
        
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorId(usuarioId);
        
        if (!usuarioOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("erro", "Usuário não encontrado"));
        }
        
        Usuario usuario = usuarioOpt.get();
        
        return ResponseEntity.ok(Map.of(
            "id", usuario.getId(),
            "nome", usuario.getNome(),
            "email", usuario.getEmail(),
            "perfil", usuario.getPerfil() != null ? usuario.getPerfil().getNome() : "USER",
            "senhaExpirada", usuario.getSenhaExpirada()
        ));
    }
}