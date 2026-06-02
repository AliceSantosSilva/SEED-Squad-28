/**
 * PROVA SERGIPE — auth.js
 * Autenticação real com backend Spring Security
 */

document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm');
    const cadastroForm = document.getElementById('cadastroForm');
    const trocarSenhaForm = document.getElementById('trocarSenhaForm');
    
    // ==================== LOGIN ====================
    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const email = document.getElementById('email').value.trim();
            const senha = document.getElementById('password').value;
            
            if (!email || !senha) {
                exibirAlerta('Preencha todos os campos', 'erro');
                return;
            }
            
            try {
                const response = await fetch('/api/login', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    credentials: 'include',
                    body: JSON.stringify({ email, senha })
                });
                
                const data = await response.json();
                
                if (response.status === 200 && data.sucesso) {
                    exibirAlerta(`Bem-vindo, ${data.nome}!`, 'sucesso');
                    
                    // Redirecionar baseado no perfil
                    const perfil = data.perfil.toLowerCase();
                    let destino = '/';
                    if (perfil === 'admin') destino = '/admin/admin.html';
                    else if (perfil === 'professor') destino = '/professor/professor.html';
                    else if (perfil === 'aluno') destino = '/aluno/aluno.html';
                    else if (perfil === 'coordenador') destino = '/coordenacao/coordenacao.html';
                    
                    setTimeout(() => { window.location.href = destino; }, 1000);
                    
                } else if (response.status === 403 && data.senhaExpirada) {
                    // Senha expirada - redirecionar para tela de troca de senha
                    sessionStorage.setItem('email', email);
                    sessionStorage.setItem('senhaAtual', senha);
                    window.location.href = '/trocar-senha.html';
                    
                } else {
                    exibirAlerta(data.mensagem || 'Credenciais inválidas', 'erro');
                }
                
            } catch (error) {
                console.error('Erro no login:', error);
                exibirAlerta('Erro ao conectar ao servidor', 'erro');
            }
        });
    }
    
    // ==================== CADASTRO ====================
    if (cadastroForm) {
        cadastroForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const email = document.getElementById('email').value.trim();
            const senha = document.getElementById('password').value;
            const confirm = document.getElementById('confirmPassword').value;
            
            if (!email || !senha || !confirm) {
                exibirAlerta('Preencha todos os campos', 'erro');
                return;
            }
            if (senha !== confirm) {
                exibirAlerta('As senhas não coincidem', 'erro');
                return;
            }
            
            try {
                const response = await fetch('/api/usuarios', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    credentials: 'include',
                    body: JSON.stringify({
                        nome: email.split('@')[0],
                        email: email,
                        senha: senha,
                        perfil: { id: 4 }, // ALUNO
                        ativo: true
                    })
                });
                
                if (response.ok) {
                    const data = await response.json();
                    exibirAlerta('Cadastro realizado com sucesso! Faça login.', 'sucesso');
                    setTimeout(() => { window.location.href = '/login.html'; }, 1500);
                } else {
                    const error = await response.json();
                    exibirAlerta(error.mensagem || 'Erro no cadastro', 'erro');
                }
            } catch (error) {
                console.error('Erro no cadastro:', error);
                exibirAlerta('Erro ao conectar ao servidor', 'erro');
            }
        });
    }
    
    // ==================== TROCAR SENHA ====================
    if (trocarSenhaForm) {
        const emailSalvo = sessionStorage.getItem('email');
        if (emailSalvo) {
            const emailInput = document.getElementById('email');
            if (emailInput) emailInput.value = emailSalvo;
        }
        
        trocarSenhaForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const novaSenha = document.getElementById('novaSenha').value;
            const confirmSenha = document.getElementById('confirmNovaSenha').value;
            
            if (novaSenha !== confirmSenha) {
                exibirAlerta('As senhas não coincidem', 'erro');
                return;
            }
            
            if (novaSenha.length < 6) {
                exibirAlerta('A senha deve ter pelo menos 6 caracteres', 'erro');
                return;
            }
            
            const senhaAtual = sessionStorage.getItem('senhaAtual');
            
            try {
                const response = await fetch('/api/trocar-senha', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    credentials: 'include',
                    body: JSON.stringify({
                        senhaAtual: senhaAtual,
                        novaSenha: novaSenha
                    })
                });
                
                if (response.ok) {
                    exibirAlerta('Senha alterada com sucesso! Faça login novamente.', 'sucesso');
                    sessionStorage.clear();
                    setTimeout(() => { window.location.href = '/login.html'; }, 1500);
                } else {
                    const error = await response.json();
                    exibirAlerta(error.erro || 'Erro ao trocar senha', 'erro');
                }
            } catch (error) {
                console.error('Erro ao trocar senha:', error);
                exibirAlerta('Erro ao conectar ao servidor', 'erro');
            }
        });
    }
});