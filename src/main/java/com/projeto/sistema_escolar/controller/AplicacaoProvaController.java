package com.projeto.sistema_escolar.controller;

import com.projeto.sistema_escolar.dto.ResultadoSubmissaoDTO;
import com.projeto.sistema_escolar.dto.SubmissaoProvaDTO;
import com.projeto.sistema_escolar.model.*;
import com.projeto.sistema_escolar.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/aplicacao")
public class AplicacaoProvaController {

    private final UsuarioService usuarioService;
    private final ProvaService provaService;
    private final QuestaoService questaoService;
    private final AlternativaService alternativaService;
    private final RespostaService respostaService;

    public AplicacaoProvaController(UsuarioService usuarioService,
                                     ProvaService provaService,
                                     QuestaoService questaoService,
                                     AlternativaService alternativaService,
                                     RespostaService respostaService) {
        this.usuarioService      = usuarioService;
        this.provaService        = provaService;
        this.questaoService      = questaoService;
        this.alternativaService  = alternativaService;
        this.respostaService     = respostaService;
    }

    // ── Utilitário: pega usuário logado da sessão ──────────────────────────

    private Optional<Usuario> getUsuarioLogado(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return Optional.empty();
        Integer usuarioId = (Integer) session.getAttribute("usuarioId");
        if (usuarioId == null) return Optional.empty();
        return usuarioService.buscarPorId(usuarioId);
    }

    // ── GET /api/aplicacao/provas/{provaId} ────────────────────────────────
    // Retorna a prova com questões e alternativas embaralhadas

    @GetMapping("/provas/{provaId}")
    public ResponseEntity<?> getProvaParaResponder(@PathVariable Integer provaId,
                                                    HttpServletRequest request) {
        Optional<Usuario> usuarioOpt = getUsuarioLogado(request);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(401)
                .body(Map.of("erro", "Não autenticado"));
        }

        Usuario aluno = usuarioOpt.get();

        Optional<Prova> provaOpt = provaService.buscarPorId(provaId);
        if (provaOpt.isEmpty()) {
            return ResponseEntity.status(404)
                .body(Map.of("erro", "Prova não encontrada"));
        }

        Prova prova = provaOpt.get();

        // Valida se a prova está ativa
        if (prova.getAtivo() == null || !prova.getAtivo()) {
            return ResponseEntity.status(403)
                .body(Map.of("erro", "Esta prova não está disponível"));
        }

        // Valida período
        LocalDateTime agora = LocalDateTime.now();
        if (prova.getDataInicio() != null && prova.getDataInicio().isAfter(agora)) {
            return ResponseEntity.status(403)
                .body(Map.of("erro", "Esta prova ainda não começou"));
        }
        if (prova.getDataFim() != null && prova.getDataFim().isBefore(agora)) {
            return ResponseEntity.status(403)
                .body(Map.of("erro", "O prazo desta prova já encerrou"));
        }

        // Valida se o aluno pertence à turma da prova
        if (prova.getTurma() != null && aluno.getTurma() != null) {
            if (!prova.getTurma().getId().equals(aluno.getTurma().getId())) {
                return ResponseEntity.status(403)
                    .body(Map.of("erro", "Você não pertence à turma desta prova"));
            }
        }

        // Valida se o aluno já respondeu
        if (respostaService.jaRespondeu(aluno.getId(), provaId)) {
            return ResponseEntity.status(409)
                .body(Map.of("erro", "Você já respondeu esta prova"));
        }

        // Monta questões com alternativas
        // Remove a flag "correta" das alternativas para não entregar a resposta
        List<Map<String, Object>> questoesFormatadas = new ArrayList<>();

        List<Questao> questoes = prova.getQuestoes();
        if (prova.isGerarVariacoes()) {
            questoes = new ArrayList<>(questoes);
            Collections.shuffle(questoes);
        }

        for (Questao questao : questoes) {
            List<Alternativa> alternativas =
                alternativaService.buscarPorQuestao(questao.getId());

            // Embaralha alternativas se variações ativas
            if (prova.isGerarVariacoes()) {
                alternativas = new ArrayList<>(alternativas);
                Collections.shuffle(alternativas);
            }

            // Oculta qual é a correta
            List<Map<String, Object>> alternativasFormatadas = alternativas.stream()
                .map(a -> {
                    Map<String, Object> alt = new LinkedHashMap<>();
                    alt.put("id", a.getId());
                    alt.put("texto", a.getTexto());
                    return alt;
                }).toList();

            Map<String, Object> questaoFormatada = new LinkedHashMap<>();
            questaoFormatada.put("id",           questao.getId());
            questaoFormatada.put("enunciado",     questao.getEnunciado());
            questaoFormatada.put("dificuldade",   questao.getDificuldade());
            questaoFormatada.put("alternativas",  alternativasFormatadas);

            questoesFormatadas.add(questaoFormatada);
        }

        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("provaId",          prova.getId());
        resposta.put("titulo",           prova.getTitulo());
        resposta.put("duracaoMinutos",   prova.getDuracaoMinutos());
        resposta.put("totalQuestoes",    questoesFormatadas.size());
        resposta.put("questoes",         questoesFormatadas);

