/**
 * PROVA SERGIPE — cord.js (Coordenador)
 * Lógica específica do perfil Coordenador (compatível com JWT)
 */

'use strict';

// ── FUNÇÃO fetchAPI COM TOKEN JWT ─────────────────────────────────────────────

async function fetchAPI(url, options = {}) {
    const token = localStorage.getItem('authToken');
    const headers = {
        'Content-Type': 'application/json',
        ...options.headers
    };

    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(url, {
        ...options,
        headers
    });

    if (response.status === 401) {
        localStorage.removeItem('authToken');
        localStorage.removeItem('usuarioPerfil');
        localStorage.removeItem('usuarioNome');
        window.location.href = '/login.html';
        throw new Error('Sessão expirada');
    }

    return response.json();
}

// ── CALENDÁRIO ────────────────────────────────────────────────────────────────

const coordEvents = [
    { date: getDataLocal(new Date()), type: "exam", title: "Hoje" },
    { date: "2026-06-20", type: "exam", title: "Simulado Estadual" }
];

let currentCalendarDate = new Date();

function initCoordenacaoCalendar() {
    renderCalendar('coordenacao-calendar', currentCalendarDate, coordEvents, (date, events) => {
        if (!events.length) {
            exibirAlerta(`Nenhum evento em ${formatarData(date)}`, 'info');
            return;
        }
        exibirAlerta(`${formatarData(date)} • ${events.map(e => e.title).join(', ')}`, 'sucesso');
    });
    const container = document.getElementById('coordenacao-calendar');
    container?.addEventListener('calendarChange', (e) => { currentCalendarDate = e.detail.date; });
}

// ── CARREGAR ESCOLA ────────────────────────────────────────────────────────────

async function fillEscolasRecentes() {
    const container = document.getElementById('escolasRecentesList');
    if (!container) return;

    try {
        const escola = await fetchAPI('/api/coordenador/minha-escola');

        if (!escola || !escola.nome) {
            container.innerHTML = '<p style="color:#64748b;font-size:12px;padding:8px;">Nenhuma escola encontrada.</p>';
            return;
        }

        container.innerHTML = `
            <div class="exam-item">
                <div class="exam-info">
                    <span class="exam-name">${escola.nome}</span>
                    <span class="exam-date"><i class='bx bx-map'></i> ${escola.cidade || '—'}</span>
                </div>
                <span class="result-grade">${escola.totalAlunos || 0} alunos</span>
            </div>
        `;

    } catch (_) {
        container.innerHTML = '<p style="color:#e74c3c;font-size:12px;padding:8px;">Erro ao carregar escola.</p>';
    }
}

// ── CARREGAR PROFESSORES ────────────────────────────────────────────────────────

async function fillProfessoresRecentes() {
    const container = document.getElementById('professoresRecentesList');
    if (!container) return;

    try {
        const professores = await fetchAPI('/api/coordenador/meus-professores');

        if (!professores.length) {
            container.innerHTML = '<p style="color:#64748b;font-size:12px;padding:8px;">Nenhum professor encontrado.</p>';
            return;
        }

        container.innerHTML = professores.map(p => `
            <div class="result-item">
                <div class="result-info">
                    <div class="result-name">${p.nome}</div>
                    <div class="result-date">${p.disciplina || '—'}</div>
                </div>
                <div class="result-grade">${p.totalTurmas || 0} turmas</div>
            </div>
        `).join('');

    } catch (_) {
        container.innerHTML = '<p style="color:#e74c3c;font-size:12px;padding:8px;">Erro ao carregar professores.</p>';
    }
}

// ── CARREGAR TURMAS ────────────────────────────────────────────────────────────

