package com.projeto.sistema_escolar.controller;

import com.projeto.sistema_escolar.model.Aluno;
import com.projeto.sistema_escolar.model.Turma;
import com.projeto.sistema_escolar.service.AlunoService;
import com.projeto.sistema_escolar.service.TurmaService;
import com.projeto.sistema_escolar.util.EscolaFiltroUtil;
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
    private final TurmaService turmaService;
    private final EscolaFiltroUtil filtroUtil;

    public AlunoController(AlunoService service, TurmaService turmaService, EscolaFiltroUtil filtroUtil) {
        this.service = service;
        this.turmaService = turmaService;
        this.filtroUtil = filtroUtil;
    }

    @GetMapping
    public List<Aluno> listar() {
        Integer escolaId = filtroUtil.getEscolaIdDoUsuarioLogado();
        if (escolaId != null) {
            return service.listarPorEscola(escolaId);
        }
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
        Integer escolaId = filtroUtil.getEscolaIdDoUsuarioLogado();
        if (escolaId != null && aluno.getTurma() != null && aluno.getTurma().getId() != null) {
            Turma turma = turmaService.buscarPorId(aluno.getTurma().getId())
                    .orElseThrow(() -> new RuntimeException("Turma não encontrada"));
            if (turma.getEscola() == null || !turma.getEscola().getId().equals(escolaId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        Aluno saved = service.salvar(aluno);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Aluno> atualizar(@PathVariable Integer id, @Valid @RequestBody Aluno alunoAtualizado) {
        Optional<Aluno> alunoOpt = service.buscarPorId(id);
        if (alunoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Aluno aluno = alunoOpt.get();
        aluno.setNome(alunoAtualizado.getNome());
        aluno.setMatricula(alunoAtualizado.getMatricula());
        aluno.setEmail(alunoAtualizado.getEmail());
        aluno.setIdade(alunoAtualizado.getIdade());
        aluno.setStatus(alunoAtualizado.getStatus());
        
        if (alunoAtualizado.getTurma() != null && alunoAtualizado.getTurma().getId() != null) {
            Integer escolaId = filtroUtil.getEscolaIdDoUsuarioLogado();
            if (escolaId != null) {
                Turma turma = turmaService.buscarPorId(alunoAtualizado.getTurma().getId())
                        .orElseThrow(() -> new RuntimeException("Turma não encontrada"));
                if (turma.getEscola() == null || !turma.getEscola().getId().equals(escolaId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }
            aluno.setTurma(alunoAtualizado.getTurma());
        }
        aluno.setUsuario(alunoAtualizado.getUsuario());
        
        Aluno saved = service.salvar(aluno);
        return ResponseEntity.ok(saved);
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