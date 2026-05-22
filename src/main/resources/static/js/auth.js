/**
 * Autenticação - Login e Cadastro
 * Integração futura com backend (Spring Security)
 */

document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm');
    const cadastroForm = document.getElementById('cadastroForm');

    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const email = document.getElementById('email').value.trim();
            const senha = document.getElementById('password').value;

            if (!email || !senha) {
                exibirAlerta('Preencha todos os campos', 'erro');
                return;
            }

            // Simulação de requisição (substituir por fetchAPI quando backend estiver pronto)
            exibirAlerta('Conectando ao servidor...', 'info');
            
            // Exemplo de chamada real (descomente quando o backend estiver pronto)
            /*
            fetchAPI('/api/login', {
                method: 'POST',
                body: JSON.stringify({ email, senha })
            }, (data) => {
                if (data.cargo) {
                    exibirAlerta(`Bem-vindo, ${data.nome}! Redirecionando...`, 'sucesso');
                    // Redireciona baseado no cargo (mapear conforme os nomes das pastas)
                    const cargoLower = data.cargo.toLowerCase();
                    let destino = '/';
                    if (cargoLower === 'admin') destino = '/admin/admin.html';
                    else if (cargoLower === 'professor') destino = '/professor/professor.html';
                    else if (cargoLower === 'aluno') destino = '/aluno/aluno.html';
                    else if (cargoLower === 'coordenador') destino = '/coordenacao/coordenacao.html';
                    else destino = '/';
                    setTimeout(() => { window.location.href = destino; }, 1500);
                } else {
                    exibirAlerta(data.mensagem || 'Credenciais inválidas', 'erro');
                }
            }, (error) => {
                exibirAlerta(error.message, 'erro');
            });
            */

            // Mock para teste visual (remover depois)
            setTimeout(() => {
                exibirAlerta('Login simulado - backend em breve', 'sucesso');
            }, 500);
        });
    }

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

            exibirAlerta('Cadastro simulado - backend em breve', 'sucesso');
            setTimeout(() => {
                window.location.href = '/login.html';
            }, 1500);
        });
    }
});