async function fillTurmas() {
    const container = document.getElementById('turmas-list');
    if (!container) return;

    try {
        const turmas = await fetchAPI('/api/coordenador/minhas-turmas');

        if (!turmas.length) {
            container.innerHTML = '<p style="color:#64748b;font-size:12px;padding:8px;">Nenhuma turma encontrada.</p>';
            return;
        }

        container.innerHTML = turmas.map(t => `
            <div class="exam-item">
                <div class="exam-info">
                    <span class="exam-name">${t.nome} - ${t.serie}</span>
                    <span class="exam-date"><i class='bx bx-group'></i> ${t.totalAlunos || 0} alunos</span>
                </div>
                <span class="result-grade">Média: ${t.mediaGeral || '—'}</span>
            </div>
        `).join('');

    } catch (_) {
        container.innerHTML = '<p style="color:#e74c3c;font-size:12px;padding:8px;">Erro ao carregar turmas.</p>';
    }
}

// ── NAVEGAÇÃO ENTRE PÁGINAS ───────────────────────────────────────────────────

function setupPages() {
    const navLinks = document.querySelectorAll('.nav-item[data-page]');

    const pages = [
        'dashboard', 'escolas', 'professores', 'turmas',
        'provas', 'relatorios', 'calendario', 'configuracoes'
    ];

    navLinks.forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const pageId = link.getAttribute('data-page');

            navLinks.forEach(l => l.classList.remove('active'));
            link.classList.add('active');

            pages.forEach(p => {
                const el = document.getElementById(`${p}-page`);
                if (el) el.style.display = 'none';
            });

            const activePage = document.getElementById(`${pageId}-page`);
            if (activePage) activePage.style.display = 'block';

            if (pageId === 'escolas') fillEscolasRecentes();
            if (pageId === 'professores') fillProfessoresRecentes();
            if (pageId === 'turmas') fillTurmas();
        });
    });

    document.querySelectorAll('.view-all-link').forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            const target = btn.getAttribute('data-page');
            if (target) document.querySelector(`.nav-item[data-page="${target}"]`)?.click();
        });
    });
}

// ── LOGOUT ────────────────────────────────────────────────────────────────────

function initLogout() {
    const logoutBtn = document.getElementById('logout');
    if (!logoutBtn) return;
    logoutBtn.addEventListener('click', (e) => {
        e.preventDefault();
        if (confirm('Deseja sair da sua conta?')) {
            localStorage.removeItem('authToken');
            localStorage.removeItem('usuarioPerfil');
            localStorage.removeItem('usuarioNome');
            window.location.href = '/login.html';
        }
    });
}

// ── PREENCHER HEADER ──────────────────────────────────────────────────────────

function preencherHeaderUsuario(usuario) {
    const avatarEl = document.querySelector('.user-avatar');
    const nomeEl = document.querySelector('.user-name');
    const roleEl = document.querySelector('.user-role');

    if (avatarEl) avatarEl.textContent = (usuario.nome || 'C').charAt(0).toUpperCase();
    if (nomeEl) nomeEl.textContent = usuario.nome || 'Coordenador';
    if (roleEl) roleEl.textContent = 'Coordenador';
}

// ── VERIFICAÇÃO DE AUTENTICAÇÃO ───────────────────────────────────────────────

function verificarAutenticacao() {
    const token = localStorage.getItem('authToken');
    if (!token) {
        window.location.href = '/login.html';
        return false;
    }
    return true;
}

// ── INICIALIZAÇÃO PRINCIPAL ───────────────────────────────────────────────────

document.addEventListener('DOMContentLoaded', () => {
    if (!verificarAutenticacao()) return;

    const token = localStorage.getItem('authToken');
    
    fetch('/api/usuario/logado', {
        headers: { 'Authorization': `Bearer ${token}` }
    })
    .then(response => {
        if (!response.ok) throw new Error('Não autenticado');
        return response.json();
    })
    .then(usuario => {
        preencherHeaderUsuario(usuario);
        inicializarMenuMobile();
        initCoordenacaoCalendar();
        fillEscolasRecentes();
        fillProfessoresRecentes();
        fillTurmas();
        setupPages();
        initLogout();
    })
    .catch(() => {
        localStorage.removeItem('authToken');
        window.location.href = '/login.html';
    });
});