/**
 * PROVA SERGIPE — aluno.js
 * Lógica específica do perfil Aluno.
 */

'use strict';

// ── Calendário ────────────────────────────────────────────────────────────────

const eventsData = [
    { date: "2026-06-05", type: "exam",     title: "Matemática - Prova Bimestral" },
    { date: "2026-06-10", type: "exam",     title: "Português - Avaliação" },
    { date: "2026-06-15", type: "deadline", title: "Entrega de Trabalho" },
    { date: "2026-06-20", type: "meeting",  title: "Reunião de Pais" }
];

let currentCalendarDate = new Date();

function initCalendar() {
    renderCalendar('calendarContainer', currentCalendarDate, eventsData, (date, events) => {
        if (!events.length) {
            exibirAlerta('Sem eventos nesta data.', 'info');
            return;
        }
        exibirAlerta(events.map(e => e.title).join(' • '), 'info');
    });
    const container = document.getElementById('calendarContainer');
    container?.addEventListener('calendarChange', (e) => {
        currentCalendarDate = e.detail.date;
    });
}

// ── Stat-cards (dados reais) ──────────────────────────────────────────────────

async function carregarDashboard() {
    try {
        const data = await fetchAPI('/api/aluno/dashboard');

        document.querySelector('.stat-card.primary .stat-value').textContent =
            data.provasPendentes;

        document.querySelector('.stat-card.success .stat-value').textContent =
            data.provasRealizadas;

        document.querySelector('.stat-card.warning .stat-value').textContent =
            data.mediaGeral.toFixed(1);

        document.querySelector('.stat-card.info .stat-value2').textContent =
            data.totalProvas;

    } catch (_) {
        exibirAlerta('Erro ao carregar estatísticas.', 'erro');
    }
}

// ── Provas pendentes (dados reais) ────────────────────────────────────────────

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
                    <div class="exam-date">
                        <i class='bx bx-calendar'></i>
                        ${p.dataInicio ? new Date(p.dataInicio).toLocaleDateString('pt-BR') : 'Sem data'}
                    </div>
                </div>
                <button class="btn-start" data-prova-id="${p.id}">
                    Iniciar Prova <i class='bx bx-play'></i>
                </button>
            </div>
        `).join('');

        // Botões de iniciar prova
        container.querySelectorAll('.btn-start').forEach(btn => {
            btn.addEventListener('click', () => {
                const provaId = btn.getAttribute('data-prova-id');
                exibirAlerta('Tela de prova em desenvolvimento.', 'aviso');
                // Futuramente: window.location.href = `/prova.html?id=${provaId}`;
            });
        });

    } catch (_) {
        container.innerHTML = '<p style="color:#e74c3c;font-size:12px;padding:8px;">Erro ao carregar provas.</p>';
    }
}

// ── Resultados recentes (dados reais) ─────────────────────────────────────────

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
            const data   = r.dataRealizado
                ? new Date(r.dataRealizado).toLocaleDateString('pt-BR')
                : '—';
            return `
                <div class="result-item">
                    <div class="result-info">
                        <div class="result-name">${r.tituloProva}</div>
                        <div class="result-date">${data}</div>
                    </div>
                    <div class="result-grade ${classe}">${r.nota.toFixed(1)}</div>
                </div>
            `;
        }).join('');

    } catch (_) {
        container.innerHTML = '<p style="color:#e74c3c;font-size:12px;padding:8px;">Erro ao carregar resultados.</p>';
    }
}

// ── Página Minhas Provas (dados reais) ────────────────────────────────────────

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
                    <div class="exam-date">
                        <i class='bx bx-calendar'></i>
                        ${p.dataInicio ? new Date(p.dataInicio).toLocaleDateString('pt-BR') : 'Sem data'}
                    </div>
                </div>
                <button class="btn-start" data-prova-id="${p.id}">
                    Iniciar <i class='bx bx-play'></i>
                </button>
            </div>
        `).join('');

        container.querySelectorAll('.btn-start').forEach(btn => {
            btn.addEventListener('click', () => {
                exibirAlerta('Tela de prova em desenvolvimento.', 'aviso');
            });
        });

    } catch (_) {
        container.innerHTML = '<p style="color:#e74c3c;padding:16px;">Erro ao carregar provas.</p>';
    }
}

// ── Página Resultados (dados reais) ───────────────────────────────────────────

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
            const data   = r.dataRealizado
                ? new Date(r.dataRealizado).toLocaleDateString('pt-BR')
                : '—';
            return `
                <div class="result-item">
                    <div class="result-info">
                        <div class="result-name">${r.tituloProva}</div>
                        <div class="result-date">${data}</div>
                    </div>
                    <div class="result-grade ${classe}">${r.nota.toFixed(1)}</div>
                </div>
            `;
        }).join('');

    } catch (_) {
        container.innerHTML = '<p style="color:#e74c3c;padding:16px;">Erro ao carregar resultados.</p>';
    }
}

// ── Navegação entre páginas ───────────────────────────────────────────────────

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

            // Carrega dados ao navegar
            if (pageId === 'minhas-provas') fillFullExams();
            if (pageId === 'resultados')    fillFullResults();
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

function initLogout() {
    const logoutBtn = document.getElementById('logout');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', (e) => {
            e.preventDefault();
            if (confirm('Deseja sair da sua conta?')) logout();
        });
    }
}

// ── Inicialização ─────────────────────────────────────────────────────────────

document.addEventListener('DOMContentLoaded', () => {
    verificarSessao((usuario) => {
        preencherHeaderUsuario(usuario);
        inicializarMenuMobile();
        initCalendar();
        carregarDashboard();
        carregarProvasPendentes();
        carregarResultados();
        setupPages();
        initLogout();
    });
});