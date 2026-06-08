package com.projeto.sistema_escolar.service;

import com.projeto.sistema_escolar.model.Usuario;
import com.projeto.sistema_escolar.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder   passwordEncoder;

    public UsuarioService(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        this.repository      = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Usuario> listarTodos() {
        return repository.findAll();
    }

    public Optional<Usuario> buscarPorId(Integer id) {
        return repository.findById(id);
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        return repository.findByEmail(email);
    }

    /**
     * Salva o usuário.
     * A senha só é criptografada se ainda não estiver no formato BCrypt
     * (evita double-encode quando o controller atualiza outros campos
     * sem alterar a senha).
     */
    public Usuario salvar(Usuario usuario) {
        String senha = usuario.getSenha();
        if (senha != null && !senha.isEmpty() && !isBcrypt(senha)) {
            usuario.setSenha(passwordEncoder.encode(senha));
        }
        return repository.save(usuario);
    }

    public void deletar(Integer id) {
        repository.deleteById(id);
    }

    public boolean existePorId(Integer id) {
        return repository.existsById(id);
    }

    /** BCrypt sempre começa com $2a$, $2b$ ou $2y$ */
    private boolean isBcrypt(String senha) {
        return senha.startsWith("$2a$")
            || senha.startsWith("$2b$")
            || senha.startsWith("$2y$");
    }
}