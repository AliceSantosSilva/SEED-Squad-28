/**
 * PROVA SERGIPE — prof.js
 * Lógica específica do perfil Professor (compatível com JWT)
 */

'use strict';

// ── FUNÇÃO fetchAPI COM TOKEN JWT ─────────────────────────────────────────────

async function fetchAPI(url, options = {}) {
    const token = localStorage.getItem('authToken');
    const headers = {
        'Content-Type': 'application/json',
        ...options.headers
    };
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

let professorEventosCache = [];

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

async function initProfessorCalendar() {
    let events = [];
    try {
        const token = localStorage.getItem('authToken');
        const res = await fetch('/api/eventos', {
            headers: { 'Authorization': 'Bearer ' + token }
        });
        if (res.ok) {
            const todos = await res.json();
            events = expandirEventos(todos, false); // professor vê tudo
            professorEventosCache = events;
        }
    } catch (_) {}

    renderCalendar('professor-calendar', new Date(), professorEventosCache, (date, evs) => {
        if (!evs.length) { exibirAlerta(`Sem eventos em ${formatarData(date)}`, 'info'); return; }
        exibirAlerta(evs.map(e => e.title).join(' • '), 'sucesso');
    });

    document.getElementById('professor-calendar')
        ?.addEventListener('calendarChange', (e) => {
            currentCalendarDate = e.detail.date;
        });
}

// ── DASHBOARD: PROVAS RECENTES ────────────────────────────────────────────────

async function fillProvasRecentes() {
    const container = document.getElementById('provasRecentesList');
    if (!container) return;

    try {
        const provas = await fetchAPI('/api/professor/minhas-provas/recentes');
        if (!provas.length) {
            container.innerHTML = '<p style="color:#64748b;font-size:12px;padding:8px;">Nenhuma prova recente.</p>';
            return;
        }
        container.innerHTML = provas.map(p => `
            <div class="exam-item">
                <div class="exam-info">
                    <div class="exam-name">${p.titulo}</div>
                    <div class="exam-date"><i class='bx bx-calendar'></i> ${p.data || '—'}</div>
                </div>
                <span class="exam-progress">${p.respondidos || 0}/${p.total || 0} responderam</span>
            </div>
        `).join('');
    } catch (_) {
        container.innerHTML = '<p style="color:#64748b;font-size:12px;padding:8px;">Nenhuma prova recente.</p>';
    }
}

// ── DASHBOARD: TURMAS ─────────────────────────────────────────────────────────

async function fillTurmasList() {
    const container = document.getElementById('turmasList');
    if (!container) return;

    try {
        const turmas = await fetchAPI('/api/professor/minhas-turmas');
        if (!turmas.length) {
            container.innerHTML = '<p style="color:#64748b;font-size:12px;padding:8px;">Nenhuma turma encontrada.</p>';
            return;
        }
        container.innerHTML = turmas.map(t => `
            <div class="result-item">
                <div class="result-info">
                    <div class="result-name">${t.nome}</div>
                    <div class="result-date">${t.alunos || 0} alunos</div>
                </div>
                <div class="result-grade">${t.media || '—'}</div>
            </div>
        `).join('');
    } catch (_) {
        container.innerHTML = '<p style="color:#64748b;font-size:12px;padding:8px;">Erro ao carregar turmas.</p>';
    }
}

// ── PÁGINA PROVAS (tabela) ────────────────────────────────────────────────────

async function renderProvasTable(filtro = 'todas') {
    const container = document.getElementById('provasTableBody');
    if (!container) return;

    try {
        let provas = await fetchAPI('/api/professor/minhas-provas');
        if (filtro === 'ativas')     provas = provas.filter(p => p.status === 'Ativa');
        else if (filtro === 'encerradas') provas = provas.filter(p => p.status === 'Encerrada');
        else if (filtro === 'rascunhos') provas = provas.filter(p => p.status === 'Rascunho');

        if (!provas.length) {
            container.innerHTML = '<tr><td colspan="6" style="padding:24px;color:#64748b;">Nenhuma prova encontrada.</td></tr>';
            return;
        }
        const statusClass = { 'Ativa': 'ativa', 'Encerrada': 'encerrada', 'Rascunho': 'rascunho' };
        container.innerHTML = provas.map(p => `
            <tr style="border-bottom:1px solid #eef2f8;">
                <td style="padding:12px 16px;">
                    <div class="prova-info">
                        <div class="prova-icon"><i class='bx bx-file'></i></div>
                        <span>${p.titulo}</span>
                    </div>
                </td>
                <td style="padding:12px 16px;">${p.turma || '—'}</td>
                <td style="padding:12px 16px;">${p.data || '—'}</td>
                <td style="padding:12px 16px;">${p.questoes || 0}</td>
                <td style="padding:12px 16px;">${p.participacao || '0/0'}</td>
                <td style="padding:12px 16px;">
                    <span class="status ${statusClass[p.status] || 'rascunho'}">${p.status}</span>
                </td>
            </tr>
        `).join('');
    } catch (_) {
        container.innerHTML = '<tr><td colspan="6" style="padding:24px;color:#64748b;">Nenhuma prova encontrada.</td></tr>';
    }
}

function setupProvasFilters() {
    const filters = document.querySelectorAll('.filter-btn');
    filters.forEach(btn => {
        btn.addEventListener('click', () => {
            filters.forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            renderProvasTable(btn.getAttribute('data-filter'));
        });
    });
}

// ── PÁGINA TURMAS (cards) ─────────────────────────────────────────────────────

async function fillTurmasCards() {
    const container = document.getElementById('turmasGrid');
    if (!container) return;

    try {
        const turmas = await fetchAPI('/api/professor/minhas-turmas');
        if (!turmas.length) {
            container.innerHTML = '<p style="color:#64748b;padding:16px;">Nenhuma turma encontrada.</p>';
            return;
        }
        container.innerHTML = turmas.map(t => `
            <div class="turma-card">
                <div class="turma-card-top">
                    <div>
                        <h2>${t.nome}</h2>
                        <p>${t.disciplina || 'Matemática'}</p>
                    </div>
                    <span class="turma-status">Ativa</span>
                </div>
                <div class="turma-info-grid">
                    <div class="turma-info-box">
                        <strong>${t.alunos || 0}</strong>
                        <span>Alunos</span>
                    </div>
                    <div class="turma-info-box">
                        <strong class="green-text">${t.media || '—'}</strong>
                        <span>Média</span>
                    </div>
                    <div class="turma-info-box">
                        <strong>${t.ultimaProva || '—'}</strong>
                        <span>Última Prova</span>
                    </div>
                </div>
                <button class="turma-btn" data-turma-id="${t.id}">Ver Detalhes</button>
            </div>
        `).join('');

        document.querySelectorAll('.turma-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                exibirAlerta('Detalhes da turma em desenvolvimento.', 'info');
            });
        });
    } catch (_) {
        container.innerHTML = '<p style="color:#64748b;padding:16px;">Erro ao carregar turmas.</p>';
    }
}

