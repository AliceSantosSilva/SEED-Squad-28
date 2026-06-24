package com.projeto.sistema_escolar.service;

import com.projeto.sistema_escolar.dto.ResultadoProvaDTO;
import com.projeto.sistema_escolar.dto.SubmissaoProvaDTO;
import com.projeto.sistema_escolar.model.*;
import com.projeto.sistema_escolar.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class RespostaService {

    private final RespostaRepository repository;
    private final ProvaRepository provaRepository;
    private final QuestaoRepository questaoRepository;
    private final AlternativaRepository alternativaRepository;
    private final UsuarioRepository usuarioRepository;

    public RespostaService(RespostaRepository repository, ProvaRepository provaRepository,
                           QuestaoRepository questaoRepository, AlternativaRepository alternativaRepository,
                           UsuarioRepository usuarioRepository) {
        this.repository = repository;
        this.provaRepository = provaRepository;
        this.questaoRepository = questaoRepository;
        this.alternativaRepository = alternativaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public ResultadoProvaDTO corrigirProva(SubmissaoProvaDTO submissao, Integer alunoLogadoId) {

        if (jaRespondeu(alunoLogadoId, submissao.getProvaId())) {
            throw new RuntimeException("Este aluno já submeteu as respostas para esta prova!");
        }

        Prova prova = provaRepository.findById(submissao.getProvaId())
                .orElseThrow(() -> new RuntimeException("Prova não encontrada"));

        Usuario aluno = usuarioRepository.findById(alunoLogadoId)
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado"));


        if (prova.getTurma() == null || aluno.getTurma() == null || !prova.getTurma().getId().equals(aluno.getTurma().getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Acesso negado: Você não está matriculado na turma desta prova!"
            );
        }

        long acertos = 0;

        for (SubmissaoProvaDTO.RespostaItemDTO item : submissao.getRespostas()) {

            Questao questao = questaoRepository.findById(item.getQuestaoId())
                    .orElseThrow(() -> new RuntimeException("Questão não encontrada"));

            Alternativa alternativaEscolhida = alternativaRepository.findById(item.getAlternativaId())
                    .orElseThrow(() -> new RuntimeException("Alternativa não encontrada"));

            boolean acertou = alternativaEscolhida.isCorreta();
            if (acertou) {
                acertos++;
            }

            Resposta resposta = new Resposta();
            resposta.setAluno(aluno);
            resposta.setProva(prova);
            resposta.setQuestao(questao);
            resposta.setAlternativa(alternativaEscolhida);
            resposta.setCorreta(acertou);

            repository.save(resposta);
        }

        return new ResultadoProvaDTO(
                prova.getId(),
                prova.getTitulo(),
                java.time.LocalDateTime.now(),
                prova.getQuestoes().size(),
                acertos,
                prova.getNotaMinimaAprovacao()
        );
    }

    public List<Resposta> listarTodas() {
        return repository.findAll();
    }

    public Optional<Resposta> buscarPorId(Integer id) {
        return repository.findById(id);
    }

    public List<Resposta> buscarPorAluno(Integer alunoId) {
        return repository.findByAlunoId(alunoId);
    }

    public List<Resposta> buscarPorProva(Integer provaId) {
        return repository.findByProvaId(provaId);
    }

    public List<Resposta> buscarPorAlunoEProva(Integer alunoId, Integer provaId) {
        return repository.findByAlunoIdAndProvaId(alunoId, provaId);
    }

    public boolean jaRespondeu(Integer alunoId, Integer provaId) {
        return repository.existsByAlunoIdAndProvaId(alunoId, provaId);
    }

    public List<Integer> buscarIdsProvasRespondidas(Integer alunoId) {
        return repository.findProvasRespondidasIdsByAluno(alunoId);
    }

    public long contarAcertos(Integer alunoId, Integer provaId) {
        return repository.countRespostasCorretas(alunoId, provaId);
    }

    public long contarTotalRespostas(Integer alunoId, Integer provaId) {
        return repository.countRespostasTotal(alunoId, provaId);
    }

    public Resposta salvar(Resposta resposta) {
        return repository.save(resposta);
    }

    public void deletar(Integer id) {
        repository.deleteById(id);
    }

    public boolean existePorId(Integer id) {
        return repository.existsById(id);
    }

    public long contarAlunosQueResponderam(Integer provaId) {
        return repository.countAlunosQueResponderamProva(provaId);
    }

    public double calcularMediaProva(Integer provaId) {
        Double media = repository.calcularMediaProva(provaId);
        if (media == null) return 0.0;
        return Math.round(media * 100.0) / 100.0;
    }

    public double calcularMediaGeral() {
        Double media = repository.calcularMediaGeral();
        if (media == null) return 0.0;
        return Math.round(media * 100.0) / 100.0;
    }

    public double calcularMediaGeralPorEscola(Integer escolaId) {
        Double media = repository.calcularMediaGeralPorEscola(escolaId);
        if (media == null) return 0.0;
        return Math.round(media * 100.0) / 100.0;
    }
}