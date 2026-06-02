package com.projeto.sistema_escolar.controller;

import com.projeto.sistema_escolar.dto.UsuarioRequestDTO;
import com.projeto.sistema_escolar.dto.UsuarioResponseDTO;
import com.projeto.sistema_escolar.model.Perfil;
import com.projeto.sistema_escolar.model.Turma;
import com.projeto.sistema_escolar.model.Usuario;
import com.projeto.sistema_escolar.service.PerfilService;
import com.projeto.sistema_escolar.service.TurmaService;
import com.projeto.sistema_escolar.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService service;
    private final PerfilService perfilService;
    private final TurmaService turmaService;

    public UsuarioController(UsuarioService service,
                             PerfilService perfilService,
                             TurmaService turmaService) {
        this.service = service;
        this.perfilService = perfilService;
        this.turmaService = turmaService;
    }

    // ==================== LISTAR TODOS ====================
    
    @GetMapping
    public List<UsuarioResponseDTO> listar() {
        return service.listarTodos().stream()
                .map(UsuarioResponseDTO::new)
                .collect(Collectors.toList());
    }

    // ==================== BUSCAR POR ID ====================
    
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable Integer id) {
        Optional<Usuario> usuario = service.buscarPorId(id);
        return usuario.map(u -> ResponseEntity.ok(new UsuarioResponseDTO(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    // ==================== CRIAR NOVO USUÁRIO ====================
    
    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> criar(@Valid @RequestBody UsuarioRequestDTO usuarioDTO) {
        Usuario usuario = new Usuario();
        usuario.setNome(usuarioDTO.getNome());
        usuario.setEmail(usuarioDTO.getEmail());
        usuario.setSenha(usuarioDTO.getSenha());
        usuario.setAtivo(usuarioDTO.getAtivo() != null ? usuarioDTO.getAtivo() : true);
        
        // Associar Perfil
        if (usuarioDTO.getPerfilId() != null) {
            Optional<Perfil> perfil = perfilService.buscarPorId(usuarioDTO.getPerfilId());
            perfil.ifPresent(usuario::setPerfil);
        }
        
        // Associar Turma
        if (usuarioDTO.getTurmaId() != null) {
            Optional<Turma> turma = turmaService.buscarPorId(usuarioDTO.getTurmaId());
            turma.ifPresent(usuario::setTurma);
        }
        
        Usuario saved = service.salvar(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(new UsuarioResponseDTO(saved));
    }

    // ==================== ATUALIZAR USUÁRIO ====================
    
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> atualizar(@PathVariable Integer id,
                                                        @Valid @RequestBody UsuarioRequestDTO usuarioDTO) {
        return service.buscarPorId(id).map(usuario -> {
            usuario.setNome(usuarioDTO.getNome());
            usuario.setEmail(usuarioDTO.getEmail());
            usuario.setAtivo(usuarioDTO.getAtivo() != null ? usuarioDTO.getAtivo() : usuario.getAtivo());
            
            // Atualizar senha se fornecida
            String novaSenha = usuarioDTO.getSenha();
            if (novaSenha != null && !novaSenha.trim().isEmpty()) {
                usuario.setSenha(novaSenha);
            }
            
            // Atualizar Perfil
            if (usuarioDTO.getPerfilId() != null) {
                perfilService.buscarPorId(usuarioDTO.getPerfilId())
                        .ifPresent(usuario::setPerfil);
            }
            
            // Atualizar Turma
            if (usuarioDTO.getTurmaId() != null) {
                turmaService.buscarPorId(usuarioDTO.getTurmaId())
                        .ifPresent(usuario::setTurma);
            }
            
            Usuario updated = service.salvar(usuario);
            return ResponseEntity.ok(new UsuarioResponseDTO(updated));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ==================== DELETAR USUÁRIO ====================
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        if (service.existePorId(id)) {
            service.deletar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}