package com.projeto.sistema_escolar.controller;

import com.projeto.sistema_escolar.dto.ResumoCoordenacaoDTO;
import com.projeto.sistema_escolar.model.Prova;
import com.projeto.sistema_escolar.model.Usuario;
import com.projeto.sistema_escolar.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/coordenacao")
public class DashboardCoordenacaoController {

    private final UsuarioService usuarioService;
    private final CoordenadorService coordenadorService;
    private final EscolaService escolaService;
    private final ProfessorService professorService;
    private final TurmaService turmaService;
    private final ProvaService provaService;
    private final RespostaService respostaService;
    private final AlunoService alunoService;

    public DashboardCoordenacaoController(UsuarioService usuarioService,
                                           CoordenadorService coordenadorService,
                                           EscolaService escolaService,
                                           ProfessorService professorService,
                                           TurmaService turmaService,
                                           ProvaService provaService,
                                           RespostaService respostaService,
                                           AlunoService alunoService) {
        this.usuarioService      = usuarioService;
        this.coordenadorService  = coordenadorService;
        this.escolaService       = escolaService;
        this.professorService    = professorService;
        this.turmaService        = turmaService;
        this.provaService        = provaService;
        this.respostaService     = respostaService;
        this.alunoService        = alunoService;
    }

    // ── Utilitário: pega escola do coordenador logado ──────────────────────

    private Optional<Integer> getEscolaIdDocoordenador(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return Optional.empty();
        Integer usuarioId = (Integer) session.getAttribute("usuarioId");
        if (usuarioId == null) return Optional.empty();

        return coordenadorService.buscarPorUsuarioId(usuarioId)
            .map(coord -> coord.getEscola() != null
                ? coord.getEscola().getId() : null);
    }

    // ── GET /api/coordenacao/dashboard ────────────────────────────────────
    // Stat-cards do painel da coordenação

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(HttpServletRequest request) {
        Optional<Integer> escolaIdOpt = getEscolaIdDoCoord(request);

        // Se não achou escola, retorna dados gerais
        if (escolaIdOpt.isEmpty()) {
            return ResponseEntity.ok(new ResumoCoordenacaoDTO(
                escolaService.listarTodos().size(),
                professorService.listarTodos().size(),
                turmaService.listarTodos().size(),
                provaService.contarProvasAplicadas(),
                respostaService.calcularMediaGeral()
            ));
        }

        Integer escolaId = escolaIdOpt.get();

        long totalProfessores    = professorService.listarPorEscola(escolaId).size();
        long totalTurmas         = turmaService.listarPorEscola(escolaId).size();
        long totalProvasAplicadas = provaService.contarProvasAplicadasPorEscola(escolaId);
        double mediaGeral        = respostaService.calcularMediaGeralPorEscola(escolaId);

        return ResponseEntity.ok(new ResumoCoordenacaoDTO(
            1L,
            totalProfessores,
            totalTurmas,
            totalProvasAplicadas,
            mediaGeral
        ));
    }

    // ── GET /api/coordenacao/professores ──────────────────────────────────
    // Professores da escola do coordenador

    @GetMapping("/professores")
    public ResponseEntity<?> getProfessores(HttpServletRequest request) {
        Optional<Integer> escolaIdOpt = getEscolaIdDoCoord(request);

        var professores = escolaIdOpt.isPresent()
            ? professorService.listarPorEscola(escolaIdOpt.get())
            : professorService.listarTodos();

        List<Map<String, Object>> resultado = new ArrayList<>();

        for (var prof : professores) {
            long totalTurmas = turmaService
                .buscarTurmasPorProfessor(prof.getUsuario() != null
                    ? prof.getUsuario().getId() : -1).size();

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id",          prof.getId());
            item.put("nome",        prof.getUsuario() != null
                ? prof.getUsuario().getNome() : "—");
            item.put("disciplina",  prof.getDisciplina());
            item.put("escola",      prof.getEscola() != null
                ? prof.getEscola().getNome() : "—");
            item.put("totalTurmas", totalTurmas);
            item.put("status",      prof.getUsuario() != null
                && Boolean.TRUE.equals(prof.getUsuario().getAtivo())
                ? "Ativo" : "Inativo");

            resultado.add(item);
        }

        return ResponseEntity.ok(resultado);
    }

