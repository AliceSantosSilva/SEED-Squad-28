package com.projeto.sistema_escolar.controller;

import com.projeto.sistema_escolar.model.Perfil;
import com.projeto.sistema_escolar.model.Usuario;
import com.projeto.sistema_escolar.service.PerfilService;
import com.projeto.sistema_escolar.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * Endpoint público para auto-cadastro de alunos.
 * Separado do UsuarioController (que é protegido) para evitar
 * que qualquer pessoa crie usuários com perfis arbitrários.
 *
 * O perfil ALUNO é definido internamente pelo backend — o frontend
 * não envia nem precisa conhecer o ID do perfil.
 */
@RestController
@RequestMapping("/api/cadastro")
public class CadastroPublicoController {

    // Nome exato do perfil de aluno conforme cadastrado na tabela `perfis`
    private static final String PERFIL_ALUNO = "ALUNO";

    private final UsuarioService usuarioService;
    private final PerfilService  perfilService;

    public CadastroPublicoController(UsuarioService usuarioService,
                                     PerfilService perfilService) {
        this.usuarioService = usuarioService;
        this.perfilService  = perfilService;
    }

    @PostMapping
    public ResponseEntity<?> cadastrar(@RequestBody CadastroRequest request) {

        // Validações básicas
        if (request.email() == null || request.email().isBlank()) {
            return ResponseEntity.badRequest()
                .body(Map.of("mensagem", "E-mail é obrigatório."));
        }

        if (request.senha() == null || request.senha().length() < 6) {
            return ResponseEntity.badRequest()
                .body(Map.of("mensagem", "A senha deve ter pelo menos 6 caracteres."));
        }

        // Verifica se o e-mail já está em uso
        if (usuarioService.buscarPorEmail(request.email()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("mensagem", "E-mail já cadastrado."));
        }

        // Busca o perfil ALUNO no banco (não usa ID hardcoded)
        Optional<Perfil> perfilAluno = perfilService.buscarPorNome(PERFIL_ALUNO);
        if (perfilAluno.isEmpty()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("mensagem", "Configuração do sistema incompleta. Contate o administrador."));
        }

        // Monta e salva o usuário
        Usuario usuario = new Usuario();
        usuario.setNome(request.nome() != null ? request.nome() : request.email().split("@")[0]);
        usuario.setEmail(request.email().trim().toLowerCase());
        usuario.setSenha(request.senha()); // será criptografada no UsuarioService
        usuario.setPerfil(perfilAluno.get());
        usuario.setAtivo(true);
        usuario.setSenhaExpirada(false);

        usuarioService.salvar(usuario);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(Map.of("mensagem", "Cadastro realizado com sucesso."));
    }

    // Record interno para o corpo da requisição
    record CadastroRequest(String nome, String email, String senha) {}
}