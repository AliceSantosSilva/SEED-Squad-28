package com.projeto.sistema_escolar.controller;

import com.projeto.sistema_escolar.dto.UsuarioRequestDTO;
import com.projeto.sistema_escolar.dto.UsuarioResponseDTO;
import com.projeto.sistema_escolar.model.Perfil;
import com.projeto.sistema_escolar.model.Turma;
import com.projeto.sistema_escolar.model.Usuario;
import com.projeto.sistema_escolar.service.PerfilService;
import com.projeto.sistema_escolar.service.TurmaService;
import com.projeto.sistema_escolar.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuários", description = "CRUD de usuários do sistema")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final PerfilService  perfilService;
    private final TurmaService   turmaService;

    public UsuarioController(UsuarioService usuarioService,
                             PerfilService perfilService,
                             TurmaService turmaService) {
        this.usuarioService = usuarioService;
        this.perfilService  = perfilService;
        this.turmaService   = turmaService;
    }

    // ── LISTAR ────────────────────────────────────────────────────────────────

    @Operation(summary = "Lista todos os usuários (sem senha)")
    @GetMapping
    public List<UsuarioResponseDTO> listar() {
        return usuarioService.listarTodos().stream()
                .map(UsuarioResponseDTO::new)
                .collect(Collectors.toList());
    }

    // ── BUSCAR POR ID ─────────────────────────────────────────────────────────

    @Operation(summary = "Busca um usuário por ID (sem senha)")
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable Integer id) {
        return usuarioService.buscarPorId(id)
                .map(u -> ResponseEntity.ok(new UsuarioResponseDTO(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    // ── CRIAR ─────────────────────────────────────────────────────────────────

    @Operation(summary = "Cria novo usuário — perfilId é obrigatório")
    @PostMapping
    public ResponseEntity<?> criar(@Valid @RequestBody UsuarioRequestDTO dto) {

        // Perfil é obrigatório — sem ele o usuário ficaria sem papel no sistema
        if (dto.getPerfilId() == null) {
            return ResponseEntity.badRequest()
                .body(Map.of("erro", "perfilId é obrigatório."));
        }

        Optional<Perfil> perfil = perfilService.buscarPorId(dto.getPerfilId());
        if (perfil.isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("erro", "Perfil não encontrado com id: " + dto.getPerfilId()));
        }

        // Verifica e-mail duplicado
        if (usuarioService.buscarPorEmail(dto.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("erro", "E-mail já cadastrado."));
        }

        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail().trim().toLowerCase());
        usuario.setSenha(dto.getSenha());         // será criptografada no service
        usuario.setPerfil(perfil.get());
        usuario.setAtivo(dto.getAtivo() != null ? dto.getAtivo() : true);
        usuario.setSenhaExpirada(true);           // força troca no primeiro acesso

        if (dto.getTurmaId() != null) {
            turmaService.buscarPorId(dto.getTurmaId())
                .ifPresent(usuario::setTurma);
        }

        Usuario salvo = usuarioService.salvar(usuario);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new UsuarioResponseDTO(salvo));
    }

    // ── ATUALIZAR ─────────────────────────────────────────────────────────────

    @Operation(summary = "Atualiza usuário — senha só é alterada se fornecida")
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Integer id,
                                       @Valid @RequestBody UsuarioRequestDTO dto) {

        Optional<Usuario> usuarioOpt = usuarioService.buscarPorId(id);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Usuario usuario = usuarioOpt.get();
        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail().trim().toLowerCase());
        usuario.setAtivo(dto.getAtivo() != null ? dto.getAtivo() : usuario.getAtivo());

        // Senha: só atualiza se veio preenchida no body
        // O service garante que não vai reencriptar uma senha já criptografada
        String novaSenha = dto.getSenha();
        if (novaSenha != null && !novaSenha.trim().isEmpty()) {
            usuario.setSenha(novaSenha);
        }

        // Perfil: só atualiza se veio no body
        if (dto.getPerfilId() != null) {
            perfilService.buscarPorId(dto.getPerfilId())
                .ifPresent(usuario::setPerfil);
        }

        // Turma: só atualiza se veio no body
        if (dto.getTurmaId() != null) {
            turmaService.buscarPorId(dto.getTurmaId())
                .ifPresent(usuario::setTurma);
        }

        Usuario atualizado = usuarioService.salvar(usuario);
        return ResponseEntity.ok(new UsuarioResponseDTO(atualizado));
    }

    // ── DELETAR ───────────────────────────────────────────────────────────────

    @Operation(summary = "Remove um usuário por ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        if (!usuarioService.existePorId(id)) {
            return ResponseEntity.notFound().build();
        }
        usuarioService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}