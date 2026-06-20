package com.projeto.sistema_escolar.controller;

import com.projeto.sistema_escolar.model.Usuario;
import com.projeto.sistema_escolar.security.JwtUtil;
import com.projeto.sistema_escolar.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.projeto.sistema_escolar.dto.LoginResponseDTO;
import java.util.Optional;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final UsuarioService  usuarioService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil         jwtUtil;

    public AuthController(UsuarioService usuarioService,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil) {
        this.usuarioService  = usuarioService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil         = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String senha = body.get("senha");

        Optional<Usuario> usuarioOpt = usuarioService.buscarPorEmail(email);

        if (usuarioOpt.isEmpty() || !passwordEncoder.matches(senha, usuarioOpt.get().getSenha())) {
            return ResponseEntity.status(401)
                .body(new LoginResponseDTO("Email ou senha inválidos"));
        }

        Usuario u = usuarioOpt.get();
        String perfil = u.getPerfil().getNome();
        String token  = jwtUtil.gerarToken(u.getEmail(), perfil, u.getId());

        return ResponseEntity.ok(new LoginResponseDTO(
            u.getNome(),
            perfil,
            "Login realizado com sucesso",
            true,   // sucesso
            false,  // senhaExpirada
            null,   // redirect — front calcula via obterDestinoPorPerfil()
            token
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(Map.of("mensagem", "Logout realizado com sucesso"));
    }

    @GetMapping("/usuario/logado")
    public ResponseEntity<?> usuarioLogado(Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        String email = (String) auth.getPrincipal();
        return usuarioService.buscarPorEmail(email)
            .map(u -> ResponseEntity.ok(Map.of(
                "id",     u.getId(),
                "nome",   u.getNome(),
                "email",  u.getEmail(),
                "perfil", u.getPerfil().getNome()
            )))
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/trocar-senha")
    public ResponseEntity<?> trocarSenha(@RequestBody Map<String, String> body,
                                          Authentication auth) {
        String email     = (String) auth.getPrincipal();
        String senhaAtual = body.get("senhaAtual");
        String novaSenha  = body.get("novaSenha");

        return usuarioService.buscarPorEmail(email)
            .map(u -> {
                if (!passwordEncoder.matches(senhaAtual, u.getSenha())) {
                    return ResponseEntity.status(400)
                        .body(Map.of("erro", "Senha atual incorreta"));
                }
                u.setSenha(passwordEncoder.encode(novaSenha));
                usuarioService.salvar(u);
                return ResponseEntity.ok(Map.of("mensagem", "Senha alterada com sucesso"));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/cadastro")
    public ResponseEntity<?> cadastro(@RequestBody Usuario usuario) {
        if (usuarioService.buscarPorEmail(usuario.getEmail()).isPresent()) {
            return ResponseEntity.status(409)
                .body(Map.of("erro", "Email já cadastrado"));
        }
        Usuario salvo = usuarioService.salvar(usuario);
        return ResponseEntity.status(201).body(Map.of(
            "id",    salvo.getId(),
            "nome",  salvo.getNome(),
            "email", salvo.getEmail()
        ));
    }
}