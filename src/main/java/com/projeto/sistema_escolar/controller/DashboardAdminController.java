package com.projeto.sistema_escolar.controller;

import com.projeto.sistema_escolar.dto.ResumoAdminDTO;
import com.projeto.sistema_escolar.model.Escola;
import com.projeto.sistema_escolar.model.Prova;
import com.projeto.sistema_escolar.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class DashboardAdminController {

    private final AlunoService     alunoService;
    private final ProfessorService professorService;
    private final EscolaService    escolaService;
    private final ProvaService     provaService;
    private final RespostaService  respostaService;
    private final TurmaService     turmaService;

    public DashboardAdminController(AlunoService alunoService,
                                    ProfessorService professorService,
                                    EscolaService escolaService,
                                    ProvaService provaService,
                                    RespostaService respostaService,
                                    TurmaService turmaService) {
        this.alunoService     = alunoService;
        this.professorService = professorService;
        this.escolaService    = escolaService;
        this.provaService     = provaService;
        this.respostaService  = respostaService;
        this.turmaService     = turmaService;
    }

    // ── GET /api/admin/dashboard ───────────────────────────────────────────
    // Retorna os stat-cards do painel administrativo.
    // Campos esperados pelo admin.js:
    //   totalAlunos, totalProvas, totalProfessores, mediaGeral

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard() {
        long totalAlunos      = alunoService.listarTodos().size();
        long totalProfessores = professorService.listarTodos().size();
        long totalEscolas     = escolaService.listarTodos().size();
        long totalProvas      = provaService.contarProvasAplicadas();
        double mediaGeral     = respostaService.calcularMediaGeral();

        return ResponseEntity.ok(new ResumoAdminDTO(
            totalAlunos,
            totalProfessores,
            totalEscolas,
            totalProvas,
            mediaGeral
        ));
    }

    // ── GET /api/admin/escolas ─────────────────────────────────────────────
    // Lista de escolas com totais e médias para tabela do admin.

    @GetMapping("/escolas")
    public ResponseEntity<?> getEscolas() {
        List<Escola> escolas  = escolaService.listarTodos();
        List<Map<String, Object>> resultado = new ArrayList<>();

        for (Escola escola : escolas) {
            long totalProfessores = professorService
                .listarPorEscola(escola.getId()).size();
            long totalTurmas      = turmaService
                .listarPorEscola(escola.getId()).size();
            long totalAlunos      = alunoService
                .listarPorEscola(escola.getId()).size();
            long provasAplicadas  = provaService
                .contarProvasAplicadasPorEscola(escola.getId());
            double media          = respostaService
                .calcularMediaGeralPorEscola(escola.getId());

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id",               escola.getId());
            item.put("nome",             escola.getNome());
            item.put("cidade",           escola.getCidade());
            item.put("totalProfessores", totalProfessores);
            item.put("totalTurmas",      totalTurmas);
            item.put("totalAlunos",      totalAlunos);
            item.put("provasAplicadas",  provasAplicadas);
            item.put("mediaGeral",       media);

            resultado.add(item);
        }

        return ResponseEntity.ok(resultado);
    }

    // ── GET /api/admin/provas/recentes ─────────────────────────────────────
    // Últimas 10 provas cadastradas no sistema.

    @GetMapping("/provas/recentes")
    public ResponseEntity<?> getProvasRecentes() {
        List<Prova> todasProvas = provaService.listarTodas();
        LocalDateTime agora     = LocalDateTime.now();

        List<Map<String, Object>> resultado = todasProvas.stream()
            .sorted((a, b) -> {
                if (a.getCriadoEm() == null) return 1;
                if (b.getCriadoEm() == null) return -1;
                return b.getCriadoEm().compareTo(a.getCriadoEm());
            })
            .limit(10)
            .map(prova -> {
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
                item.put("id",         prova.getId());
                item.put("titulo",     prova.getTitulo());
                item.put("turma",      prova.getTurma() != null
                    ? prova.getTurma().getNome() : "—");
                item.put("escola",     prova.getTurma() != null
                    && prova.getTurma().getEscola() != null
                    ? prova.getTurma().getEscola().getNome() : "—");
                item.put("dataInicio", prova.getDataInicio());
                item.put("status",     status);
                return item;
            })
            .toList();

        return ResponseEntity.ok(resultado);
    }

    // ── GET /api/admin/relatorio/desempenho ────────────────────────────────
    // Desempenho geral por escola para a tela de relatórios.

    @GetMapping("/relatorio/desempenho")
    public ResponseEntity<?> getRelatorioDesempenho() {
        List<Escola> escolas  = escolaService.listarTodos();
        List<Map<String, Object>> resultado = new ArrayList<>();

        for (Escola escola : escolas) {
            double media          = respostaService
                .calcularMediaGeralPorEscola(escola.getId());
            long provasAplicadas  = provaService
                .contarProvasAplicadasPorEscola(escola.getId());
            long totalAlunos      = alunoService
                .listarPorEscola(escola.getId()).size();

            String desempenho;
            if (media >= 8.0)      desempenho = "Excelente";
            else if (media >= 6.0) desempenho = "Regular";
            else if (media > 0)    desempenho = "Abaixo da média";
            else                   desempenho = "Sem dados";

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("escola",          escola.getNome());
            item.put("cidade",          escola.getCidade());
            item.put("totalAlunos",     totalAlunos);
            item.put("provasAplicadas", provasAplicadas);
            item.put("mediaGeral",      media);
            item.put("desempenho",      desempenho);

            resultado.add(item);
        }

        return ResponseEntity.ok(resultado);
    }
}