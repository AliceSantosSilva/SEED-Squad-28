/**
 * PROVA SERGIPE — auth.js
 * Lógica das telas de autenticação: login, cadastro, trocar senha.
 * Depende de global.js (API_BASE, exibirAlerta, fetchAPI).
 */

document.addEventListener('DOMContentLoaded', () => {

    const loginForm       = document.getElementById('loginForm');
    const cadastroForm    = document.getElementById('cadastroForm');
    const trocarSenhaForm = document.getElementById('trocarSenhaForm');

    /* =============================================
       LOGIN
       ============================================= */

    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();

            const email = document.getElementById('email').value.trim();
            const senha = document.getElementById('password').value;

            if (!email || !senha) {
                exibirAlerta('Preencha todos os campos.', 'erro');
                return;
            }

            const btnSubmit = loginForm.querySelector('button[type="submit"]');
            btnSubmit.disabled    = true;
            btnSubmit.textContent = 'Aguarde...';

            try {
                const response = await fetch(`${API_BASE}/api/login`, {
                    method:      'POST',
                    headers:     { 'Content-Type': 'application/json' },
                    credentials: 'include',
                    body:        JSON.stringify({ email, senha })
                });

                const data = await response.json();

                if (response.ok && data.sucesso) {
                    exibirAlerta(`Bem-vindo, ${data.nome}!`, 'sucesso');
                    setTimeout(() => {
                        window.location.href = data.redirect;
                    }, 800);

                } else if (response.status === 403 && data.senhaExpirada) {
                    // Primeiro acesso — salva a senha temporária e redireciona
                    sessionStorage.setItem('emailPrimeiroAcesso', email);
                    sessionStorage.setItem('senhaPrimeiroAcesso', senha);
                    window.location.href = '/trocar-senha.html';

                } else {
                    exibirAlerta(data.mensagem || 'Credenciais inválidas.', 'erro');
                }

            } catch (error) {
                console.error('Erro no login:', error);
                exibirAlerta('Erro ao conectar ao servidor.', 'erro');
            } finally {
                btnSubmit.disabled    = false;
                btnSubmit.textContent = 'Acessar';
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

            const btnSubmit = cadastroForm.querySelector('button[type="submit"]');
            btnSubmit.disabled    = true;
            btnSubmit.textContent = 'Aguarde...';

            try {
                const response = await fetch(`${API_BASE}/api/cadastro`, {
                    method:      'POST',
                    headers:     { 'Content-Type': 'application/json' },
                    credentials: 'include',
                    body:        JSON.stringify({ email, senha })
                });

                const data = await response.json();

                if (response.ok) {
                    exibirAlerta('Cadastro realizado! Redirecionando...', 'sucesso');
                    setTimeout(() => {
                        window.location.href = '/login.html';
                    }, 1500);
                } else {
                    exibirAlerta(data.mensagem || 'Erro ao realizar cadastro.', 'erro');
                }

            } catch (error) {
                console.error('Erro no cadastro:', error);
                exibirAlerta('Erro ao conectar ao servidor.', 'erro');
            } finally {
                btnSubmit.disabled    = false;
                btnSubmit.textContent = 'Cadastrar';
            }
        });
    }

    /* =============================================
       TROCAR SENHA (primeiro acesso)
       ============================================= */

    if (trocarSenhaForm) {
        // Preenche automaticamente a senha atual com o valor
        // salvo no sessionStorage durante o redirecionamento do login
        const senhaAtualInput = document.getElementById('senhaAtual');
        const senhaGuardada   = sessionStorage.getItem('senhaPrimeiroAcesso');
        if (senhaAtualInput && senhaGuardada) {
            senhaAtualInput.value = senhaGuardada;
        }

        trocarSenhaForm.addEventListener('submit', async (e) => {
            e.preventDefault();

            const senhaAtual    = document.getElementById('senhaAtual').value;
            const novaSenha     = document.getElementById('novaSenha').value;
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

            const btnSubmit = trocarSenhaForm.querySelector('button[type="submit"]');
            btnSubmit.disabled    = true;
            btnSubmit.textContent = 'Aguarde...';

            try {
                const response = await fetch(`${API_BASE}/api/trocar-senha`, {
                    method:      'POST',
                    headers:     { 'Content-Type': 'application/json' },
                    credentials: 'include',
                    body:        JSON.stringify({ senhaAtual, novaSenha })
                });

                const data = await response.json();

                if (response.ok) {
                    // Limpa dados do primeiro acesso
                    sessionStorage.removeItem('emailPrimeiroAcesso');
                    sessionStorage.removeItem('senhaPrimeiroAcesso');

                    exibirAlerta('Senha alterada com sucesso! Redirecionando...', 'sucesso');
                    setTimeout(() => {
                        window.location.href = data.redirect || '/login.html';
                    }, 1200);

                } else {
                    exibirAlerta(data.erro || 'Erro ao trocar a senha.', 'erro');
                }

            } catch (error) {
                console.error('Erro ao trocar senha:', error);
                exibirAlerta('Erro ao conectar ao servidor.', 'erro');
            } finally {
                btnSubmit.disabled    = false;
                btnSubmit.textContent = 'Confirmar nova senha';
            }
        });
    }

});