    // ── GET /api/coordenacao/turmas ───────────────────────────────────────
    // Turmas da escola com média e total de alunos

    @GetMapping("/turmas")
    public ResponseEntity<?> getTurmas(HttpServletRequest request) {
        Optional<Integer> escolaIdOpt = getEscolaIdDoCoord(request);

        var turmas = escolaIdOpt.isPresent()
            ? turmaService.listarPorEscola(escolaIdOpt.get())
            : turmaService.listarTodos();

        List<Map<String, Object>> resultado = new ArrayList<>();

        for (var turma : turmas) {
            int totalAlunos = turma.getAlunos() != null
                ? turma.getAlunos().size() : 0;

            List<Prova> provasTurma = provaService.buscarPorTurma(turma.getId());
            double mediaTurma = 0.0;
            int count = 0;

            for (Prova prova : provasTurma) {
                double media = respostaService.calcularMediaProva(prova.getId());
                if (media > 0) {
                    mediaTurma += media;
                    count++;
                }
            }
            if (count > 0) {
                mediaTurma = Math.round((mediaTurma / count) * 100.0) / 100.0;
            }

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id",           turma.getId());
            item.put("nome",         turma.getNome());
            item.put("escola",       turma.getEscola() != null
                ? turma.getEscola().getNome() : "—");
            item.put("totalAlunos",  totalAlunos);
            item.put("totalProvas",  provasTurma.size());
            item.put("media",        mediaTurma);

            resultado.add(item);
        }

        return ResponseEntity.ok(resultado);
    }

    // ── GET /api/coordenacao/provas ───────────────────────────────────────
    // Provas aplicadas na escola com status e participação

    @GetMapping("/provas")
    public ResponseEntity<?> getProvas(HttpServletRequest request) {
        Optional<Integer> escolaIdOpt = getEscolaIdDoCoord(request);

        var provas = escolaIdOpt.isPresent()
            ? provaService.buscarPorEscola(escolaIdOpt.get())
            : provaService.listarTodas();

        LocalDateTime agora = LocalDateTime.now();
        List<Map<String, Object>> resultado = new ArrayList<>();

        for (Prova prova : provas) {
            long participacao = respostaService
                .contarAlunosQueResponderam(prova.getId());
            double media = respostaService.calcularMediaProva(prova.getId());

            String status;
            if (prova.getAtivo() == null || !prova.getAtivo()) {
                status = "Inativa";
            } else if (prova.getDataInicio() != null
                    && prova.getDataInicio().isAfter(agora)) {
                status = "Agendada";
            } else if (prova.getDataFim() != null
                    && prova.getDataFim().isBefore(agora)) {
                status = "Encerrada";
            } else {
                status = "Ativa";
            }

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id",           prova.getId());
            item.put("titulo",       prova.getTitulo());
            item.put("turma",        prova.getTurma() != null
                ? prova.getTurma().getNome() : "—");
            item.put("dataInicio",   prova.getDataInicio());
            item.put("dataFim",      prova.getDataFim());
            item.put("participacao", participacao);
            item.put("media",        media);
            item.put("status",       status);

            resultado.add(item);
        }

        return ResponseEntity.ok(resultado);
    }

    // ── Utilitário privado ─────────────────────────────────────────────────

    private Optional<Integer> getEscolaIdDoCoord(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return Optional.empty();
        Integer usuarioId = (Integer) session.getAttribute("usuarioId");
        if (usuarioId == null) return Optional.empty();

        return coordenadorService.buscarPorUsuarioId(usuarioId)
            .filter(coord -> coord.getEscola() != null)
            .map(coord -> coord.getEscola().getId());
    }
}