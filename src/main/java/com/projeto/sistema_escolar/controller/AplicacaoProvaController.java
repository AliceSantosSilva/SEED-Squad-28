package com.projeto.sistema_escolar.controller;

import com.projeto.sistema_escolar.dto.IncidenteProvaRequestDTO;
import com.projeto.sistema_escolar.dto.ResultadoSubmissaoDTO;
import com.projeto.sistema_escolar.dto.SubmissaoProvaDTO;
import com.projeto.sistema_escolar.model.*;
import com.projeto.sistema_escolar.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/aplicacao")
public class AplicacaoProvaController {

    private final UsuarioService     usuarioService;
    private final ProvaService       provaService;
    private final QuestaoService     questaoService;
    private final AlternativaService alternativaService;
    private final RespostaService    respostaService;
    private final IncidenteProvaService incidenteProvaService;  // 🔧 NOVO

    public AplicacaoProvaController(UsuarioService usuarioService,
                                     ProvaService provaService,
                                     QuestaoService questaoService,
                                     AlternativaService alternativaService,
                                     RespostaService respostaService,
                                     IncidenteProvaService incidenteProvaService) {
        this.usuarioService     = usuarioService;
        this.provaService       = provaService;
        this.questaoService     = questaoService;
        this.alternativaService = alternativaService;
        this.respostaService    = respostaService;
        this.incidenteProvaService = incidenteProvaService;  // 🔧 NOVO
    }

    // ── Utilitário ────────────────────────────────────────────────────────

    private Optional<Usuario> getUsuarioLogado(Authentication auth) {
        if (auth == null) return Optional.empty();
        Integer usuarioId = (Integer) auth.getDetails();
        if (usuarioId == null) return Optional.empty();
        return usuarioService.buscarPorId(usuarioId);
    }

    // ── GET /api/aplicacao/provas/{provaId} ───────────────────────────────

