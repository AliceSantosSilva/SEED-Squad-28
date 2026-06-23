package com.projeto.sistema_escolar.service;

import com.projeto.sistema_escolar.dto.DesempenhoDisciplinaDTO;
import com.projeto.sistema_escolar.dto.QuestaoTrilhaDTO;
import com.projeto.sistema_escolar.dto.TrilhaDisciplinaDTO;
import com.projeto.sistema_escolar.model.Questao;
import com.projeto.sistema_escolar.model.Usuario;
import com.projeto.sistema_escolar.repository.QuestaoRepository;
import com.projeto.sistema_escolar.repository.RespostaRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TrilhaService {

    private static final int MIN_RESPOSTAS_PARA_ANALISE = 3; // ignora disciplinas com poucos dados
    private static final double LIMITE_PERCENTUAL = 80.0;    // abaixo disso entra na trilha
    private static final int MAX_QUESTOES_POR_DISCIPLINA = 5;

    private final RespostaRepository respostaRepository;
    private final QuestaoRepository questaoRepository;

    public TrilhaService(RespostaRepository respostaRepository, QuestaoRepository questaoRepository) {
        this.respostaRepository = respostaRepository;
        this.questaoRepository = questaoRepository;
    }

    public List<TrilhaDisciplinaDTO> gerarTrilha(Usuario aluno) {
        List<DesempenhoDisciplinaDTO> desempenhos =
            respostaRepository.calcularDesempenhoPorDisciplina(aluno.getId());

        Integer serieId = (aluno.getTurma() != null && aluno.getTurma().getSerie() != null)
            ? aluno.getTurma().getSerie().getId() : null;

        Set<Integer> jaRespondidas = respostaRepository.findByAlunoId(aluno.getId())
            .stream()
            .map(r -> r.getQuestao().getId())
            .collect(Collectors.toSet());

        List<TrilhaDisciplinaDTO> trilha = new ArrayList<>();

        for (DesempenhoDisciplinaDTO d : desempenhos) {
            if (d.getTotalRespostas() < MIN_RESPOSTAS_PARA_ANALISE) continue;
            if (d.getPercentualAcerto() >= LIMITE_PERCENTUAL) continue;

            String nivel = d.getPercentualAcerto() < 50 ? "Crítico"
                         : d.getPercentualAcerto() < 70 ? "Atenção"
                         : "Reforço";

            List<Questao> candidatas = serieId != null
                ? questaoRepository.findByDisciplinaIdAndSerieIdOrderByDificuldadeAsc(d.getDisciplinaId(), serieId)
                : questaoRepository.findByDisciplinaId(d.getDisciplinaId());

            List<QuestaoTrilhaDTO> recomendadas = candidatas.stream()
                .filter(q -> !jaRespondidas.contains(q.getId()))
                .limit(MAX_QUESTOES_POR_DISCIPLINA)
                .map(q -> new QuestaoTrilhaDTO(q.getId(), q.getEnunciado(), q.getDificuldade()))
                .collect(Collectors.toList());

            trilha.add(new TrilhaDisciplinaDTO(
                d.getDisciplinaId(), d.getDisciplinaNome(), d.getPercentualAcerto(), nivel, recomendadas
            ));
        }

        trilha.sort(Comparator.comparingDouble(TrilhaDisciplinaDTO::getPercentualAcerto));
        return trilha;
    }
}