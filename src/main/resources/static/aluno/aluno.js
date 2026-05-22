/**
 * PROVA SERGIPE — aluno.js
 * Lógica específica do perfil Aluno.
 * Depende de: global.js (carregado antes via defer).
 */

'use strict';

// ─── Dados de eventos do calendário ───────────────────────────────────────────
const eventsData = [
    { date: "2026-04-15", type: "exam",     title: "Matemática - Prova Bimestral" },
    { date: "2026-04-18", type: "exam",     title: "Português - Avaliação"         },
    { date: "2026-04-22", type: "exam",     title: "História - Prova"              },
    { date: "2026-04-10", type: "deadline", title: "Entrega de Trabalho"           },
    { date: "2026-04-20", type: "deadline", title: "Prazo de Inscrição"            },
    { date: "2026-04-25", type: "meeting",  title: "Reunião de Pais"               }
];

let currentCalendarDate = new Date();

function initCalendar() {

    renderCalendar(
        'calendarContainer',
        currentCalendarDate,
        eventsData,
        (date, events) => {

            if (!events.length) {
                exibirAlerta('Sem eventos nesta data.', 'info');
                return;
            }

            exibirAlerta(
                events.map(e => e.title).join(' • '),
                'info'
            );
        }
    );

    const container = document.getElementById('calendarContainer');

    container?.addEventListener('calendarChange', (e) => {
        currentCalendarDate = e.detail.date;
    });
}

// ─── Troca de páginas ─────────────────────────────────────────────────────────
function setupPages() {
    const navLinks = document.querySelectorAll('.nav-item[data-page]');
    const pages    = ['dashboard', 'minhas-provas', 'resultados', 'calendario', 'configuracoes'];

    navLinks.forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const pageId = link.getAttribute('data-page');

            // Ativa link clicado
            navLinks.forEach(l => l.classList.remove('active'));
            link.classList.add('active');

            // Esconde todas as páginas e exibe a alvo
            pages.forEach(p => {
                const el = document.getElementById(`${p}-page`);
                if (el) el.style.display = 'none';
            });
            const activePage = document.getElementById(`${pageId}-page`);
            if (activePage) activePage.style.display = 'block';
        });
    });

    // Botões "Ver todas" acionam a navegação correspondente
    document.querySelectorAll('.view-all-link').forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            const target = btn.getAttribute('data-page');
            if (target) {
                document.querySelector(`.nav-item[data-page="${target}"]`)?.click();
            }
        });
    });
}

// ─── Listas completas ─────────────────────────────────────────────────────────
function fillFullExams() {
    // Substituir futuramente por: fetchAPI('/api/aluno/provas', ...)
    const examsFull = [
        { name: "Matemática - Prova Bimestral", date: "15/04/2026 08:00", status: "pendente"  },
        { name: "Português - Avaliação",         date: "18/04/2026 10:00", status: "pendente"  },
        { name: "História - Prova",              date: "22/04/2026 14:00", status: "pendente"  },
        { name: "Geografia - Final",             date: "10/03/2026",       status: "realizada", grade: "8.5" },
        { name: "Ciências - Prova",              date: "05/03/2026",       status: "realizada", grade: "7.8" }
    ];

    const container = document.getElementById('allExamsList');
    if (!container) return;

    container.innerHTML = examsFull.map(ex => `
        <div class="exam-item" style="justify-content:space-between;">
            <div>
                <strong>${ex.name}</strong><br>
                <span style="font-size:12px;">${ex.date}</span>
            </div>
            ${ex.status === 'pendente'
                ? '<button class="btn-start">Iniciar</button>'
                : `<span class="result-grade good">Nota: ${ex.grade}</span>`
            }
        </div>
    `).join('');
}

function fillFullResults() {
    // Substituir futuramente por: fetchAPI('/api/aluno/resultados', ...)
    const allResults = [
        { name: "Matemática Parcial",   date: "15/03/2026", grade: 8.5 },
        { name: "Português Redação",    date: "10/03/2026", grade: 9.0 },
        { name: "História Trabalho",    date: "05/03/2026", grade: 7.5 },
        { name: "Física - Laboratório", date: "25/02/2026", grade: 9.2 }
    ];

    const container = document.getElementById('allResultsList');
    if (!container) return;

    container.innerHTML = allResults.map(r => `
        <div class="result-item" style="justify-content:space-between;">
            <div>
                <strong>${r.name}</strong><br>
                <span>${r.date}</span>
            </div>
            <div class="result-grade good">${r.grade}</div>
        </div>
    `).join('');
}

// ─── Menu mobile ──────────────────────────────────────────────────────────────
function mobileMenu() {
    const btn     = document.getElementById('menuBtn');
    const sidebar = document.querySelector('.sidebar');
    btn?.addEventListener('click', () => sidebar.classList.toggle('open'));
    document.addEventListener('click', (e) => {
        if (sidebar?.classList.contains('open') && !sidebar.contains(e.target) && !btn?.contains(e.target)) {
            sidebar.classList.remove('open');
        }
    });
}

// ─── Botões "Iniciar prova" ────────────────────────────────────────────────────
function initExamButtons() {
    document.querySelectorAll('.btn-start').forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.stopPropagation();
            // Usa exibirAlerta do global.js se disponível, senão alert nativo
            if (typeof exibirAlerta === 'function') {
                exibirAlerta('🚀 Iniciando prova. Função em desenvolvimento.', 'aviso');
            } else {
                alert('🚀 Iniciando simulado/prova. Função em desenvolvimento.');
            }
        });
    });
}


// ─── Logout ───────────────────────────────────────────────────────────────────
function initLogout() {
    document.getElementById('logout')?.addEventListener('click', (e) => {
        e.preventDefault();
        if (confirm('Deseja sair da sua conta?')) {
            // Usa logout() do global.js se disponível
            if (typeof logout === 'function') {
                logout();
            } else {
                alert('Até logo, João!');
            }
        }
    });
}

// ─── Inicialização ────────────────────────────────────────────────────────────
function init() {
    initCalendar();
    setupPages();
    fillFullExams();
    fillFullResults();
    mobileMenu();
    initExamButtons();
    initLogout();
}

init();