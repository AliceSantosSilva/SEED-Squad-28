package com.projeto.sistema_escolar.controller;

import com.projeto.sistema_escolar.dto.ResumoAlunoDTO;
import com.projeto.sistema_escolar.dto.ResultadoProvaDTO;
import com.projeto.sistema_escolar.model.Prova;
import com.projeto.sistema_escolar.model.Usuario;
import com.projeto.sistema_escolar.service.ProvaService;
import com.projeto.sistema_escolar.service.RespostaService;
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
@RequestMapping("/api/aluno")
public class DashboardAlunoController {

    private final UsuarioService  usuarioService;
    private final ProvaService    provaService;
    private final RespostaService respostaService;

    public DashboardAlunoController(UsuarioService usuarioService,
                                    ProvaService provaService,
                                    RespostaService respostaService) {
        this.usuarioService  = usuarioService;
        this.provaService    = provaService;
        this.respostaService = respostaService;
    }

    // ── Utilitário ────────────────────────────────────────────────────────

    private Optional<Usuario> getUsuarioLogado(Authentication auth) {
        if (auth == null) return Optional.empty();
        Integer usuarioId = (Integer) auth.getDetails();
        if (usuarioId == null) return Optional.empty();
        return usuarioService.buscarPorId(usuarioId);
    }

    // ── GET /api/aluno/dashboard ──────────────────────────────────────────

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(Authentication auth) {
        Optional<Usuario> usuarioOpt = getUsuarioLogado(auth);

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(401)
                .body(Map.of("erro", "Não autenticado"));
        }

        Usuario usuario = usuarioOpt.get();

        if (usuario.getTurma() == null) {
            return ResponseEntity.ok(new ResumoAlunoDTO(0, 0, 0, 0.0));
        }

        Integer turmaId = usuario.getTurma().getId();
        Integer alunoId = usuario.getId();

        List<Prova> todasProvas  = provaService.buscarPorTurma(turmaId);
        List<Integer> respondidas = respostaService.buscarIdsProvasRespondidas(alunoId);

        int totalProvas      = todasProvas.size();
        int provasRealizadas = respondidas.size();
        int provasPendentes  = (int) todasProvas.stream()
            .filter(p -> !respondidas.contains(p.getId()))
            .filter(p -> p.getAtivo() != null && p.getAtivo())
            .filter(p -> p.getDataFim() == null || p.getDataFim().isAfter(LocalDateTime.now()))
            .count();

        double mediaGeral = 0.0;
        if (!respondidas.isEmpty()) {
            double soma  = 0.0;
            int    count = 0;
            for (Prova prova : todasProvas) {
                if (!respondidas.contains(prova.getId())) continue;
                long acertos = respostaService.contarAcertos(alunoId, prova.getId());
                long total   = respostaService.contarTotalRespostas(alunoId, prova.getId());
                if (total > 0) {
                    soma += (acertos * 10.0 / total);
                    count++;
                }
            }
            if (count > 0) {
                mediaGeral = Math.round((soma / count) * 100.0) / 100.0;
            }
        }

        return ResponseEntity.ok(
            new ResumoAlunoDTO(provasPendentes, provasRealizadas, totalProvas, mediaGeral)
        );
    }

    // ── GET /api/aluno/provas/pendentes ───────────────────────────────────

    @GetMapping("/provas/pendentes")
    public ResponseEntity<?> getProvasPendentes(Authentication auth) {
        Optional<Usuario> usuarioOpt = getUsuarioLogado(auth);

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

    // ── GET /api/aluno/provas/realizadas ──────────────────────────────────

    @GetMapping("/provas/realizadas")
    public ResponseEntity<?> getProvasRealizadas(Authentication auth) {
        Optional<Usuario> usuarioOpt = getUsuarioLogado(auth);

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(401)
                .body(Map.of("erro", "Não autenticado"));
        }

        Usuario  usuario = usuarioOpt.get();
        Integer  alunoId = usuario.getId();

        if (usuario.getTurma() == null) {
            return ResponseEntity.ok(List.of());
        }

        List<Integer> respondidas     = respostaService.buscarIdsProvasRespondidas(alunoId);
        List<Prova>   todasProvasTurma = provaService.buscarPorTurma(usuario.getTurma().getId());

        List<ResultadoProvaDTO> resultados = new ArrayList<>();

        for (Prova prova : todasProvasTurma) {
            if (!respondidas.contains(prova.getId())) continue;

            long acertos = respostaService.contarAcertos(alunoId, prova.getId());
            long total   = respostaService.contarTotalRespostas(alunoId, prova.getId());

            LocalDateTime dataRealizado = respostaService
                .buscarPorAlunoEProva(alunoId, prova.getId()).stream()
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