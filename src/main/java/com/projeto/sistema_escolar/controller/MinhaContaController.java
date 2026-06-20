package com.projeto.sistema_escolar.controller;


import com.projeto.sistema_escolar.dto.MinhaContaDTO;
import com.projeto.sistema_escolar.dto.TrocarSenhaDTO;
import com.projeto.sistema_escolar.model.Usuario;
import com.projeto.sistema_escolar.repository.UsuarioRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/minha-conta")
public class MinhaContaController {


    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;



    public MinhaContaController(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder){

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;

    }



    // Ver dados da conta logada
    @GetMapping
    public MinhaContaDTO verConta(
            Authentication authentication){


        String email = authentication.getName();


        Usuario usuario =
                usuarioRepository
                .findByEmail(email)
                .orElseThrow();



        return new MinhaContaDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfil().getNome()
        );

    }



    // Alterar nome e email
    @PutMapping
    public MinhaContaDTO atualizarConta(
            @RequestBody Usuario dados,
            Authentication authentication){


        String email = authentication.getName();


        Usuario usuario =
                usuarioRepository
                .findByEmail(email)
                .orElseThrow();



        usuario.setNome(dados.getNome());
        usuario.setEmail(dados.getEmail());


        usuarioRepository.save(usuario);



        return new MinhaContaDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfil().getNome()
        );

    }




    // Trocar senha
    @PutMapping("/senha")
    public String trocarSenha(
            @RequestBody TrocarSenhaDTO dados,
            Authentication authentication){


        String email = authentication.getName();


        Usuario usuario =
                usuarioRepository
                .findByEmail(email)
                .orElseThrow();



        if(!passwordEncoder.matches(
                dados.getSenhaAtual(),
                usuario.getSenha())){


            return "Senha atual incorreta";

        }



        usuario.setSenha(
                passwordEncoder.encode(
                        dados.getNovaSenha()
                )
        );


        usuarioRepository.save(usuario);



        return "Senha alterada com sucesso";

    }

}