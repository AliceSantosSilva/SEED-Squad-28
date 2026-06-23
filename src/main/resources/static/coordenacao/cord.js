/**
 * PROVA SERGIPE — cord.js (Coordenador)
 */

'use strict';

async function fetchAPI(url, options = {}) {
    const token = localStorage.getItem('authToken');
    const headers = { 'Content-Type': 'application/json', ...options.headers };
    if (token) headers['Authorization'] = `Bearer ${token}`;

    const response = await fetch(url, { ...options, headers });

    if (response.status === 401) {
        localStorage.removeItem('authToken');
        localStorage.removeItem('usuarioPerfil');
        localStorage.removeItem('usuarioNome');
        window.location.href = '/login.html';
        throw new Error('Sessão expirada');
    }

    return response.json();
}

// ── CALENDÁRIO MINI DO DASHBOARD ──────────────────────────────────────────────

let coordEventosCache = [];

// Expande eventos com período em múltiplos dias
function expandirEventos(todos, filtrarPeriodo) {
    const result = [];
    todos.forEach(e => {
        if (filtrarPeriodo && e.tipo === 'PERIODO_PROVA') return;
        const tipo = e.tipo === 'PROVA' ? 'exam' : e.tipo === 'PERIODO_PROVA' ? 'deadline' : 'meeting';
        const inicio = new Date(e.dataInicio + 'T00:00:00');
        const fim    = new Date(e.dataFim    + 'T00:00:00');
        let d = new Date(inicio);
        while (d <= fim) {
            result.push({
                date: d.toISOString().split('T')[0],
                type: tipo,
                title: e.titulo
            });
            d.setDate(d.getDate() + 1);
        }
    });
    return result;
}

async function initCoordenacaoCalendar() {
    let events = [];
    try {
        const token = localStorage.getItem('authToken');
        const res = await fetch('/api/eventos', {
            headers: { 'Authorization': 'Bearer ' + token }
        });
        if (res.ok) {
            const todos = await res.json();
            events = expandirEventos(todos, false); // coordenador vê tudo
            coordEventosCache = events;
        }
    } catch (_) {}

    renderCalendar('coordenacao-calendar', new Date(), coordEventosCache, (date, evs) => {
        if (!evs.length) { exibirAlerta(`Sem eventos em ${formatarData(date)}`, 'info'); return; }
        exibirAlerta(evs.map(e => e.title).join(' • '), 'sucesso');
    });

    document.getElementById('coordenacao-calendar')
        ?.addEventListener('calendarChange', (e) => {
            currentCalendarDate = e.detail.date;
        });
}

// ── DASHBOARD ─────────────────────────────────────────────────────────────────

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
            </div>`;
    } catch (_) {
        container.innerHTML = '<p style="color:#e74c3c;font-size:12px;padding:8px;">Erro ao carregar escola.</p>';
    }
}

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
            </div>`).join('');
    } catch (_) {
        container.innerHTML = '<p style="color:#e74c3c;font-size:12px;padding:8px;">Erro ao carregar professores.</p>';
    }
}

async function fillTurmas() {
    const container = document.getElementById('turmasTableBody');
    if (!container) return;
    try {
        const turmas = await fetchAPI('/api/coordenador/minhas-turmas');
        if (!turmas.length) {
            container.innerHTML = '<tr><td colspan="5" style="padding:24px;color:#64748b;">Nenhuma turma encontrada.</td></tr>';
            return;
        }
        container.innerHTML = turmas.map(t => `
            <tr style="border-bottom:1px solid #eef2f8;">
                <td style="padding:12px 16px;">${t.nome} - ${t.serie || ''}</td>
                <td style="padding:12px 16px;">${t.escola || '—'}</td>
                <td style="padding:12px 16px;">${t.professor || '—'}</td>
                <td style="padding:12px 16px;">${t.totalAlunos || 0}</td>
                <td style="padding:12px 16px;">${t.mediaGeral || '—'}</td>
            </tr>`).join('');
    } catch (_) {
        container.innerHTML = '<tr><td colspan="5" style="padding:24px;color:#64748b;">Erro ao carregar turmas.</td></tr>';
    }
}

// ── NAVEGAÇÃO ─────────────────────────────────────────────────────────────────

let calendarioInicializado = false;

function setupPages() {
    const navLinks = document.querySelectorAll('.nav-item[data-page]');
    const pages = ['dashboard', 'escolas', 'professores', 'turmas', 'provas', 'relatorios', 'calendario', 'configuracoes'];

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

            if (pageId === 'escolas')     fillEscolasRecentes();
            if (pageId === 'professores') fillProfessoresRecentes();
            if (pageId === 'turmas')      fillTurmas();
            if (pageId === 'calendario' && !calendarioInicializado) {
                Calendario.init('COORDENADOR');
                calendarioInicializado = true;
            }
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

// ── HEADER ────────────────────────────────────────────────────────────────────

function preencherHeaderUsuario(usuario) {
    const avatarEl = document.querySelector('.user-avatar');
    const nomeEl   = document.querySelector('.user-name');
    const roleEl   = document.querySelector('.user-role');
    if (avatarEl) avatarEl.textContent = (usuario.nome || 'C').charAt(0).toUpperCase();
    if (nomeEl)   nomeEl.textContent   = usuario.nome || 'Coordenador';
    if (roleEl)   roleEl.textContent   = 'Coordenação';
}

function verificarAutenticacao() {
    const token = localStorage.getItem('authToken');
    if (!token) { window.location.href = '/login.html'; return false; }
    return true;
}

// ── INIT ──────────────────────────────────────────────────────────────────────

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
        setupPages();
        initLogout();
    })
    .catch(() => {
        localStorage.removeItem('authToken');
        window.location.href = '/login.html';
    });
});