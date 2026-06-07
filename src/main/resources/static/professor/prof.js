/**
 * PROVA SERGIPE — professor.js
 * Lógica específica do perfil Professor.
 */

'use strict';

// Dados mockados para calendário
const professorEvents = [
    { date: "2026-06-05", type: "exam", title: "Prova Matemática - 9º Ano" },
    { date: "2026-06-10", type: "meeting", title: "Reunião Pedagógica" },
    { date: "2026-06-15", type: "deadline", title: "Prazo lançamento notas" },
    { date: "2026-06-20", type: "exam", title: "Simulado Estadual" }
];

let currentCalendarDate = new Date();

function initProfessorCalendar() {
    renderCalendar('professor-calendar', currentCalendarDate, professorEvents, (date, events) => {
        if (!events.length) {
            exibirAlerta(`Nenhum evento em ${formatarData(date)}`, 'info');
            return;
        }
        exibirAlerta(`${formatarData(date)} • ${events.map(e => e.title).join(', ')}`, 'sucesso');
    });
    const container = document.getElementById('professor-calendar');
    container?.addEventListener('calendarChange', (e) => { currentCalendarDate = e.detail.date; });
}

// Dashboard: provas recentes
function fillProvasRecentes() {
    const provas = [
        { nome: "Matemática - Prova Bimestral", data: "15 Jun 2026", respondidos: 19, total: 32 },
        { nome: "Matemática - Avaliação", data: "18 Jun 2026", respondidos: 16, total: 30 },
        { nome: "Matemática - Prova 1", data: "22 Jun 2026", respondidos: 30, total: 32 }
    ];
    const container = document.getElementById('provasRecentesList');
    if (!container) return;
    container.innerHTML = provas.map(p => `
        <div class="exam-item">
            <div class="exam-info">
                <div class="exam-name">${p.nome}</div>
                <div class="exam-date"><i class='bx bx-calendar'></i> ${p.data}</div>
            </div>
            <span class="exam-progress">${p.respondidos}/${p.total} responderam</span>
        </div>
    `).join('');
}

function fillTurmasList() {
    const turmas = [
        { nome: "6º Ano B", alunos: 32, media: 7.5 },
        { nome: "7º Ano C", alunos: 30, media: 9.0 },
        { nome: "8º Ano A", alunos: 32, media: 7.5 },
        { nome: "9º Ano A", alunos: 32, media: 7.8 }
    ];
    const container = document.getElementById('turmasList');
    if (!container) return;
    container.innerHTML = turmas.map(t => `
        <div class="result-item">
            <div class="result-info">
                <div class="result-name">${t.nome}</div>
                <div class="result-date">${t.alunos} alunos</div>
            </div>
            <div class="result-grade">${t.media}</div>
        </div>
    `).join('');
}

// Página de Provas (tabela)
const provasData = [
    { nome: "Matemática - Prova Bimestral", turma: "9º Ano A", data: "15 Jun 2026", questoes: 20, participacao: "28/32", status: "Ativa" },
    { nome: "Matemática - Avaliação Diagnóstica", turma: "8º Ano B", data: "12 Jun 2026", questoes: 15, participacao: "30/30", status: "Encerrada" },
    { nome: "Matemática - Prova Mensal", turma: "7º Ano A", data: "22 Jun 2026", questoes: 10, participacao: "0/28", status: "Rascunho" }
];

function renderProvasTable(filtro = 'todas') {
    let filtered = provasData;
    if (filtro === 'ativas') filtered = provasData.filter(p => p.status === 'Ativa');
    else if (filtro === 'encerradas') filtered = provasData.filter(p => p.status === 'Encerrada');
    else if (filtro === 'rascunhos') filtered = provasData.filter(p => p.status === 'Rascunho');
    const container = document.getElementById('provasTableBody');
    if (!container) return;
    container.innerHTML = filtered.map(p => {
        let statusClass = p.status === 'Ativa' ? 'ativa' : (p.status === 'Encerrada' ? 'encerrada' : 'rascunho');
        return `
            <tr>
                <td><div class="prova-info"><div class="prova-icon"><i class='bx bx-file'></i></div>${p.nome}</div></td>
                <td>${p.turma}</td><td>${p.data}</td><td>${p.questoes}</td><td>${p.participacao}</td>
                <td><span class="status ${statusClass}">${p.status}</span></td>
            </tr>
        `;
    }).join('');
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

// Página de Turmas
const turmasData = [
    { nome: "6º Ano B", materia: "Matemática", alunos: 32, media: 7.5, ultimaProva: "05 Jun 2026" },
    { nome: "7º Ano C", materia: "Matemática", alunos: 30, media: 9.0, ultimaProva: "03 Jun 2026" },
    { nome: "8º Ano A", materia: "Matemática", alunos: 30, media: 8.0, ultimaProva: "01 Jun 2026" },
    { nome: "9º Ano A", materia: "Matemática", alunos: 32, media: 7.8, ultimaProva: "28 Mai 2026" }
];

function fillTurmasCards() {
    const container = document.getElementById('turmasGrid');
    if (!container) return;
    container.innerHTML = turmasData.map(t => `
        <div class="turma-card">
            <div class="turma-card-top"><div><h2>${t.nome}</h2><p>${t.materia}</p></div><span class="turma-status">Ativa</span></div>
            <div class="turma-info-grid">
                <div class="turma-info-box"><strong>${t.alunos}</strong><span>Alunos</span></div>
                <div class="turma-info-box"><strong class="green-text">${t.media}</strong><span>Média</span></div>
                <div class="turma-info-box"><strong>${t.ultimaProva}</strong><span>Última Prova</span></div>
            </div>
            <button class="turma-btn">Ver Detalhes</button>
        </div>
    `).join('');
    document.querySelectorAll('.turma-btn').forEach(btn => {
        btn.addEventListener('click', () => exibirAlerta('Detalhes da turma em breve', 'info'));
    });
}

// Página de Resultados
const resultadosData = [
    { turma: "6º Ano B", prova: "Prova Bimestral", media: "7.5", participacao: "30/32", status: "Regular" },
    { turma: "7º Ano C", prova: "Avaliação Final", media: "9.0", participacao: "30/30", status: "Excelente" },
    { turma: "8º Ano A", prova: "Prova Mensal", media: "7.5", participacao: "28/32", status: "Regular" }
];

function fillResultadosTable() {
    const container = document.getElementById('resultadosTableBody');
    if (!container) return;
    container.innerHTML = resultadosData.map(r => {
        let statusClass = r.status === 'Excelente' ? 'ativa' : (r.status === 'Regular' ? 'encerrada' : 'rascunho');
        return `<tr><td>${r.turma}</td><td>${r.prova}</td><td>${r.media}</td><td>${r.participacao}</td><td><span class="status ${statusClass}">${r.status}</span></td></tr>`;
    }).join('');
}

// Navegação entre páginas
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
            if (pageId === 'provas') renderProvasTable();
            else if (pageId === 'turmas') fillTurmasCards();
            else if (pageId === 'resultados') fillResultadosTable();
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

// =============================================
// INICIALIZAÇÃO PRINCIPAL (com verificação de sessão)
// =============================================

document.addEventListener('DOMContentLoaded', () => {
    verificarSessao((usuario) => {
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
    });
});