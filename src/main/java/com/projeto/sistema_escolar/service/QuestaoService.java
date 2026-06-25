package com.projeto.sistema_escolar.service;

import com.projeto.sistema_escolar.model.Alternativa;
import com.projeto.sistema_escolar.model.Prova;
import com.projeto.sistema_escolar.model.Questao;
import com.projeto.sistema_escolar.repository.AlternativaRepository;
import com.projeto.sistema_escolar.repository.ProvaRepository;
import com.projeto.sistema_escolar.repository.QuestaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import jakarta.transaction.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Service
public class QuestaoService {

    private final QuestaoRepository repository;
    private final DisciplinaService disciplinaService;
    private final SerieService serieService;


    private final ProvaRepository provaRepository;
    private final AlternativaRepository alternativaRepository;

    public QuestaoService(QuestaoRepository repository,
                          DisciplinaService disciplinaService,
                          SerieService serieService,
                          ProvaRepository provaRepository,
                          AlternativaRepository alternativaRepository) {
        this.repository = repository;
        this.disciplinaService = disciplinaService;
        this.serieService = serieService;
        this.provaRepository = provaRepository;
        this.alternativaRepository = alternativaRepository;
    }

    public List<Questao> listarTodos() {
        return repository.findAll();
    }

    public Optional<Questao> buscarPorId(Integer id) {
        return repository.findById(id);
    }

    public Questao salvar(Questao questao) {
        if (questao.getDisciplina() != null && questao.getDisciplina().getId() != null) {
            disciplinaService.buscarPorId(questao.getDisciplina().getId())
                    .ifPresent(questao::setDisciplina);
        }
        if (questao.getSerie() != null && questao.getSerie().getId() != null) {
            serieService.buscarPorId(questao.getSerie().getId())
                    .ifPresent(questao::setSerie);
        }
        return repository.save(questao);
    }

    public void deletar(Integer id) {
        repository.deleteById(id);
    }

    public boolean existePorId(Integer id) {
        return repository.existsById(id);
    }

    public List<Questao> buscarPorDisciplina(Integer disciplinaId) {
        return repository.findByDisciplinaId(disciplinaId);
    }

    public List<Questao> buscarPorSerie(Integer serieId) {
        return repository.findBySerieId(serieId);
    }

    public List<Questao> buscarPorDisciplinaESerie(Integer disciplinaId, Integer serieId) {
        return repository.findByDisciplinaIdAndSerieId(disciplinaId, serieId);
    }



    @Transactional
    public void importarQuestoesPorCsv(MultipartFile file, Integer provaId) throws Exception {

        // 1. Busca a Prova
        Prova prova = provaRepository.findById(provaId)
                .orElseThrow(() -> new RuntimeException("Prova não encontrada com ID: " + provaId));

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String linha;
            boolean primeiraLinha = true;

            while ((linha = br.readLine()) != null) {
                // Pula o cabeçalho
                if (primeiraLinha) {
                    primeiraLinha = false;
                    continue;
                }

                if (linha.trim().isEmpty()) continue;

                String[] colunas = linha.split(";");

                if (colunas.length < 6) continue;


                String enunciado = colunas[0].trim();
                String opcaoA = colunas[1].trim();
                String opcaoB = colunas[2].trim();
                String opcaoC = colunas[3].trim();
                String opcaoD = colunas[4].trim();
                String letraCorreta = colunas[5].trim().toUpperCase();


                Questao questao = new Questao();
                questao.setEnunciado(enunciado);
                Questao questaoSalva = repository.save(questao);


                salvarAlternativa(opcaoA, letraCorreta.equals("A"), questaoSalva);
                salvarAlternativa(opcaoB, letraCorreta.equals("B"), questaoSalva);
                salvarAlternativa(opcaoC, letraCorreta.equals("C"), questaoSalva);
                salvarAlternativa(opcaoD, letraCorreta.equals("D"), questaoSalva);


                prova.getQuestoes().add(questaoSalva);
            }
        }


        provaRepository.save(prova);
    }


    private void salvarAlternativa(String texto, boolean isCorreta, Questao questao) {
        Alternativa alternativa = new Alternativa();
        alternativa.setTexto(texto);
        alternativa.setCorreta(isCorreta);
        alternativa.setQuestao(questao);
        alternativaRepository.save(alternativa);
    }
}