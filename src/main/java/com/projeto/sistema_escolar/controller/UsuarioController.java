package com.projeto.sistema_escolar.controller;

import com.projeto.sistema_escolar.model.Usuario;
import com.projeto.sistema_escolar.service.UsuarioService;
import com.projeto.sistema_escolar.service.PerfilService;
import com.projeto.sistema_escolar.service.TurmaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
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

    @GetMapping
    public List<Usuario> listar() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Usuario> criar(@RequestBody Usuario usuario) {
        // Buscar e associar Perfil se fornecido
        if (usuario.getPerfil() != null && usuario.getPerfil().getId() != null) {
            perfilService.buscarPorId(usuario.getPerfil().getId())
                .ifPresent(usuario::setPerfil);
        }
        
        // Buscar e associar Turma se fornecido
        if (usuario.getTurma() != null && usuario.getTurma().getId() != null) {
            turmaService.buscarPorId(usuario.getTurma().getId())
                .ifPresent(usuario::setTurma);
        }
        
        Usuario saved = service.salvar(usuario);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Usuario> atualizar(@PathVariable Long id, @RequestBody Usuario usuarioAtualizado) {
        return service.buscarPorId(id).map(usuario -> {
            usuario.setNome(usuarioAtualizado.getNome());
            usuario.setEmail(usuarioAtualizado.getEmail());
            usuario.setSenha(usuarioAtualizado.getSenha());
            usuario.setAtivo(usuarioAtualizado.getAtivo());
            
            // Atualizar Perfil
            if (usuarioAtualizado.getPerfil() != null && usuarioAtualizado.getPerfil().getId() != null) {
                perfilService.buscarPorId(usuarioAtualizado.getPerfil().getId())
                    .ifPresent(usuario::setPerfil);
            }
            
            // Atualizar Turma
            if (usuarioAtualizado.getTurma() != null && usuarioAtualizado.getTurma().getId() != null) {
                turmaService.buscarPorId(usuarioAtualizado.getTurma().getId())
                    .ifPresent(usuario::setTurma);
            }
            
            return ResponseEntity.ok(service.salvar(usuario));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (service.existePorId(id)) {
            service.deletar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}