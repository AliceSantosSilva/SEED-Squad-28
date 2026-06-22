/**
 * PROVA SERGIPE — aluno.js
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

// ── CALENDÁRIO MINI DO DASHBOARD (estático, só visual) ────────────────────────

const eventsData = [
    { date: "2026-06-05", type: "exam", title: "Matemática - Prova Bimestral" },
    { date: "2026-06-10", type: "exam", title: "Português - Avaliação" },
];

let currentCalendarDate = new Date();

function initCalendar() {
    renderCalendar('calendarContainer', currentCalendarDate, eventsData, (date, events) => {
        if (!events.length) { exibirAlerta('Sem eventos nesta data.', 'info'); return; }
        exibirAlerta(events.map(e => e.title).join(' • '), 'info');
    });
    const container = document.getElementById('calendarContainer');
    container?.addEventListener('calendarChange', (e) => { currentCalendarDate = e.detail.date; });
}

// ── DASHBOARD ─────────────────────────────────────────────────────────────────

async function carregarDashboard() {
    try {
        const data = await fetchAPI('/api/aluno/dashboard');
        const pendentesEl  = document.querySelector('.stat-card.primary .stat-value');
        const realizadasEl = document.querySelector('.stat-card.success .stat-value');
        const mediaEl      = document.querySelector('.stat-card.warning .stat-value');
        const totalEl      = document.querySelector('.stat-card.info .stat-value2');
        if (pendentesEl)  pendentesEl.textContent  = data.provasPendentes ?? 0;
        if (realizadasEl) realizadasEl.textContent = data.provasRealizadas ?? 0;
        if (mediaEl)      mediaEl.textContent      = data.mediaGeral ? data.mediaGeral.toFixed(1) : '—';
        if (totalEl)      totalEl.textContent      = data.totalProvas ?? 0;
    } catch (_) {}
}

async function carregarProvasPendentes() {
    const container = document.getElementById('exams-list-dashboard');
    if (!container) return;
    try {
        const provas = await fetchAPI('/api/aluno/provas/pendentes');
        if (!provas.length) {
            container.innerHTML = '<p style="color:#64748b;font-size:12px;padding:8px;">Nenhuma prova pendente.</p>';
            return;
        }
        container.innerHTML = provas.map(p => `
            <div class="exam-item">
                <div class="exam-info">
                    <div class="exam-name">${p.titulo}</div>
                    <div class="exam-date"><i class='bx bx-calendar'></i>
                        ${p.dataInicio ? new Date(p.dataInicio).toLocaleDateString('pt-BR') : 'Sem data'}
                    </div>
                </div>
                <button class="btn-start" data-prova-id="${p.id}">Iniciar Prova <i class='bx bx-play'></i></button>
            </div>`).join('');

        container.querySelectorAll('.btn-start').forEach(btn => {
            btn.addEventListener('click', () => exibirAlerta('Tela de prova em desenvolvimento.', 'aviso'));
        });
    } catch (_) {
        container.innerHTML = '<p style="color:#e74c3c;font-size:12px;padding:8px;">Erro ao carregar provas.</p>';
    }
}

async function carregarResultados() {
    const container = document.getElementById('results-list-dashboard');
    if (!container) return;
    try {
        const resultados = await fetchAPI('/api/aluno/provas/realizadas');
        if (!resultados.length) {
            container.innerHTML = '<p style="color:#64748b;font-size:12px;padding:8px;">Nenhuma prova realizada ainda.</p>';
            return;
        }
        container.innerHTML = resultados.map(r => {
            const classe = r.nota >= 7 ? 'good' : r.nota >= 5 ? 'average' : 'bad';
            const data   = r.dataRealizado ? new Date(r.dataRealizado).toLocaleDateString('pt-BR') : '—';
            return `
                <div class="result-item">
                    <div class="result-info">
                        <div class="result-name">${r.tituloProva}</div>
                        <div class="result-date">${data}</div>
                    </div>
                    <div class="result-grade ${classe}">${r.nota.toFixed(1)}</div>
                </div>`;
        }).join('');
    } catch (_) {
        container.innerHTML = '<p style="color:#e74c3c;font-size:12px;padding:8px;">Erro ao carregar resultados.</p>';
    }
}

// ── PÁGINAS COMPLETAS ─────────────────────────────────────────────────────────

async function fillFullExams() {
    const container = document.getElementById('allExamsList');
    if (!container) return;
    try {
        const provas = await fetchAPI('/api/aluno/provas/pendentes');
        if (!provas.length) {
            container.innerHTML = '<p style="color:#64748b;padding:16px;">Nenhuma prova disponível.</p>';
            return;
        }
        container.innerHTML = provas.map(p => `
            <div class="exam-item">
                <div class="exam-info">
                    <div class="exam-name">${p.titulo}</div>
                    <div class="exam-date"><i class='bx bx-calendar'></i>
                        ${p.dataInicio ? new Date(p.dataInicio).toLocaleDateString('pt-BR') : 'Sem data'}
                    </div>
                </div>
                <button class="btn-start" data-prova-id="${p.id}">Iniciar <i class='bx bx-play'></i></button>
            </div>`).join('');
        container.querySelectorAll('.btn-start').forEach(btn => {
            btn.addEventListener('click', () => exibirAlerta('Tela de prova em desenvolvimento.', 'aviso'));
        });
    } catch (_) {
        container.innerHTML = '<p style="color:#e74c3c;padding:16px;">Erro ao carregar provas.</p>';
    }
}

async function fillFullResults() {
    const container = document.getElementById('allResultsList');
    if (!container) return;
    try {
        const resultados = await fetchAPI('/api/aluno/provas/realizadas');
        if (!resultados.length) {
            container.innerHTML = '<p style="color:#64748b;padding:16px;">Nenhum resultado disponível.</p>';
            return;
        }
        container.innerHTML = resultados.map(r => {
            const classe = r.nota >= 7 ? 'good' : r.nota >= 5 ? 'average' : 'bad';
            const data   = r.dataRealizado ? new Date(r.dataRealizado).toLocaleDateString('pt-BR') : '—';
            return `
                <div class="result-item">
                    <div class="result-info">
                        <div class="result-name">${r.tituloProva}</div>
                        <div class="result-date">${data}</div>
                    </div>
                    <div class="result-grade ${classe}">${r.nota.toFixed(1)}</div>
                </div>`;
        }).join('');
    } catch (_) {
        container.innerHTML = '<p style="color:#e74c3c;padding:16px;">Erro ao carregar resultados.</p>';
    }
}

// ── NAVEGAÇÃO ─────────────────────────────────────────────────────────────────

let calendarioInicializado = false;

function setupPages() {
    const navLinks = document.querySelectorAll('.nav-item[data-page]');
    const pages    = ['dashboard', 'minhas-provas', 'resultados', 'calendario', 'configuracoes'];

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

            if (pageId === 'minhas-provas') fillFullExams();
            if (pageId === 'resultados')    fillFullResults();
            if (pageId === 'calendario' && !calendarioInicializado) {
                // ALUNO: passa perfil 'ALUNO' — o calendário filtra e esconde PERIODO_PROVA
                Calendario.init('ALUNO');
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

// ── LOGOUT / HEADER ───────────────────────────────────────────────────────────

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

function preencherHeaderUsuario(usuario) {
    const avatarEl = document.querySelector('.user-avatar');
    const nomeEl   = document.querySelector('.user-name');
    const roleEl   = document.querySelector('.user-role');
    const welcomeH1 = document.querySelector('.welcome h1');
    if (avatarEl)   avatarEl.textContent  = (usuario.nome || 'A').charAt(0).toUpperCase();
    if (nomeEl)     nomeEl.textContent    = usuario.nome || 'Aluno';
    if (roleEl)     roleEl.textContent    = 'Aluno';
    if (welcomeH1)  welcomeH1.textContent = `Olá, ${(usuario.nome || 'Aluno').split(' ')[0]}!`;
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
        initCalendar();
        carregarDashboard();
        carregarProvasPendentes();
        carregarResultados();
        setupPages();
        initLogout();
    })
    .catch(() => {
        localStorage.removeItem('authToken');
        window.location.href = '/login.html';
    });
});