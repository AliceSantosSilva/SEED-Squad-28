/**
 * PROVA SERGIPE — auth.js
 * Login, cadastro e troca de senha.
 * Depende de global.js (getToken, setToken, removerToken, salvarDadosUsuario, exibirAlerta).
 */

document.addEventListener('DOMContentLoaded', () => {

    const loginForm       = document.getElementById('loginForm');
    const cadastroForm    = document.getElementById('cadastroForm');
    const trocarSenhaForm = document.getElementById('trocarSenhaForm');

    /* =============================================
       LOGIN
       ============================================= */

    if (loginForm) {

        // Se já tem token válido, redireciona direto
        const tokenExistente = getToken();
        if (tokenExistente) {
            window.location.href = '/api/usuario/logado';
        }

        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();

            const email = document.getElementById('email').value.trim();
            const senha = document.getElementById('password').value;

            if (!email || !senha) {
                exibirAlerta('Preencha todos os campos.', 'erro');
                return;
            }

            const btn = loginForm.querySelector('button[type="submit"]');
            btn.disabled    = true;
            btn.textContent = 'Aguarde...';

            try {
                const response = await fetch('/api/login', {
                    method:  'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body:    JSON.stringify({ email, senha }),
                });

                const data = await response.json();

                if (response.ok && data.sucesso) {
                    // Salva token e dados do usuário
                    setToken(data.token);
                    salvarDadosUsuario({ nome: data.nome, perfil: data.perfil });

                    exibirAlerta(`Bem-vindo, ${data.nome}!`, 'sucesso');
                    setTimeout(() => {
                        window.location.href = data.redirect;
                    }, 800);

                } else if (response.status === 403 && data.senhaExpirada) {
                    // Primeiro acesso — salva token temporário e redireciona
                    localStorage.setItem('tokenTemp', data.tokenTemp);
                    window.location.href = '/trocar-senha.html';

                } else {
                    exibirAlerta(data.mensagem || 'Credenciais inválidas.', 'erro');
                }

            } catch (error) {
                console.error('Erro no login:', error);
                exibirAlerta('Erro ao conectar ao servidor.', 'erro');
            } finally {
                btn.disabled    = false;
                btn.textContent = 'Acessar';
            }
        });
    }

    /* =============================================
       CADASTRO
       ============================================= */

    if (cadastroForm) {
        cadastroForm.addEventListener('submit', async (e) => {
            e.preventDefault();

            const email        = document.getElementById('email').value.trim();
            const senha        = document.getElementById('password').value;
            const confirmSenha = document.getElementById('confirmPassword').value;

            if (!email || !senha || !confirmSenha) {
                exibirAlerta('Preencha todos os campos.', 'erro');
                return;
            }
            if (senha !== confirmSenha) {
                exibirAlerta('As senhas não coincidem.', 'erro');
                return;
            }
            if (senha.length < 6) {
                exibirAlerta('A senha deve ter pelo menos 6 caracteres.', 'erro');
                return;
            }

            const btn = cadastroForm.querySelector('button[type="submit"]');
            btn.disabled    = true;
            btn.textContent = 'Aguarde...';

            try {
                const response = await fetch('/api/cadastro', {
                    method:  'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body:    JSON.stringify({ email, senha }),
                });

                const data = await response.json();

                if (response.ok) {
                    exibirAlerta('Cadastro realizado! Redirecionando...', 'sucesso');
                    setTimeout(() => { window.location.href = '/login.html'; }, 1500);
                } else {
                    exibirAlerta(data.mensagem || 'Erro ao realizar cadastro.', 'erro');
                }

            } catch (error) {
                exibirAlerta('Erro ao conectar ao servidor.', 'erro');
            } finally {
                btn.disabled    = false;
                btn.textContent = 'Cadastrar';
            }
        });
    }

    /* =============================================
       TROCAR SENHA (primeiro acesso)
       ============================================= */

    if (trocarSenhaForm) {
        // Usa o token temporário gerado no login com senha expirada
        const tokenTemp = localStorage.getItem('tokenTemp');
        if (!tokenTemp) {
            window.location.href = '/login.html';
            return;
        }

        trocarSenhaForm.addEventListener('submit', async (e) => {
            e.preventDefault();

            const senhaAtual     = document.getElementById('senhaAtual').value;
            const novaSenha      = document.getElementById('novaSenha').value;
            const confirmarSenha = document.getElementById('confirmarSenha').value;

            if (!senhaAtual || !novaSenha || !confirmarSenha) {
                exibirAlerta('Preencha todos os campos.', 'erro');
                return;
            }
            if (novaSenha !== confirmarSenha) {
                exibirAlerta('As novas senhas não coincidem.', 'erro');
                return;
            }
            if (novaSenha.length < 6) {
                exibirAlerta('A nova senha deve ter pelo menos 6 caracteres.', 'erro');
                return;
            }
            if (novaSenha === senhaAtual) {
                exibirAlerta('A nova senha deve ser diferente da atual.', 'aviso');
                return;
            }

            const btn = trocarSenhaForm.querySelector('button[type="submit"]');
            btn.disabled    = true;
            btn.textContent = 'Aguarde...';

            try {
                const response = await fetch('/api/trocar-senha', {
                    method:  'POST',
                    headers: {
                        'Content-Type':  'application/json',
                        'Authorization': `Bearer ${tokenTemp}`,
                    },
                    body: JSON.stringify({ senhaAtual, novaSenha }),
                });

                const data = await response.json();

                if (response.ok) {
                    // Troca o token temporário pelo definitivo
                    localStorage.removeItem('tokenTemp');
                    setToken(data.token);
                    salvarDadosUsuario({ nome: data.nome || '', perfil: data.perfil || '' });

                    exibirAlerta('Senha alterada! Redirecionando...', 'sucesso');
                    setTimeout(() => {
                        window.location.href = data.redirect || '/login.html';
                    }, 1200);
                } else {
                    exibirAlerta(data.erro || 'Erro ao trocar a senha.', 'erro');
                }

            } catch (error) {
                exibirAlerta('Erro ao conectar ao servidor.', 'erro');
            } finally {
                btn.disabled    = false;
                btn.textContent = 'Confirmar nova senha';
            }
        });
    }

});