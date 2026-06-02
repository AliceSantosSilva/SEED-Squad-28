package com.projeto.sistema_escolar.controller;

import com.projeto.sistema_escolar.model.Aluno;
import com.projeto.sistema_escolar.service.AlunoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/alunos")
public class AlunoController {

    private final AlunoService service;

    public AlunoController(AlunoService service) {
        this.service = service;
    }

    @GetMapping
    public List<Aluno> listar() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Aluno> buscarPorId(@PathVariable Integer id) {
        Optional<Aluno> aluno = service.buscarPorId(id);
        return aluno.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<Aluno> buscarPorUsuario(@PathVariable Integer usuarioId) {
        return service.buscarPorUsuarioId(usuarioId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Aluno> criar(@Valid @RequestBody Aluno aluno) {
        Aluno saved = service.salvar(aluno);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Aluno> atualizar(@PathVariable Integer id, @Valid @RequestBody Aluno alunoAtualizado) {
        return service.buscarPorId(id).map(aluno -> {
            aluno.setNome(alunoAtualizado.getNome());
            aluno.setMatricula(alunoAtualizado.getMatricula());
            aluno.setEmail(alunoAtualizado.getEmail());
            aluno.setIdade(alunoAtualizado.getIdade());
            aluno.setStatus(alunoAtualizado.getStatus());
            aluno.setTurma(alunoAtualizado.getTurma());
            aluno.setUsuario(alunoAtualizado.getUsuario());
            return ResponseEntity.ok(service.salvar(aluno));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        if (service.existePorId(id)) {
            service.deletar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}