    @GetMapping("/provas/{provaId}")
    public ResponseEntity<?> getProvaParaResponder(@PathVariable Integer provaId,
                                                    Authentication auth) {
        Optional<Usuario> usuarioOpt = getUsuarioLogado(auth);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("erro", "Não autenticado"));
        }

        Usuario aluno = usuarioOpt.get();

        Optional<Prova> provaOpt = provaService.buscarPorId(provaId);
        if (provaOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("erro", "Prova não encontrada"));
        }

        Prova         prova = provaOpt.get();
        LocalDateTime agora = LocalDateTime.now();

        if (prova.getAtivo() == null || !prova.getAtivo()) {
            return ResponseEntity.status(403).body(Map.of("erro", "Esta prova não está disponível"));
        }
        if (prova.getDataInicio() != null && prova.getDataInicio().isAfter(agora)) {
            return ResponseEntity.status(403).body(Map.of("erro", "Esta prova ainda não começou"));
        }
        if (prova.getDataFim() != null && prova.getDataFim().isBefore(agora)) {
            return ResponseEntity.status(403).body(Map.of("erro", "O prazo desta prova já encerrou"));
        }
        if (prova.getTurma() != null && aluno.getTurma() != null
                && !prova.getTurma().getId().equals(aluno.getTurma().getId())) {
            return ResponseEntity.status(403).body(Map.of("erro", "Você não pertence à turma desta prova"));
        }
        if (respostaService.jaRespondeu(aluno.getId(), provaId)) {
            return ResponseEntity.status(409).body(Map.of("erro", "Você já respondeu esta prova"));
        }

        List<Questao> questoes = prova.getQuestoes();
        if (prova.isGerarVariacoes()) {
            questoes = new ArrayList<>(questoes);
            Collections.shuffle(questoes);
        }

        List<Map<String, Object>> questoesFormatadas = new ArrayList<>();

        for (Questao questao : questoes) {
            List<Alternativa> alternativas = alternativaService.buscarPorQuestao(questao.getId());

            if (prova.isGerarVariacoes()) {
                alternativas = new ArrayList<>(alternativas);
                Collections.shuffle(alternativas);
            }

            List<Map<String, Object>> altsFormatadas = alternativas.stream()
                .map(a -> {
                    Map<String, Object> alt = new LinkedHashMap<>();
                    alt.put("id",    a.getId());
                    alt.put("texto", a.getTexto());
                    return alt;
                }).toList();

            Map<String, Object> questaoFormatada = new LinkedHashMap<>();
            questaoFormatada.put("id",          questao.getId());
            questaoFormatada.put("enunciado",    questao.getEnunciado());
            questaoFormatada.put("dificuldade",  questao.getDificuldade());
            questaoFormatada.put("alternativas", altsFormatadas);
            questoesFormatadas.add(questaoFormatada);
        }

        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("provaId",        prova.getId());
        resposta.put("titulo",         prova.getTitulo());
        resposta.put("duracaoMinutos", prova.getDuracaoMinutos());
        resposta.put("totalQuestoes",  questoesFormatadas.size());
        resposta.put("questoes",       questoesFormatadas);

        return ResponseEntity.ok(resposta);
    }

    // ── POST /api/aplicacao/submeter ──────────────────────────────────────

    @PostMapping("/submeter")
    public ResponseEntity<?> submeterProva(@RequestBody SubmissaoProvaDTO submissao,
                                            Authentication auth) {
        Optional<Usuario> usuarioOpt = getUsuarioLogado(auth);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("erro", "Não autenticado"));
        }

        Usuario aluno = usuarioOpt.get();

        Optional<Prova> provaOpt = provaService.buscarPorId(submissao.getProvaId());
        if (provaOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("erro", "Prova não encontrada"));
        }

        Prova         prova = provaOpt.get();
        LocalDateTime agora = LocalDateTime.now();

        if (respostaService.jaRespondeu(aluno.getId(), prova.getId())) {
            return ResponseEntity.status(409).body(Map.of("erro", "Você já respondeu esta prova"));
        }
        if (prova.getDataFim() != null && prova.getDataFim().isBefore(agora)) {
            return ResponseEntity.status(403).body(Map.of("erro", "O prazo desta prova já encerrou"));
        }

        long acertos = 0;

        for (SubmissaoProvaDTO.RespostaItemDTO item : submissao.getRespostas()) {
            Optional<Questao>     questaoOpt     = questaoService.buscarPorId(item.getQuestaoId());
            Optional<Alternativa> alternativaOpt = alternativaService.buscarPorId(item.getAlternativaId());

            if (questaoOpt.isEmpty() || alternativaOpt.isEmpty()) continue;

            Optional<Alternativa> corretaOpt =
                alternativaService.buscarCorretaPorQuestao(item.getQuestaoId());

            boolean correta = corretaOpt.isPresent()
                && corretaOpt.get().getId().equals(item.getAlternativaId());

            if (correta) acertos++;

            Resposta resposta = new Resposta();
            resposta.setAluno(aluno);
            resposta.setProva(prova);
            resposta.setQuestao(questaoOpt.get());
            resposta.setAlternativa(alternativaOpt.get());
            resposta.setCorreta(correta);
            resposta.setDataHoraResposta(agora);

            respostaService.salvar(resposta);
        }

        long   totalQuestoes = submissao.getRespostas().size();
        double notaMinima    = prova.getNotaMinimaAprovacao() != null
            ? prova.getNotaMinimaAprovacao() : 5.0;

        // 🔧 NOVO: Cria resultado e adiciona total de incidentes
        ResultadoSubmissaoDTO resultado = new ResultadoSubmissaoDTO(
            prova.getId(), prova.getTitulo(), totalQuestoes, acertos, notaMinima
        );
        resultado.setTotalIncidentes(incidenteProvaService.contar(aluno.getId(), prova.getId()));
        return ResponseEntity.ok(resultado);
    }

    // ── GET /api/aplicacao/provas/{provaId}/resultado ─────────────────────

    @GetMapping("/provas/{provaId}/resultado")
    public ResponseEntity<?> getResultado(@PathVariable Integer provaId,
                                           Authentication auth) {
        Optional<Usuario> usuarioOpt = getUsuarioLogado(auth);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("erro", "Não autenticado"));
        }

        Usuario aluno = usuarioOpt.get();

        if (!respostaService.jaRespondeu(aluno.getId(), provaId)) {
            return ResponseEntity.status(404)
                .body(Map.of("erro", "Você ainda não respondeu esta prova"));
        }

        Optional<Prova> provaOpt = provaService.buscarPorId(provaId);
        if (provaOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("erro", "Prova não encontrada"));
        }

        Prova  prova         = provaOpt.get();
        long   acertos       = respostaService.contarAcertos(aluno.getId(), provaId);
        long   totalRespostas = respostaService.contarTotalRespostas(aluno.getId(), provaId);
        double notaMinima    = prova.getNotaMinimaAprovacao() != null
            ? prova.getNotaMinimaAprovacao() : 5.0;

        // 🔧 NOVO: Cria resultado e adiciona total de incidentes
        ResultadoSubmissaoDTO resultado = new ResultadoSubmissaoDTO(
            prova.getId(), prova.getTitulo(), totalRespostas, acertos, notaMinima
        );
        resultado.setTotalIncidentes(incidenteProvaService.contar(aluno.getId(), provaId));
        return ResponseEntity.ok(resultado);
    }

    // ── POST /api/aplicacao/provas/{provaId}/incidente ────────────────────
    // 🔧 NOVO ENDPOINT

    @PostMapping("/provas/{provaId}/incidente")
    public ResponseEntity<?> registrarIncidente(@PathVariable Integer provaId,
                                                 @RequestBody IncidenteProvaRequestDTO dto,
                                                 Authentication auth) {
        Optional<Usuario> usuarioOpt = getUsuarioLogado(auth);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("erro", "Não autenticado"));
        }

        Optional<Prova> provaOpt = provaService.buscarPorId(provaId);
        if (provaOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("erro", "Prova não encontrada"));
        }

        IncidenteProva incidente = new IncidenteProva();
        incidente.setAluno(usuarioOpt.get());
        incidente.setProva(provaOpt.get());
        incidente.setTipo(dto.getTipo() != null ? dto.getTipo() : "SAIU_DA_ABA");
        incidente.setMensagem(dto.getMensagem());
        incidenteProvaService.registrar(incidente);

        long total = incidenteProvaService.contar(usuarioOpt.get().getId(), provaId);
        return ResponseEntity.ok(Map.of("totalIncidentes", total));
    }
}