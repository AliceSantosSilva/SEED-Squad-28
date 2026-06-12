/**
 * PROVA SERGIPE — auth.js
 * Autenticação via JWT (compatível com o backend JWT)
 */

'use strict';

const API_BASE = 'http://localhost:8081';

// ── UTILITÁRIOS DE TOKEN ─────────────────────────────────────────────────

function setToken(token) {
    localStorage.setItem('authToken', token);
}

function getToken() {
    return localStorage.getItem('authToken');
}

function removeToken() {
    localStorage.removeItem('authToken');
    localStorage.removeItem('usuarioPerfil');
    localStorage.removeItem('usuarioNome');
    localStorage.removeItem('usuarioId');
}

function isAuthenticated() {
    const token = getToken();
    if (!token) return false;
    try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        return payload.exp * 1000 > Date.now();
    } catch {
        return false;
    }
}

function obterDestinoPorPerfil(perfil) {
    const mapa = {
        'ADMIN': '/admin/adm.html',
        'PROFESSOR': '/professor/prof.html',
        'ALUNO': '/aluno/aluno.html',
        'COORDENADOR': '/coordenacao/cord.html'
    };
    return mapa[perfil] || '/login.html';
}

// ── EVENTOS DA PÁGINA ─────────────────────────────────────────────────────

document.addEventListener('DOMContentLoaded', () => {

    const loginForm = document.getElementById('loginForm');
    const cadastroForm = document.getElementById('cadastroForm');
    const trocarSenhaForm = document.getElementById('trocarSenhaForm');

    // ── LOGIN ─────────────────────────────────────────────────────────────

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
            btnSubmit.disabled = true;
            btnSubmit.textContent = 'Aguarde...';

            try {
                const response = await fetch(`${API_BASE}/api/login`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ email, senha })
                });

                const data = await response.json();

                if (response.ok && data.sucesso) {
                    setToken(data.token);
                    localStorage.setItem('usuarioPerfil', data.perfil);
                    localStorage.setItem('usuarioNome', data.nome);

                    exibirAlerta(`Bem-vindo, ${data.nome}!`, 'sucesso');
                    setTimeout(() => {
                        window.location.href = data.redirect || obterDestinoPorPerfil(data.perfil);
                    }, 800);

                } else if (response.status === 403 && data.senhaExpirada) {
                    sessionStorage.setItem('emailPrimeiroAcesso', email);
                    if (data.tokenTemp) {
                        localStorage.setItem('tokenTemp', data.tokenTemp);
                    }
                    window.location.href = '/trocar-senha.html';

                } else {
                    exibirAlerta(data.mensagem || 'Credenciais inválidas.', 'erro');
                }

            } catch (error) {
                console.error('Erro no login:', error);
                exibirAlerta('Erro ao conectar ao servidor.', 'erro');
            } finally {
                btnSubmit.disabled = false;
                btnSubmit.textContent = 'Acessar';
            }
        });
    }

    // ── CADASTRO ──────────────────────────────────────────────────────────

    if (cadastroForm) {
        cadastroForm.addEventListener('submit', async (e) => {
            e.preventDefault();

            const email = document.getElementById('email').value.trim();
            const senha = document.getElementById('password').value;
            const confirm = document.getElementById('confirmPassword')?.value;

            if (!email || !senha || !confirm) {
                exibirAlerta('Preencha todos os campos.', 'erro');
                return;
            }

            if (senha.length < 6) {
                exibirAlerta('A senha deve ter pelo menos 6 caracteres.', 'erro');
                return;
            }

            if (senha !== confirm) {
                exibirAlerta('As senhas não coincidem.', 'erro');
                return;
            }

            const btnSubmit = cadastroForm.querySelector('button[type="submit"]');
            btnSubmit.disabled = true;
            btnSubmit.textContent = 'Cadastrando...';

            try {
                const response = await fetch(`${API_BASE}/api/cadastro`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        nome: email.split('@')[0],
                        email: email,
                        senha: senha
                    })
                });

                if (response.ok || response.status === 201) {
                    exibirAlerta('Cadastro realizado! Redirecionando para login...', 'sucesso');
                    setTimeout(() => { window.location.href = '/login.html'; }, 1500);
                } else {
                    const error = await response.json();
                    exibirAlerta(error.mensagem || 'Erro no cadastro.', 'erro');
                }

            } catch (error) {
                console.error('Erro no cadastro:', error);
                exibirAlerta('Erro ao conectar ao servidor.', 'erro');
            } finally {
                btnSubmit.disabled = false;
                btnSubmit.textContent = 'Cadastrar';
            }
        });
    }

    // ── TROCAR SENHA ──────────────────────────────────────────────────────

    if (trocarSenhaForm) {
        const emailSalvo = sessionStorage.getItem('emailPrimeiroAcesso');
        const emailInput = document.getElementById('email');
        if (emailSalvo && emailInput) {
            emailInput.value = emailSalvo;
            emailInput.readOnly = true;
        }

        trocarSenhaForm.addEventListener('submit', async (e) => {
            e.preventDefault();

            const senhaAtual = document.getElementById('senhaAtual')?.value;
            const novaSenha = document.getElementById('novaSenha').value;
            const confirmar = document.getElementById('confirmarSenha')?.value;

            if (confirmar && novaSenha !== confirmar) {
                exibirAlerta('As senhas não coincidem.', 'erro');
                return;
            }

            if (novaSenha.length < 6) {
                exibirAlerta('A nova senha deve ter pelo menos 6 caracteres.', 'erro');
                return;
            }

            const tokenUsado = localStorage.getItem('tokenTemp') || getToken();

            if (!tokenUsado) {
                exibirAlerta('Sessão inválida. Faça login novamente.', 'erro');
                setTimeout(() => { window.location.href = '/login.html'; }, 1500);
                return;
            }

            const btnSubmit = trocarSenhaForm.querySelector('button[type="submit"]');
            btnSubmit.disabled = true;
            btnSubmit.textContent = 'Salvando...';

            try {
                const response = await fetch(`${API_BASE}/api/trocar-senha`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${tokenUsado}`
                    },
                    body: JSON.stringify({ senhaAtual, novaSenha })
                });

                const data = await response.json();

                if (response.ok) {
                    if (data.token) {
                        setToken(data.token);
                        localStorage.setItem('usuarioPerfil', data.perfil);
                        localStorage.setItem('usuarioNome', data.nome);
                    }
                    localStorage.removeItem('tokenTemp');
                    sessionStorage.removeItem('emailPrimeiroAcesso');

                    exibirAlerta('Senha alterada com sucesso!', 'sucesso');
                    setTimeout(() => {
                        window.location.href = data.redirect || '/login.html';
                    }, 1000);

                } else {
                    exibirAlerta(data.erro || data.mensagem || 'Erro ao trocar senha.', 'erro');
                }

            } catch (error) {
                console.error('Erro ao trocar senha:', error);
                exibirAlerta('Erro ao conectar ao servidor.', 'erro');
            } finally {
                btnSubmit.disabled = false;
                btnSubmit.textContent = 'Salvar nova senha';
            }
        });
    }
});