/**
 * PROVA SERGIPE — admin.js
 * Lógica do painel administrativo.
 * Depende de: global.js (carregado antes via defer).
 */

'use strict';

/* =============================================
   CALENDÁRIO
============================================= */

let adminCalendarDate = new Date();

const adminCalendarEvents = [
    { date: getDataLocal(new Date()), type: "exam",     title: "Hoje"                     },
    { date: "2026-06-10",            type: "meeting",  title: "Reunião Pedagógica"        },
    { date: "2026-06-15",            type: "deadline", title: "Prazo lançamento de notas" },
    { date: "2026-06-20",            type: "exam",     title: "Simulado Estadual"         }
];

function inicializarCalendario() {
    renderCalendar('admin-calendar', adminCalendarDate, adminCalendarEvents, (date, events) => {
        if (!events.length) {
            exibirAlerta(`Nenhum evento em ${formatarData(date)}`, 'info');
            return;
        }
        exibirAlerta(`${formatarData(date)} • ${events.map(e => e.title).join(', ')}`, 'sucesso');
    });
    document.getElementById('admin-calendar')
        ?.addEventListener('calendarChange', (e) => { adminCalendarDate = e.detail.date; });
}

/* =============================================
   DASHBOARD — carrega dados da API
============================================= */

async function carregarDashboard() {
    atualizarBoasVindas();
    try {
        const data = await fetchAPI('/api/admin/dashboard');
        document.getElementById('stat-alunos').textContent      = data.totalAlunos      ?? '—';
        document.getElementById('stat-provas').textContent      = data.totalProvas      ?? '—';
        document.getElementById('stat-professores').textContent = data.totalProfessores ?? '—';
        document.getElementById('stat-media').textContent       = data.mediaGeral != null
            ? data.mediaGeral.toFixed(1) : '—';
    } catch (_) { /* mantém os traços — */ }

    carregarProvasRecentes();
    carregarDesempenho();
}

async function carregarProvasRecentes() {
    const container = document.getElementById('provas-recentes-list');
    if (!container) return;
    try {
        const provas = await fetchAPI('/api/admin/provas/recentes');
        if (!provas.length) {
            container.innerHTML = '<p style="color:#94a3b8;font-size:12px;padding:8px;">Nenhuma prova encontrada.</p>';
            return;
        }
        container.innerHTML = provas.slice(0, 4).map(p => {
            const corStatus = { Ativa: '#dcfce7', Encerrada: '#fee2e2', Agendada: '#fef9c3', Inativa: '#e2e8f0' };
            const txtStatus = { Ativa: '#166534', Encerrada: '#991b1b', Agendada: '#854d0e', Inativa: '#475569' };
            const bg  = corStatus[p.status] || '#e2e8f0';
            const txt = txtStatus[p.status] || '#334155';
            return `
                <div class="exam-item">
                    <div class="exam-info">
                        <div class="exam-name">${p.titulo}</div>
                        <div class="exam-date"><i class='bx bx-building'></i> ${p.escola} • ${p.turma}</div>
                    </div>
                    <span style="padding:4px 10px;border-radius:20px;font-size:11px;font-weight:600;background:${bg};color:${txt};">
                        ${p.status}
                    </span>
                </div>`;
        }).join('');
    } catch (_) {
        container.innerHTML = '<p style="color:#e74c3c;font-size:12px;padding:8px;">Erro ao carregar provas.</p>';
    }
}

async function carregarDesempenho() {
    const container = document.getElementById('desempenho-list');
    if (!container) return;
    try {
        const escolas = await fetchAPI('/api/admin/relatorio/desempenho');
        if (!escolas.length) {
            container.innerHTML = '<p style="color:#94a3b8;font-size:12px;padding:8px;">Nenhuma escola encontrada.</p>';
            return;
        }
        container.innerHTML = escolas.slice(0, 4).map(e => {
            const nota = Number(e.mediaGeral);
            const classe = nota >= 7 ? 'good' : nota >= 5 ? 'average' : nota > 0 ? 'bad' : '';
            return `
                <div class="result-item">
                    <div class="result-info">
                        <div class="result-name">${e.escola}</div>
                        <div class="result-date">${e.cidade} • ${e.totalAlunos} alunos</div>
                    </div>
                    <span class="result-grade ${classe}">${nota > 0 ? nota.toFixed(1) : '—'}</span>
                </div>`;
        }).join('');
    } catch (_) {
        container.innerHTML = '<p style="color:#e74c3c;font-size:12px;padding:8px;">Erro ao carregar desempenho.</p>';
    }
}

