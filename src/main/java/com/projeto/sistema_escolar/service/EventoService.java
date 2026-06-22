package com.projeto.sistema_escolar.service;

import com.projeto.sistema_escolar.dto.EventoDTO;
import com.projeto.sistema_escolar.model.Evento;
import com.projeto.sistema_escolar.model.Usuario;
import com.projeto.sistema_escolar.repository.EventoRepository;
import com.projeto.sistema_escolar.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventoService {

    private final EventoRepository eventoRepository;
    private final UsuarioRepository usuarioRepository;

    public EventoService(EventoRepository eventoRepository, UsuarioRepository usuarioRepository) {
        this.eventoRepository = eventoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<Evento> listarTodos() {
        return eventoRepository.findAll();
    }

    public Evento criar(EventoDTO dto, String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        String perfil = usuario.getPerfil().getNome().toUpperCase();

        // Professores só podem criar tipo PROVA
        if (perfil.equals("PROFESSOR") && !dto.getTipo().equals("PROVA")) {
            throw new RuntimeException("Professor só pode criar eventos do tipo PROVA");
        }

        // Se for PROVA, valida se está dentro de algum PERIODO_PROVA
        if (dto.getTipo().equals("PROVA")) {
            boolean dentroDeUmPeriodo = eventoRepository.findByTipo("PERIODO_PROVA")
                .stream()
                .anyMatch(p ->
                    !dto.getDataInicio().isBefore(p.getDataInicio()) &&
                    !dto.getDataFim().isAfter(p.getDataFim())
                );
            if (!dentroDeUmPeriodo) {
                throw new RuntimeException("A prova deve estar dentro de um período de prova definido pela coordenação");
            }
        }

        Evento evento = new Evento();
        evento.setTitulo(dto.getTitulo());
        evento.setDescricao(dto.getDescricao());
        evento.setDataInicio(dto.getDataInicio());
        evento.setDataFim(dto.getDataFim());
        evento.setTipo(dto.getTipo());
        evento.setCriadoPor(usuario);

        return eventoRepository.save(evento);
    }

    public void deletar(Integer id, String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        String perfil = usuario.getPerfil().getNome().toUpperCase();

        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado"));

        // Professor só pode deletar o próprio evento
        if (perfil.equals("PROFESSOR") &&
            !evento.getCriadoPor().getId().equals(usuario.getId())) {
            throw new RuntimeException("Professor só pode remover seus próprios eventos");
        }

        eventoRepository.deleteById(id);
    }
}