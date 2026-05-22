/**
 * PROVA SERGIPE — coordenacao.js
 * Lógica específica da Coordenação.
 */
'use strict';

// Dados mockados para calendário
const coordenacaoEvents = [
    { date: "2026-05-20", type: "meeting", title: "Reunião com diretores" },
    { date: "2026-05-25", type: "deadline", title: "Prazo lançamento notas" },
    { date: "2026-05-28", type: "exam", title: "Simulado Estadual" }
];
let currentCalendarDate = new Date();

function initCoordenacaoCalendar() {
    renderCalendar('coordenacao-calendar', currentCalendarDate, coordenacaoEvents, (date, events) => {
        if (!events.length) {
            exibirAlerta(`Nenhum evento em ${formatarData(date)}`, 'info');
            return;
        }
        exibirAlerta(`${formatarData(date)} • ${events.map(e => e.title).join(', ')}`, 'sucesso');
    });
    const container = document.getElementById('coordenacao-calendar');
    container?.addEventListener('calendarChange', (e) => { currentCalendarDate = e.detail.date; });
}

// Dados mockados para as listas do dashboard
const escolasRecentes = [
    { nome: "E.E. Professor João Alves", cidade: "Aracaju", professores: 45, alunos: 850 },
    { nome: "E.E. Maria do Carmo", cidade: "Itabaiana", professores: 32, alunos: 620 }
];
const professoresRecentes = [
    { nome: "Paula Santos", escola: "E.E. João Alves", disciplina: "Matemática", turmas: 3 },
    { nome: "Renata Almeida", escola: "E.E. Maria do Carmo", disciplina: "Português", turmas: 2 }
];

function fillEscolasRecentes() {
    const container = document.getElementById('escolasRecentesList');
    if (!container) return;
    container.innerHTML = escolasRecentes.map(e => `
        <div class="exam-item">
            <div class="exam-info">
                <div class="exam-name">${e.nome}</div>
                <div class="exam-date"><i class='bx bx-map'></i> ${e.cidade}</div>
            </div>
            <span>${e.professores} profs • ${e.alunos} alunos</span>
        </div>
    `).join('');
}

function fillProfessoresRecentes() {
    const container = document.getElementById('professoresRecentesList');
    if (!container) return;
    container.innerHTML = professoresRecentes.map(p => `
        <div class="result-item">
            <div class="result-info">
                <div class="result-name">${p.nome}</div>
                <div class="result-date">${p.escola} • ${p.disciplina}</div>
            </div>
            <div class="result-grade">${p.turmas} turmas</div>
        </div>
    `).join('');
}

// Tabela de Escolas
const escolasData = [
    { nome: "E.E. Professor João Alves", cidade: "Aracaju", professores: 45, alunos: 850, status: "Ativo" },
    { nome: "E.E. Maria do Carmo", cidade: "Itabaiana", professores: 32, alunos: 620, status: "Ativo" },
    { nome: "E.E. José de Anchieta", cidade: "Lagarto", professores: 28, alunos: 540, status: "Ativo" }
];

function fillEscolasTable() {
    const container = document.getElementById('escolasTableBody');
    if (!container) return;
    container.innerHTML = escolasData.map(e => `
        <tr>
            <td>${e.nome}</td><td>${e.cidade}</td><td>${e.professores}</td><td>${e.alunos}</td>
            <td><span class="status ativo">${e.status}</span></td>
        </tr>
    `).join('');
}

// Tabela de Professores
const professoresData = [
    { nome: "Paula Santos", escola: "E.E. João Alves", disciplina: "Matemática", turmas: 3, status: "Ativo" },
    { nome: "Renata Almeida", escola: "E.E. Maria do Carmo", disciplina: "Português", turmas: 2, status: "Ativo" },
    { nome: "Carla Mendes", escola: "E.E. José de Anchieta", disciplina: "História", turmas: 2, status: "Ativo" }
];

function fillProfessoresTable() {
    const container = document.getElementById('professoresTableBody');
    if (!container) return;
    container.innerHTML = professoresData.map(p => `
        <tr>
            <td>${p.nome}</td><td>${p.escola}</td><td>${p.disciplina}</td><td>${p.turmas}</td>
            <td><span class="status ativo">${p.status}</span></td>
        </tr>
    `).join('');
}

