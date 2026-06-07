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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/professor")
public class DashboardProfessorController {

    private final UsuarioService usuarioService;
    private final ProvaService provaService;
    private final RespostaService respostaService;
    private final TurmaService turmaService;

    public DashboardProfessorController(UsuarioService usuarioService,
                                        ProvaService provaService,
                                        RespostaService respostaService,
                                        TurmaService turmaService) {
        this.usuarioService  = usuarioService;
        this.provaService    = provaService;
        this.respostaService = respostaService;
        this.turmaService    = turmaService;
    }

    // ── Utilitário: pega usuário logado da sessão ──────────────────────────

    private Optional<Usuario> getUsuarioLogado(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return Optional.empty();
        Integer usuarioId = (Integer) session.getAttribute("usuarioId");
        if (usuarioId == null) return Optional.empty();
        return usuarioService.buscarPorId(usuarioId);
    }

    // ── GET /api/professor/dashboard ──────────────────────────────────────
    // Stat-cards: total de provas, turmas, alunos e média geral

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(HttpServletRequest request) {
        Optional<Usuario> usuarioOpt = getUsuarioLogado(request);

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(401)
                .body(Map.of("erro", "Não autenticado"));
        }

        Integer professorId = usuarioOpt.get().getId();

        List<Prova> provas   = provaService.buscarPorProfessor(professorId);
        List<Turma> turmas   = turmaService.buscarTurmasPorProfessor(professorId);

        // Total de alunos somando alunos de cada turma
        int totalAlunos = turmas.stream()
            .mapToInt(t -> t.getAlunos() != null ? t.getAlunos().size() : 0)
            .sum();

        // Média geral das provas que já têm respostas
        double mediaGeral = 0.0;
        int provasComRespostas = 0;
        double somaMedias = 0.0;

        for (Prova prova : provas) {
            double media = respostaService.calcularMediaProva(prova.getId());
            if (media > 0) {
                somaMedias += media;
                provasComRespostas++;
            }
        }

        if (provasComRespostas > 0) {
            mediaGeral = Math.round((somaMedias / provasComRespostas) * 100.0) / 100.0;
        }

        return ResponseEntity.ok(new ResumoProfessorDTO(
            provas.size(),
            turmas.size(),
            totalAlunos,
            mediaGeral
        ));
    }

    // ── GET /api/professor/provas ──────────────────────────────────────────
    // Lista de provas do professor com participação e média calculadas

    @GetMapping("/provas")
    public ResponseEntity<?> getProvas(HttpServletRequest request) {
        Optional<Usuario> usuarioOpt = getUsuarioLogado(request);

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(401)
                .body(Map.of("erro", "Não autenticado"));
        }

        Integer professorId = usuarioOpt.get().getId();
        List<Prova> provas  = provaService.buscarPorProfessor(professorId);

        List<ResumoProvaDTO> resultado = new ArrayList<>();
        LocalDateTime agora = LocalDateTime.now();

        for (Prova prova : provas) {
            long participacao = respostaService.contarAlunosQueResponderam(prova.getId());
            double media      = respostaService.calcularMediaProva(prova.getId());

            String status;
            if (prova.getAtivo() == null || !prova.getAtivo()) {
                status = "Inativa";
            } else if (prova.getDataInicio() != null && prova.getDataInicio().isAfter(agora)) {
                status = "Agendada";
            } else if (prova.getDataFim() != null && prova.getDataFim().isBefore(agora)) {
                status = "Encerrada";
            } else {
                status = "Ativa";
            }

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

    // ── GET /api/professor/turmas ──────────────────────────────────────────
    // Turmas do professor com total de alunos e média

    @GetMapping("/turmas")
    public ResponseEntity<?> getTurmas(HttpServletRequest request) {
        Optional<Usuario> usuarioOpt = getUsuarioLogado(request);

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(401)
                .body(Map.of("erro", "Não autenticado"));
        }

        Integer professorId = usuarioOpt.get().getId();
        List<Turma> turmas  = turmaService.buscarTurmasPorProfessor(professorId);
        List<Prova> provas  = provaService.buscarPorProfessor(professorId);

        List<Map<String, Object>> resultado = new ArrayList<>();

        for (Turma turma : turmas) {
            int totalAlunos = turma.getAlunos() != null ? turma.getAlunos().size() : 0;

            // Provas desta turma
            List<Prova> provasDaTurma = provas.stream()
                .filter(p -> p.getTurma() != null
                          && p.getTurma().getId().equals(turma.getId()))
                .toList();

            // Média das provas da turma
            double mediaTurma = 0.0;
            int count = 0;
            for (Prova prova : provasDaTurma) {
                double media = respostaService.calcularMediaProva(prova.getId());
                if (media > 0) {
                    mediaTurma += media;
                    count++;
                }
            }
            if (count > 0) {
                mediaTurma = Math.round((mediaTurma / count) * 100.0) / 100.0;
            }

            // Última prova aplicada
            String ultimaProva = provasDaTurma.stream()
                .filter(p -> p.getDataInicio() != null)
                .max((a, b) -> a.getDataInicio().compareTo(b.getDataInicio()))
                .map(p -> p.getTitulo())
                .orElse("Nenhuma");

            resultado.add(Map.of(
                "turmaId",    turma.getId(),
                "nome",       turma.getNome(),
                "totalAlunos", totalAlunos,
                "totalProvas", provasDaTurma.size(),
                "media",       mediaTurma,
                "ultimaProva", ultimaProva
            ));
        }

        return ResponseEntity.ok(resultado);
    }

    // ── GET /api/professor/provas/{provaId}/resultados ────────────────────
    // Participação e média detalhada de uma prova específica

    @GetMapping("/provas/{provaId}/resultados")
    public ResponseEntity<?> getResultadosProva(@PathVariable Integer provaId,
                                                 HttpServletRequest request) {
        Optional<Usuario> usuarioOpt = getUsuarioLogado(request);

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(401)
                .body(Map.of("erro", "Não autenticado"));
        }

        Optional<Prova> provaOpt = provaService.buscarPorId(provaId);
        if (provaOpt.isEmpty()) {
            return ResponseEntity.status(404)
                .body(Map.of("erro", "Prova não encontrada"));
        }

        Prova prova = provaOpt.get();

        // Valida que a prova pertence ao professor logado
        Integer professorId = usuarioOpt.get().getId();
        if (prova.getProfessor() == null
                || !prova.getProfessor().getId().equals(professorId)) {
            return ResponseEntity.status(403)
                .body(Map.of("erro", "Acesso negado"));
        }

        long participacao = respostaService.contarAlunosQueResponderam(provaId);
        double media      = respostaService.calcularMediaProva(provaId);
        int totalQuestoes = prova.getQuestoes() != null ? prova.getQuestoes().size() : 0;

        return ResponseEntity.ok(Map.of(
            "provaId",       provaId,
            "titulo",        prova.getTitulo(),
            "participacao",  participacao,
            "totalQuestoes", totalQuestoes,
            "media",         media
        ));
    }
}