/* =============================================
   PÁGINA ALUNOS
============================================= */

let alunosTodos = [];
let alunosPagina = 1;
const ALUNOS_POR_PAGINA = 10;

async function carregarAlunos() {
    const container = document.getElementById('alunos-table-body');
    if (!container) return;
    try {
        const usuarios = await fetchAPI('/api/usuarios');
        alunosTodos = usuarios.filter(u => u.perfil === 'ALUNO');
        alunosPagina = 1;
        renderizarAlunos();
    } catch (_) {
        container.innerHTML = '<p style="padding:24px;color:#e74c3c;font-size:13px;">Erro ao carregar alunos.</p>';
    }
}

function renderizarAlunos() {
    const container = document.getElementById('alunos-table-body');
    const info      = document.getElementById('alunos-pagination-info');
    if (!container) return;

    const total  = alunosTodos.length;
    const inicio = (alunosPagina - 1) * ALUNOS_POR_PAGINA;
    const fim    = Math.min(inicio + ALUNOS_POR_PAGINA, total);
    const pagina = alunosTodos.slice(inicio, fim);

    if (!pagina.length) {
        container.innerHTML = '<p style="padding:24px;color:#94a3b8;font-size:13px;">Nenhum aluno encontrado.</p>';
        if (info) info.textContent = '0 alunos';
        return;
    }

    container.innerHTML = pagina.map(u => {
        const inicial = (u.nome || 'A').charAt(0).toUpperCase();
        const status  = u.ativo
            ? '<span class="status-active">Ativo</span>'
            : '<span class="status-inactive">Inativo</span>';
        return `
            <div class="student-row">
                <div class="student-info">
                    <div class="student-avatar">${inicial}</div>
                    <div>
                        <div style="font-weight:600;font-size:13px;">${u.nome || '—'}</div>
                        <div class="student-email">${u.email || '—'}</div>
                    </div>
                </div>
                <span class="class-badge">${u.turma || '—'}</span>
                <span style="font-size:13px;color:#334155;">—</span>
                <span style="font-size:12px;color:#64748b;">${u.email || '—'}</span>
                ${status}
                <span class="action-menu"><i class='bx bx-dots-vertical-rounded'></i></span>
            </div>`;
    }).join('');

    if (info) info.textContent = `Mostrando ${inicio + 1}–${fim} de ${total} alunos`;
    if (document.getElementById('alunos-page-num'))
        document.getElementById('alunos-page-num').textContent = alunosPagina;
}

function setupPaginacaoAlunos() {
    document.getElementById('alunos-prev')?.addEventListener('click', () => {
        if (alunosPagina > 1) { alunosPagina--; renderizarAlunos(); }
    });
    document.getElementById('alunos-next')?.addEventListener('click', () => {
        const maxPagina = Math.ceil(alunosTodos.length / ALUNOS_POR_PAGINA);
        if (alunosPagina < maxPagina) { alunosPagina++; renderizarAlunos(); }
    });
}

/* =============================================
   PÁGINA PROFESSORES
============================================= */

