/**
 * PROVA SERGIPE — cord.js
 * Lógica específica do perfil Coordenador.
 */

'use strict';

const coordEvents = [
    { date: "2026-06-05", type: "exam",     title: "Prova Matemática - 9º Ano"       },
    { date: "2026-06-10", type: "meeting",  title: "Reunião de Coordenadores"         },
    { date: "2026-06-15", type: "deadline", title: "Prazo entrega de planejamento"    },
    { date: "2026-06-20", type: "exam",     title: "Simulado Estadual"                }
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

function fillEscolasRecentes() {
    const escolas = [
        { nome: "Escola Estadual João Alves", cidade: "Aracaju",   alunos: 850 },
        { nome: "E.E. Maria do Carmo",        cidade: "Itabaiana", alunos: 620 },
        { nome: "Colégio Estadual Vitória",   cidade: "Vitória",   alunos: 540 }
    ];
    const container = document.getElementById('escolasRecentesList');
    if (!container) return;
    container.innerHTML = escolas.map(e => `
        <div class="exam-item">
            <div class="exam-info">
                <span class="exam-name">${e.nome}</span>
                <span class="exam-date"><i class='bx bx-map'></i> ${e.cidade}</span>
            </div>
            <span class="result-grade">${e.alunos} alunos</span>
        </div>
    `).join('');
}

function fillProfessoresRecentes() {
    const professores = [
        { nome: "Paula Santos",    disciplina: "Matemática", turmas: 4 },
        { nome: "Carlos Fonseca",  disciplina: "Português",  turmas: 3 },
        { nome: "Fernanda Souza",  disciplina: "História",   turmas: 3 }
    ];
    const container = document.getElementById('professoresRecentesList');
    if (!container) return;
    container.innerHTML = professores.map(p => `
        <div class="result-item">
            <div class="result-info">
                <div class="result-name">${p.nome}</div>
                <div class="result-date">${p.disciplina}</div>
            </div>
            <div class="result-grade">${p.turmas} turmas</div>
        </div>
    `).join('');
}

function setupPages() {
    const navLinks = document.querySelectorAll('.nav-item[data-page]');

    // CORRIGIDO: 'provas' estava faltando — cord.html tem data-page="provas" na nav
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

            if (pageId === 'escolas')     fillEscolasRecentes();
            if (pageId === 'professores') fillProfessoresRecentes();
        });
    });

    // Links "Ver todas" dentro dos cards
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
    if (!logoutBtn) return;
    logoutBtn.addEventListener('click', (e) => {
        e.preventDefault();
        if (confirm('Deseja sair da sua conta?')) logout();
    });
}

document.addEventListener('DOMContentLoaded', () => {
    verificarSessao((usuario) => {
        preencherHeaderUsuario(usuario);
        inicializarMenuMobile();
        initCoordenacaoCalendar();
        fillEscolasRecentes();
        fillProfessoresRecentes();
        setupPages();
        initLogout();
    });
});