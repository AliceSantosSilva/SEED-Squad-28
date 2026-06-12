package com.projeto.sistema_escolar.controller;

import com.projeto.sistema_escolar.dto.ResumoProfessorDTO;
import com.projeto.sistema_escolar.dto.ResumoProvaDTO;
import com.projeto.sistema_escolar.model.Prova;
import com.projeto.sistema_escolar.model.Turma;
import com.projeto.sistema_escolar.model.Usuario;
import com.projeto.sistema_escolar.service.ProvaService;
import com.projeto.sistema_escolar.service.RespostaService;
import com.projeto.sistema_escolar.service.TurmaService;
import com.projeto.sistema_escolar.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/professor")
public class DashboardProfessorController {

    private final UsuarioService  usuarioService;
    private final ProvaService    provaService;
    private final RespostaService respostaService;
    private final TurmaService    turmaService;

    public DashboardProfessorController(UsuarioService usuarioService,
                                        ProvaService provaService,
                                        RespostaService respostaService,
                                        TurmaService turmaService) {
        this.usuarioService  = usuarioService;
        this.provaService    = provaService;
        this.respostaService = respostaService;
        this.turmaService    = turmaService;
    }

    // ── Utilitário ────────────────────────────────────────────────────────

    private Optional<Usuario> getUsuarioLogado(Authentication auth) {
        if (auth == null) return Optional.empty();
        Integer usuarioId = (Integer) auth.getDetails();
        if (usuarioId == null) return Optional.empty();
        return usuarioService.buscarPorId(usuarioId);
    }

    // ── GET /api/professor/dashboard ──────────────────────────────────────

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(Authentication auth) {
        Optional<Usuario> usuarioOpt = getUsuarioLogado(auth);

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(401)
                .body(Map.of("erro", "Não autenticado"));
        }

        Integer professorId = usuarioOpt.get().getId();

        List<Prova> provas = provaService.buscarPorProfessor(professorId);
        List<Turma> turmas = turmaService.buscarTurmasPorProfessor(professorId);

        int totalAlunos = turmas.stream()
            .mapToInt(t -> t.getAlunos() != null ? t.getAlunos().size() : 0)
            .sum();

        double mediaGeral     = 0.0;
        int    provasComResp  = 0;
        double somaMedias     = 0.0;

        for (Prova prova : provas) {
            double media = respostaService.calcularMediaProva(prova.getId());
            if (media > 0) {
                somaMedias += media;
                provasComResp++;
            }
        }

        if (provasComResp > 0) {
            mediaGeral = Math.round((somaMedias / provasComResp) * 100.0) / 100.0;
        }

        return ResponseEntity.ok(new ResumoProfessorDTO(
            provas.size(),
            turmas.size(),
            totalAlunos,
            mediaGeral
        ));
    }

    // ── GET /api/professor/provas ─────────────────────────────────────────

    @GetMapping("/provas")
    public ResponseEntity<?> getProvas(Authentication auth) {
        Optional<Usuario> usuarioOpt = getUsuarioLogado(auth);

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(401)
                .body(Map.of("erro", "Não autenticado"));
        }

        Integer     professorId = usuarioOpt.get().getId();
        List<Prova> provas      = provaService.buscarPorProfessor(professorId);
        LocalDateTime agora     = LocalDateTime.now();

        List<ResumoProvaDTO> resultado = new ArrayList<>();

        for (Prova prova : provas) {
            long   participacao = respostaService.contarAlunosQueResponderam(prova.getId());
            double media        = respostaService.calcularMediaProva(prova.getId());

            String status;
            if (prova.getAtivo() == null || !prova.getAtivo())           status = "Inativa";
            else if (prova.getDataInicio() != null
                  && prova.getDataInicio().isAfter(agora))                status = "Agendada";
            else if (prova.getDataFim() != null
                  && prova.getDataFim().isBefore(agora))                  status = "Encerrada";
            else                                                           status = "Ativa";

            String nomeTurma = prova.getTurma() != null
                ? prova.getTurma().getNome() : "—";

            resultado.add(new ResumoProvaDTO(
                prova.getId(),
                prova.getTitulo(),
                nomeTurma,
                prova.getDataInicio(),
                prova.getDataFim(),
                status,
                participacao,
                prova.getQuestoes() != null ? prova.getQuestoes().size() : 0,
                media
            ));
        }

        return ResponseEntity.ok(resultado);
    }

    // ── GET /api/professor/turmas ─────────────────────────────────────────

    @GetMapping("/turmas")
    public ResponseEntity<?> getTurmas(Authentication auth) {
        Optional<Usuario> usuarioOpt = getUsuarioLogado(auth);

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(401)
                .body(Map.of("erro", "Não autenticado"));
        }

        Integer     professorId = usuarioOpt.get().getId();
        List<Turma> turmas      = turmaService.buscarTurmasPorProfessor(professorId);
        List<Prova> provas      = provaService.buscarPorProfessor(professorId);

        List<Map<String, Object>> resultado = new ArrayList<>();

        for (Turma turma : turmas) {
            int totalAlunos = turma.getAlunos() != null ? turma.getAlunos().size() : 0;

            List<Prova> provasDaTurma = provas.stream()
                .filter(p -> p.getTurma() != null
                          && p.getTurma().getId().equals(turma.getId()))
                .toList();

            double mediaTurma = 0.0;
            int    count      = 0;
            for (Prova prova : provasDaTurma) {
                double media = respostaService.calcularMediaProva(prova.getId());
                if (media > 0) { mediaTurma += media; count++; }
            }
            if (count > 0) {
                mediaTurma = Math.round((mediaTurma / count) * 100.0) / 100.0;
            }

            String ultimaProva = provasDaTurma.stream()
                .filter(p -> p.getDataInicio() != null)
                .max((a, b) -> a.getDataInicio().compareTo(b.getDataInicio()))
                .map(Prova::getTitulo)
                .orElse("Nenhuma");

            resultado.add(Map.of(
                "turmaId",     turma.getId(),
                "nome",        turma.getNome(),
                "totalAlunos", totalAlunos,
                "totalProvas", provasDaTurma.size(),
                "media",       mediaTurma,
                "ultimaProva", ultimaProva
            ));
        }

        return ResponseEntity.ok(resultado);
    }

    // ── GET /api/professor/provas/{provaId}/resultados ────────────────────

    @GetMapping("/provas/{provaId}/resultados")
    public ResponseEntity<?> getResultadosProva(@PathVariable Integer provaId,
                                                 Authentication auth) {
        Optional<Usuario> usuarioOpt = getUsuarioLogado(auth);

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(401)
                .body(Map.of("erro", "Não autenticado"));
        }

        Optional<Prova> provaOpt = provaService.buscarPorId(provaId);
        if (provaOpt.isEmpty()) {
            return ResponseEntity.status(404)
                .body(Map.of("erro", "Prova não encontrada"));
        }

        Prova   prova       = provaOpt.get();
        Integer professorId = usuarioOpt.get().getId();

        if (prova.getProfessor() == null
                || !prova.getProfessor().getId().equals(professorId)) {
            return ResponseEntity.status(403)
                .body(Map.of("erro", "Acesso negado"));
        }

        long   participacao = respostaService.contarAlunosQueResponderam(provaId);
        double media        = respostaService.calcularMediaProva(provaId);
        int    totalQuestoes = prova.getQuestoes() != null ? prova.getQuestoes().size() : 0;

        return ResponseEntity.ok(Map.of(
            "provaId",       provaId,
            "titulo",        prova.getTitulo(),
            "participacao",  participacao,
            "totalQuestoes", totalQuestoes,
            "media",         media
        ));
    }
}