async function carregarProfessores() {
    const container = document.getElementById('professores-table-body');
    if (!container) return;
    try {
        const professores = await fetchAPI('/api/coordenacao/professores');
        if (!professores.length) {
            container.innerHTML = '<p style="padding:24px;color:#94a3b8;font-size:13px;">Nenhum professor encontrado.</p>';
            return;
        }
        container.innerHTML = professores.map(p => {
            const status = p.status === 'Ativo'
                ? '<span class="status-active">Ativo</span>'
                : '<span class="status-inactive">Inativo</span>';
            return `
                <div style="display:grid;grid-template-columns:2fr 1fr 1fr 1fr;padding:16px 24px;border-bottom:1px solid #eef2f8;align-items:center;transition:0.2s;" onmouseover="this.style.background='#f8fbff'" onmouseout="this.style.background=''">
                    <div style="display:flex;align-items:center;gap:12px;">
                        <div style="width:38px;height:38px;border-radius:50%;background:linear-gradient(135deg,#2D4496,#3b82f6);display:flex;align-items:center;justify-content:center;color:white;font-weight:700;font-size:13px;">
                            ${(p.nome || 'P').charAt(0).toUpperCase()}
                        </div>
                        <div>
                            <div style="font-weight:600;font-size:13px;">${p.nome || '—'}</div>
                        </div>
                    </div>
                    <span style="font-size:13px;color:#334155;">${p.disciplina || '—'}</span>
                    <span style="font-size:13px;color:#64748b;">${p.escola || '—'}</span>
                    ${status}
                </div>`;
        }).join('');
    } catch (_) {
        container.innerHTML = '<p style="padding:24px;color:#e74c3c;font-size:13px;">Erro ao carregar professores.</p>';
    }
}

/* =============================================
   PÁGINA PROVAS
============================================= */

async function carregarProvas() {
    const tbody = document.getElementById('provas-table-body');
    if (!tbody) return;
    try {
        const provas = await fetchAPI('/api/admin/provas/recentes');
        if (!provas.length) {
            tbody.innerHTML = '<tr><td colspan="5" style="padding:24px;color:#94a3b8;font-size:13px;">Nenhuma prova encontrada.</td></tr>';
            return;
        }
        const corStatus = { Ativa: '#dcfce7', Encerrada: '#fee2e2', Agendada: '#fef9c3', Inativa: '#e2e8f0' };
        const txtStatus = { Ativa: '#166534', Encerrada: '#991b1b', Agendada: '#854d0e', Inativa: '#475569' };
        tbody.innerHTML = provas.map(p => {
            const bg  = corStatus[p.status] || '#e2e8f0';
            const txt = txtStatus[p.status] || '#334155';
            const data = p.dataInicio ? new Date(p.dataInicio).toLocaleDateString('pt-BR') : '—';
            return `
                <tr style="border-bottom:1px solid #eef2f8;">
                    <td style="padding:14px 16px;font-weight:600;font-size:13px;">${p.titulo}</td>
                    <td style="padding:14px 16px;font-size:13px;color:#64748b;">${p.escola}</td>
                    <td style="padding:14px 16px;font-size:13px;color:#64748b;">${p.turma}</td>
                    <td style="padding:14px 16px;font-size:13px;color:#64748b;">${data}</td>
                    <td style="padding:14px 16px;">
                        <span style="padding:4px 10px;border-radius:20px;font-size:11px;font-weight:600;background:${bg};color:${txt};">${p.status}</span>
                    </td>
                </tr>`;
        }).join('');
    } catch (_) {
        tbody.innerHTML = '<tr><td colspan="5" style="padding:24px;color:#e74c3c;font-size:13px;">Erro ao carregar provas.</td></tr>';
    }
}

/* =============================================
   PÁGINA RELATÓRIOS
============================================= */