// ── PÁGINA RESULTADOS ─────────────────────────────────────────────────────────

async function fillResultadosTable() {
    const container = document.getElementById('resultadosTableBody');
    if (!container) return;

    try {
        const resultados = await fetchAPI('/api/professor/meus-resultados');
        if (!resultados.length) {
            container.innerHTML = '<tr><td colspan="5" style="padding:24px;color:#64748b;">Nenhum resultado encontrado.</td></tr>';
            return;
        }
        const statusClass = { 'Excelente': 'ativa', 'Regular': 'encerrada', 'Baixo': 'rascunho' };
        container.innerHTML = resultados.map(r => `
            <tr style="border-bottom:1px solid #eef2f8;">
                <td style="padding:12px 16px;">${r.turma}</td>
                <td style="padding:12px 16px;">${r.prova}</td>
                <td style="padding:12px 16px;">${r.media}</td>
                <td style="padding:12px 16px;">${r.participacao}</td>
                <td style="padding:12px 16px;">
                    <span class="status ${statusClass[r.status] || 'rascunho'}">${r.status}</span>
                </td>
            </tr>
        `).join('');
    } catch (_) {
        container.innerHTML = '<tr><td colspan="5" style="padding:24px;color:#64748b;">Nenhum resultado encontrado.</td></tr>';
    }
}

// ── NAVEGAÇÃO ENTRE PÁGINAS ───────────────────────────────────────────────────

let calendarioInicializado = false;

function setupPages() {
    const navLinks = document.querySelectorAll('.nav-item[data-page]');
    const pages = ['dashboard', 'provas', 'turmas', 'banco', 'resultados', 'calendario', 'configuracoes'];

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

            if (pageId === 'provas')      renderProvasTable();
            else if (pageId === 'turmas') fillTurmasCards();
            else if (pageId === 'resultados') fillResultadosTable();
            else if (pageId === 'calendario') {
                // Inicia o calendário completo só na primeira vez
                if (!calendarioInicializado) {
                    Calendario.init('PROFESSOR');
                    calendarioInicializado = true;
                }
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
    if (logoutBtn) {
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
}

// ── PREENCHER HEADER ──────────────────────────────────────────────────────────

function preencherHeaderUsuario(usuario) {
    const avatarEl = document.querySelector('.user-avatar');
    const nomeEl   = document.querySelector('.user-name');
    const roleEl   = document.querySelector('.user-role');
    if (avatarEl) avatarEl.textContent = (usuario.nome || 'P').charAt(0).toUpperCase();
    if (nomeEl)   nomeEl.textContent   = usuario.nome || 'Professor';
    if (roleEl)   roleEl.textContent   = 'Professor';
}

// ── VERIFICAÇÃO DE AUTENTICAÇÃO ───────────────────────────────────────────────

function verificarAutenticacao() {
    const token = localStorage.getItem('authToken');
    if (!token) { window.location.href = '/login.html'; return false; }
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
        initProfessorCalendar();
        fillProvasRecentes();
        fillTurmasList();
        renderProvasTable();
        fillTurmasCards();
        fillResultadosTable();
        setupPages();
        setupProvasFilters();
        initLogout();
    })
    .catch(() => {
        localStorage.removeItem('authToken');
        window.location.href = '/login.html';
    });
});