// Tabela de Turmas
const turmasData = [
    { nome: "6º Ano B", escola: "E.E. João Alves", professor: "Paula Santos", alunos: 32, media: "7.5" },
    { nome: "7º Ano C", escola: "E.E. Maria do Carmo", professor: "Renata Almeida", alunos: 30, media: "9.0" },
    { nome: "8º Ano A", escola: "E.E. José de Anchieta", professor: "Carla Mendes", alunos: 32, media: "8.2" }
];

function fillTurmasTable() {
    const container = document.getElementById('turmasTableBody');
    if (!container) return;
    container.innerHTML = turmasData.map(t => `
        <tr>
            <td>${t.nome}</td><td>${t.escola}</td><td>${t.professor}</td><td>${t.alunos}</td><td>${t.media}</td>
        </tr>
    `).join('');
}

// Tabela de Provas (visão geral)
const provasGeralData = [
    { prova: "Matemática - Bimestral", escola: "E.E. João Alves", turma: "6º Ano B", data: "15/05/2026", participacao: "30/32", media: "7.2" },
    { prova: "Português - Avaliação", escola: "E.E. Maria do Carmo", turma: "7º Ano C", data: "18/05/2026", participacao: "28/30", media: "8.5" }
];

function fillProvasTable() {
    const container = document.getElementById('provasTableBody');
    if (!container) return;
    container.innerHTML = provasGeralData.map(p => `
        <tr>
            <td>${p.prova}</td><td>${p.escola}</td><td>${p.turma}</td><td>${p.data}</td><td>${p.participacao}</td><td>${p.media}</td>
        </tr>
    `).join('');
}

// Navegação entre páginas
function setupPages() {
    const navLinks = document.querySelectorAll('.nav-item[data-page]');
    const pages = ['dashboard', 'escolas', 'professores', 'turmas', 'provas', 'relatorios', 'configuracoes'];
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

            // Recarrega dados específicos da página
            if (pageId === 'escolas') fillEscolasTable();
            else if (pageId === 'professores') fillProfessoresTable();
            else if (pageId === 'turmas') fillTurmasTable();
            else if (pageId === 'provas') fillProvasTable();
        });
    });

    // Botões "Ver todas"
    document.querySelectorAll('.view-all-link').forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            const target = btn.getAttribute('data-page');
            if (target) document.querySelector(`.nav-item[data-page="${target}"]`)?.click();
        });
    });
}

// Ações rápidas
function initQuickActions() {
    document.querySelectorAll('.quick-action-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            const action = btn.getAttribute('data-action');
            exibirAlerta(`Funcionalidade "${action.replace(/-/g, ' ')}" em desenvolvimento`, 'info');
        });
    });
}

// Botões "Novo"
function initNewButtons() {
    const btnEscola = document.getElementById('btnNovaEscola');
    if (btnEscola) btnEscola.addEventListener('click', () => exibirAlerta('Cadastro de nova escola em breve', 'info'));
    const btnProfessor = document.getElementById('btnNovoProfessor');
    if (btnProfessor) btnProfessor.addEventListener('click', () => exibirAlerta('Cadastro de novo professor em breve', 'info'));
    const btnTurma = document.getElementById('btnNovaTurma');
    if (btnTurma) btnTurma.addEventListener('click', () => exibirAlerta('Cadastro de nova turma em breve', 'info'));
}

// Logout
function initLogout() {
    const logoutBtn = document.getElementById('logout');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', (e) => {
            e.preventDefault();
            if (confirm('Deseja sair da sua conta?')) logout();
        });
    }
}

// Inicialização
document.addEventListener('DOMContentLoaded', () => {
    inicializarMenuMobile();      // global.js
    initCoordenacaoCalendar();
    fillEscolasRecentes();
    fillProfessoresRecentes();
    fillEscolasTable();
    fillProfessoresTable();
    fillTurmasTable();
    fillProvasTable();
    setupPages();
    initQuickActions();
    initNewButtons();
    initLogout();
});