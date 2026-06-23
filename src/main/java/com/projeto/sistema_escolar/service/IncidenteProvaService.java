package com.projeto.sistema_escolar.service;

import com.projeto.sistema_escolar.model.IncidenteProva;
import com.projeto.sistema_escolar.repository.IncidenteProvaRepository;
import org.springframework.stereotype.Service;

@Service
public class IncidenteProvaService {

    private final IncidenteProvaRepository repository;

    public IncidenteProvaService(IncidenteProvaRepository repository) {
        this.repository = repository;
    }

    public IncidenteProva registrar(IncidenteProva incidente) {
        return repository.save(incidente);
    }

    public long contar(Integer alunoId, Integer provaId) {
        return repository.countByAlunoIdAndProvaId(alunoId, provaId);
    }
}