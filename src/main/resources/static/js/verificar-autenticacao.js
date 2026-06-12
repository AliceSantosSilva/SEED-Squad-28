/**
 * Verificação de autenticação JWT
 * Este script DEVE ser carregado em TODAS as páginas protegidas
 * (admin.html, professor.html, aluno.html, coordenacao/cord.html)
 */

(async function verificarAutenticacao() {
    console.log('Verificando autenticação...');
    
    const token = localStorage.getItem('authToken');
    const paginaAtual = window.location.pathname;
    
    // Páginas que NÃO precisam de autenticação
    const paginasPublicas = ['/login.html', '/cadastro.html', '/trocar-senha.html', '/index.html', '/'];
    
    if (paginasPublicas.some(p => paginaAtual === p)) {
        console.log('Página pública, não requer autenticação');
        return;
    }
    
    // Se não tem token, redireciona para login
    if (!token) {
        console.log('Token não encontrado, redirecionando para login');
        window.location.href = '/login.html';
        return;
    }
    
    // Verifica se o token é válido (não expirou)
    try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        if (payload.exp * 1000 < Date.now()) {
            console.log('Token expirado, redirecionando para login');
            localStorage.removeItem('authToken');
            localStorage.removeItem('usuarioPerfil');
            localStorage.removeItem('usuarioNome');
            window.location.href = '/login.html';
            return;
        }
    } catch (e) {
        console.error('Erro ao decodificar token:', e);
        localStorage.removeItem('authToken');
        window.location.href = '/login.html';
        return;
    }
    
    // Valida o token com o backend
    try {
        const response = await fetch('/api/usuario/logado', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (!response.ok) {
            console.log('Token rejeitado pelo backend, redirecionando para login');
            localStorage.removeItem('authToken');
            localStorage.removeItem('usuarioPerfil');
            localStorage.removeItem('usuarioNome');
            window.location.href = '/login.html';
            return;
        }
        
        const usuario = await response.json();
        console.log('Usuário autenticado:', usuario);
        
        // Verifica se o perfil corresponde à página atual
        const perfil = usuario.perfil;
        
        if (paginaAtual.includes('/admin/') && perfil !== 'ADMIN') {
            console.log('Perfil ADMIN tentando acessar página errada');
            window.location.href = '/login.html?erro=acesso-negado';
        } else if (paginaAtual.includes('/professor/') && perfil !== 'PROFESSOR') {
            window.location.href = '/login.html?erro=acesso-negado';
        } else if (paginaAtual.includes('/aluno/') && perfil !== 'ALUNO') {
            window.location.href = '/login.html?erro=acesso-negado';
        } else if (paginaAtual.includes('/coordenacao/') && perfil !== 'COORDENADOR') {
            window.location.href = '/login.html?erro=acesso-negado';
        }
        
    } catch (error) {
        console.error('Erro ao validar token:', error);
        window.location.href = '/login.html';
    }
})();