        return ResponseEntity.ok(resposta);
    }

    // ── POST /api/aplicacao/submeter ───────────────────────────────────────
    // Aluno envia todas as respostas de uma vez, sistema corrige e retorna nota

    @PostMapping("/submeter")
    public ResponseEntity<?> submeterProva(@RequestBody SubmissaoProvaDTO submissao,
                                            HttpServletRequest request) {
        Optional<Usuario> usuarioOpt = getUsuarioLogado(request);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(401)
                .body(Map.of("erro", "Não autenticado"));
        }

        Usuario aluno = usuarioOpt.get();

        Optional<Prova> provaOpt = provaService.buscarPorId(submissao.getProvaId());
        if (provaOpt.isEmpty()) {
            return ResponseEntity.status(404)
                .body(Map.of("erro", "Prova não encontrada"));
        }

        Prova prova = provaOpt.get();

        // Bloqueia dupla submissão
        if (respostaService.jaRespondeu(aluno.getId(), prova.getId())) {
            return ResponseEntity.status(409)
                .body(Map.of("erro", "Você já respondeu esta prova"));
        }

        // Valida período
        LocalDateTime agora = LocalDateTime.now();
        if (prova.getDataFim() != null && prova.getDataFim().isBefore(agora)) {
            return ResponseEntity.status(403)
                .body(Map.of("erro", "O prazo desta prova já encerrou"));
        }

        // Processa e salva cada resposta
        long acertos = 0;

        for (SubmissaoProvaDTO.RespostaItemDTO item : submissao.getRespostas()) {

            Optional<Questao> questaoOpt =
                questaoService.buscarPorId(item.getQuestaoId());
            if (questaoOpt.isEmpty()) continue;

            Optional<Alternativa> alternativaOpt =
                alternativaService.buscarPorId(item.getAlternativaId());
            if (alternativaOpt.isEmpty()) continue;

            // Verifica se a alternativa escolhida é a correta
            Optional<Alternativa> corretaOpt =
                alternativaService.buscarCorretaPorQuestao(item.getQuestaoId());

            boolean correta = corretaOpt.isPresent()
                && corretaOpt.get().getId().equals(item.getAlternativaId());

            if (correta) acertos++;

            // Salva a resposta no banco
            Resposta resposta = new Resposta();
            resposta.setAluno(aluno);
            resposta.setProva(prova);
            resposta.setQuestao(questaoOpt.get());
            resposta.setAlternativa(alternativaOpt.get());
            resposta.setCorreta(correta);
            resposta.setDataHoraResposta(agora);

            respostaService.salvar(resposta);
        }

        long totalQuestoes = submissao.getRespostas().size();
        double notaMinima  = prova.getNotaMinimaAprovacao() != null
            ? prova.getNotaMinimaAprovacao() : 5.0;

        return ResponseEntity.ok(new ResultadoSubmissaoDTO(
            prova.getId(),
            prova.getTitulo(),
            totalQuestoes,
            acertos,
            notaMinima
        ));
    }

    // ── GET /api/aplicacao/provas/{provaId}/resultado ──────────────────────
    // Retorna o resultado de uma prova já realizada pelo aluno

    @GetMapping("/provas/{provaId}/resultado")
    public ResponseEntity<?> getResultado(@PathVariable Integer provaId,
                                           HttpServletRequest request) {
        Optional<Usuario> usuarioOpt = getUsuarioLogado(request);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(401)
                .body(Map.of("erro", "Não autenticado"));
        }

        Usuario aluno = usuarioOpt.get();

        if (!respostaService.jaRespondeu(aluno.getId(), provaId)) {
            return ResponseEntity.status(404)
                .body(Map.of("erro", "Você ainda não respondeu esta prova"));
        }

        Optional<Prova> provaOpt = provaService.buscarPorId(provaId);
        if (provaOpt.isEmpty()) {
            return ResponseEntity.status(404)
                .body(Map.of("erro", "Prova não encontrada"));
        }

        Prova prova = provaOpt.get();

        long acertos       = respostaService.contarAcertos(aluno.getId(), provaId);
        long totalRespostas = respostaService.contarTotalRespostas(aluno.getId(), provaId);
        double notaMinima  = prova.getNotaMinimaAprovacao() != null
            ? prova.getNotaMinimaAprovacao() : 5.0;

        return ResponseEntity.ok(new ResultadoSubmissaoDTO(
            prova.getId(),
            prova.getTitulo(),
            totalRespostas,
            acertos,
            notaMinima
        ));
    }
}