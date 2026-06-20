package com.projeto.sistema_escolar.controller;


import com.projeto.sistema_escolar.model.ForumProfessor;
import com.projeto.sistema_escolar.model.Usuario;
import com.projeto.sistema_escolar.repository.UsuarioRepository;
import com.projeto.sistema_escolar.service.ForumProfessorService;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/forum")
public class ForumProfessorController {


    private final ForumProfessorService service;
    private final UsuarioRepository usuarioRepository;


    public ForumProfessorController(
            ForumProfessorService service,
            UsuarioRepository usuarioRepository){

        this.service = service;
        this.usuarioRepository = usuarioRepository;

    }



    @GetMapping
    public List<ForumProfessor> listar(){

        return service.listar();

    }



    @PostMapping
    public ForumProfessor criar(
            @RequestBody ForumProfessor forum,
            Authentication authentication){


        String email = authentication.getName();


        Usuario usuario =
            usuarioRepository.findByEmail(email)
            .orElseThrow();


        forum.setAutor(usuario.getNome());


        return service.salvar(forum);

    }



    @DeleteMapping("/{id}")
    public void deletar(
            @PathVariable Integer id,
            Authentication authentication){


        String email = authentication.getName();


        Usuario usuario =
            usuarioRepository.findByEmail(email)
            .orElseThrow();


        ForumProfessor forum = service.buscarPorId(id);


        if(!forum.getAutor().equals(usuario.getNome())){

            throw new RuntimeException(
                "Você não pode excluir uma postagem de outro professor"
            );

        }


        service.deletar(id);

    }

}