async function carregarRelatorios() {
    const tbody = document.getElementById('relatorios-table-body');
    if (!tbody) return;
    try {
        const escolas = await fetchAPI('/api/admin/relatorio/desempenho');
        if (!escolas.length) {
            tbody.innerHTML = '<tr><td colspan="6" style="padding:24px;color:#94a3b8;font-size:13px;">Nenhuma escola encontrada.</td></tr>';
            return;
        }
        const corDesemp = { Excelente: '#dcfce7', Regular: '#fef9c3', 'Abaixo da média': '#fee2e2', 'Sem dados': '#e2e8f0' };
        const txtDesemp = { Excelente: '#166534', Regular: '#854d0e', 'Abaixo da média': '#991b1b', 'Sem dados': '#475569' };
        tbody.innerHTML = escolas.map(e => {
            const nota = Number(e.mediaGeral);
            const bg  = corDesemp[e.desempenho] || '#e2e8f0';
            const txt = txtDesemp[e.desempenho] || '#334155';
            return `
                <tr style="border-bottom:1px solid #eef2f8;">
                    <td style="padding:14px 16px;font-weight:600;font-size:13px;">${e.escola}</td>
                    <td style="padding:14px 16px;font-size:13px;color:#64748b;">${e.cidade || '—'}</td>
                    <td style="padding:14px 16px;font-size:13px;color:#334155;">${e.totalAlunos}</td>
                    <td style="padding:14px 16px;font-size:13px;color:#334155;">${e.provasAplicadas}</td>
                    <td style="padding:14px 16px;font-weight:700;font-size:13px;">${nota > 0 ? nota.toFixed(1) : '—'}</td>
                    <td style="padding:14px 16px;">
                        <span style="padding:4px 10px;border-radius:20px;font-size:11px;font-weight:600;background:${bg};color:${txt};">${e.desempenho}</span>
                    </td>
                </tr>`;
        }).join('');
    } catch (_) {
        tbody.innerHTML = '<tr><td colspan="6" style="padding:24px;color:#e74c3c;font-size:13px;">Erro ao carregar relatório.</td></tr>';
    }
}

/* =============================================
   NAVEGAÇÃO ENTRE PÁGINAS
============================================= */

function setupPages() {
    const navLinks = document.querySelectorAll('.nav-item[data-page]');
    const pages    = ['dashboard', 'alunos', 'professores', 'provas', 'relatorios', 'configuracoes', 'ajuda'];

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
            if (pageId === 'alunos')      carregarAlunos();
            if (pageId === 'professores') carregarProfessores();
            if (pageId === 'provas')      carregarProvas();
            if (pageId === 'relatorios')  carregarRelatorios();
        });
    });

    // Links "Ver todas" dentro dos cards do dashboard
    document.querySelectorAll('.view-all-link').forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const target = link.getAttribute('data-page');
            if (target) document.querySelector(`.nav-item[data-page="${target}"]`)?.click();
        });
    });
}

/* =============================================
   AÇÕES RÁPIDAS
============================================= */

function setupAcoesRapidas() {
    document.querySelectorAll('.quick-action-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            const acoes = {
                'novo-aluno':    () => document.querySelector('.nav-item[data-page="alunos"]')?.click(),
                'nova-prova':    () => document.querySelector('.nav-item[data-page="provas"]')?.click(),
                'relatorio':     () => document.querySelector('.nav-item[data-page="relatorios"]')?.click(),
                'configuracoes': () => document.querySelector('.nav-item[data-page="configuracoes"]')?.click(),
            };
            const fn = acoes[btn.dataset.action];
            if (fn) fn();
        });
    });
}

/* =============================================
   LOGOUT
============================================= */

function configurarLogout() {
    const logoutBtn = document.getElementById('logout');
    if (!logoutBtn) return;
    logoutBtn.addEventListener('click', (e) => {
        e.preventDefault();
        if (confirm('Deseja sair da sua conta?')) logout();
    });
}

/* =============================================
   BOAS-VINDAS
============================================= */

function atualizarBoasVindas() {
    const titulo = document.querySelector('.welcome h1');
    if (!titulo) return;
    const hora = new Date().getHours();
    let saudacao = 'Bem-vindo';
    if (hora >= 5  && hora < 12) saudacao = 'Bom dia';
    else if (hora >= 12 && hora < 18) saudacao = 'Boa tarde';
    else saudacao = 'Boa noite';
    titulo.textContent = `${saudacao}, Administrador`;
}

/* =============================================
   INICIALIZAÇÃO PRINCIPAL
============================================= */

document.addEventListener('DOMContentLoaded', () => {
    verificarSessao((usuario) => {
        preencherHeaderUsuario(usuario);
        inicializarMenuMobile();
        inicializarCalendario();
        carregarDashboard();
        setupPages();
        setupAcoesRapidas();
        configurarLogout();
        setupPaginacaoAlunos();
    });
});