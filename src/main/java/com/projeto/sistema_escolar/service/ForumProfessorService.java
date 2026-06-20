package com.projeto.sistema_escolar.service;

import com.projeto.sistema_escolar.model.ForumProfessor;
import com.projeto.sistema_escolar.repository.ForumProfessorRepository;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ForumProfessorService {


    private final ForumProfessorRepository repository;


    public ForumProfessorService(
            ForumProfessorRepository repository){

        this.repository = repository;

    }


    public List<ForumProfessor> listar(){

        return repository.findAll();

    }


    public ForumProfessor salvar(ForumProfessor forum){

        return repository.save(forum);

    }


    public ForumProfessor buscarPorId(Integer id){

        return repository.findById(id)
                .orElseThrow();

    }


    public void deletar(Integer id){

        repository.deleteById(id);

    }

}