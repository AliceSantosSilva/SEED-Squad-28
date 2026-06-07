package com.projeto.sistema_escolar.controller;

import com.projeto.sistema_escolar.dto.ResumoAlunoDTO;
import com.projeto.sistema_escolar.dto.ResultadoProvaDTO;
import com.projeto.sistema_escolar.model.Prova;
import com.projeto.sistema_escolar.model.Usuario;
import com.projeto.sistema_escolar.service.ProvaService;
import com.projeto.sistema_escolar.service.RespostaService;
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
@RequestMapping("/api/aluno")
public class DashboardAlunoController {

    private final UsuarioService usuarioService;
    private final ProvaService provaService;
    private final RespostaService respostaService;

    public DashboardAlunoController(UsuarioService usuarioService,
                                    ProvaService provaService,
                                    RespostaService respostaService) {
        this.usuarioService = usuarioService;
        this.provaService = provaService;
        this.respostaService = respostaService;
    }

    // ── Utilitário: pega usuário logado da sessão ──────────────────────────

    private Optional<Usuario> getUsuarioLogado(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return Optional.empty();
        Integer usuarioId = (Integer) session.getAttribute("usuarioId");
        if (usuarioId == null) return Optional.empty();
        return usuarioService.buscarPorId(usuarioId);
    }

    // ── GET /api/aluno/dashboard ───────────────────────────────────────────
    // Retorna estatísticas resumidas para os stat-cards do painel

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(HttpServletRequest request) {
        Optional<Usuario> usuarioOpt = getUsuarioLogado(request);

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(401)
                .body(Map.of("erro", "Não autenticado"));
        }

        Usuario usuario = usuarioOpt.get();

        if (usuario.getTurma() == null) {
            return ResponseEntity.ok(new ResumoAlunoDTO(0, 0, 0, 0.0));
        }

        Integer turmaId  = usuario.getTurma().getId();
        Integer alunoId  = usuario.getId();

        // Todas as provas da turma
        List<Prova> todasProvas = provaService.buscarPorTurma(turmaId);

        // IDs das provas já respondidas
        List<Integer> respondidas = respostaService.buscarIdsProvasRespondidas(alunoId);

        int totalProvas     = todasProvas.size();
        int provasRealizadas = respondidas.size();
        int provasPendentes = (int) todasProvas.stream()
            .filter(p -> !respondidas.contains(p.getId()))
            .filter(p -> p.getAtivo() != null && p.getAtivo())
            .filter(p -> p.getDataFim() == null || p.getDataFim().isAfter(LocalDateTime.now()))
            .count();

        // Calcula média geral das provas realizadas
        double mediaGeral = 0.0;
        if (!respondidas.isEmpty()) {
            double somaNotas = 0.0;
            int count = 0;
            for (Prova prova : todasProvas) {
                if (respondidas.contains(prova.getId())) {
                    long acertos = respostaService.contarAcertos(alunoId, prova.getId());
                    long total   = respostaService.contarTotalRespostas(alunoId, prova.getId());
                    if (total > 0) {
                        somaNotas += (acertos * 10.0 / total);
                        count++;
                    }
                }
            }
            if (count > 0) {
                mediaGeral = Math.round((somaNotas / count) * 100.0) / 100.0;
            }
        }

        return ResponseEntity.ok(
            new ResumoAlunoDTO(provasPendentes, provasRealizadas, totalProvas, mediaGeral)
        );
    }

    // ── GET /api/aluno/provas/pendentes ────────────────────────────────────
    // Provas disponíveis que o aluno ainda não respondeu

    @GetMapping("/provas/pendentes")
    public ResponseEntity<?> getProvasPendentes(HttpServletRequest request) {
        Optional<Usuario> usuarioOpt = getUsuarioLogado(request);

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(401)
                .body(Map.of("erro", "Não autenticado"));
        }

        Usuario usuario = usuarioOpt.get();

        if (usuario.getTurma() == null) {
            return ResponseEntity.ok(List.of());
        }

        List<Prova> pendentes = provaService.buscarProvasPendentes(
            usuario.getTurma().getId(),
            usuario.getId()
        );

        return ResponseEntity.ok(pendentes);
    }

    // ── GET /api/aluno/provas/realizadas ───────────────────────────────────
    // Provas já respondidas pelo aluno com nota calculada

    @GetMapping("/provas/realizadas")
    public ResponseEntity<?> getProvasRealizadas(HttpServletRequest request) {
        Optional<Usuario> usuarioOpt = getUsuarioLogado(request);

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(401)
                .body(Map.of("erro", "Não autenticado"));
        }

        Usuario usuario = usuarioOpt.get();
        Integer alunoId = usuario.getId();

        if (usuario.getTurma() == null) {
            return ResponseEntity.ok(List.of());
        }

        List<Integer> respondidas = respostaService.buscarIdsProvasRespondidas(alunoId);
        List<Prova> todasProvasTurma = provaService.buscarPorTurma(usuario.getTurma().getId());

        List<ResultadoProvaDTO> resultados = new ArrayList<>();

        for (Prova prova : todasProvasTurma) {
            if (!respondidas.contains(prova.getId())) continue;

            long acertos = respostaService.contarAcertos(alunoId, prova.getId());
            long total   = respostaService.contarTotalRespostas(alunoId, prova.getId());

            // Data da última resposta nessa prova
            var respostas = respostaService.buscarPorAlunoEProva(alunoId, prova.getId());
            LocalDateTime dataRealizado = respostas.stream()
                .map(r -> r.getDataHoraResposta())
                .max(LocalDateTime::compareTo)
                .orElse(null);

            double notaMinima = prova.getNotaMinimaAprovacao() != null
                ? prova.getNotaMinimaAprovacao() : 5.0;

            resultados.add(new ResultadoProvaDTO(
                prova.getId(),
                prova.getTitulo(),
                dataRealizado,
                total,
                acertos,
                notaMinima
            ));
        }

        return ResponseEntity.ok(resultados);
    }
}