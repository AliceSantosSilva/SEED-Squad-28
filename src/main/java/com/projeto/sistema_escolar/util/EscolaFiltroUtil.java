package com.projeto.sistema_escolar.util;

import com.projeto.sistema_escolar.model.Usuario;
import com.projeto.sistema_escolar.service.CoordenadorService;
import com.projeto.sistema_escolar.service.ProfessorService;
import com.projeto.sistema_escolar.service.UsuarioService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class EscolaFiltroUtil {

    private final UsuarioService usuarioService;
    private final CoordenadorService coordenadorService;
    private final ProfessorService professorService;

    public EscolaFiltroUtil(UsuarioService usuarioService,
                            CoordenadorService coordenadorService,
                            ProfessorService professorService) {
        this.usuarioService = usuarioService;
        this.coordenadorService = coordenadorService;
        this.professorService = professorService;
    }

    public Integer getEscolaIdDoUsuarioLogado() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioService.buscarPorEmail(email).orElse(null);
        if (usuario == null || usuario.getPerfil() == null) return null;

        String perfil = usuario.getPerfil().getNome();
        switch (perfil) {
            case "ADMIN":
                return null;
            case "COORDENADOR":
                return coordenadorService.buscarPorUsuarioId(usuario.getId())
                        .map(coord -> coord.getEscola().getId())
                        .orElse(null);
            case "PROFESSOR":
                return professorService.buscarPorUsuarioId(usuario.getId())
                        .map(prof -> prof.getEscola().getId())
                        .orElse(null);
            default:
                return null;
        }
    }
}