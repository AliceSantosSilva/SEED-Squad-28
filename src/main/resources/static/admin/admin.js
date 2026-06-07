/**
 * PROVA SERGIPE — admin.js
 * Lógica específica do perfil Administrador.
 * Depende de: global.js (carregado antes via defer).
 * getDataLocal() vive em global.js — não redefinir aqui.
 */

'use strict';

/* =============================================
   ESTADO GLOBAL
============================================= */

let adminCalendarDate = new Date();

/* =============================================
   EVENTOS DO CALENDÁRIO
   getDataLocal() vem do global.js
============================================= */

const adminCalendarEvents = [
    { date: getDataLocal(new Date()), type: "exam",     title: "Hoje"                        },
    { date: "2026-06-05",            type: "exam",     title: "Prova Matemática - 9º Ano"   },
    { date: "2026-06-10",            type: "meeting",  title: "Reunião Pedagógica"           },
    { date: "2026-06-15",            type: "deadline", title: "Prazo lançamento de notas"    },
    { date: "2026-06-20",            type: "exam",     title: "Simulado Estadual"            }
];

/* =============================================
   DASHBOARD
============================================= */

function inicializarDashboard() {
    atualizarBoasVindas();
    configurarBotoes();
    carregarEstatisticas();
}

async function carregarEstatisticas() {
    try {
        const data = await fetchAPI('/api/admin/dashboard');

        const primaryValue = document.querySelector('.stat-card.primary .stat-value');
        if (primaryValue && data.totalAlunos !== undefined) {
            primaryValue.textContent = data.totalAlunos;
        }

        const successValue = document.querySelector('.stat-card.success .stat-value');
        if (successValue && data.totalProvas !== undefined) {
            successValue.textContent = data.totalProvas;
        }

        const warningValue = document.querySelector('.stat-card.warning .stat-value');
        if (warningValue && data.totalProfessores !== undefined) {
            warningValue.textContent = data.totalProfessores;
        }

        const infoValue = document.querySelector('.stat-card.info .stat-value2');
        if (infoValue && data.mediaGeral !== undefined) {
            infoValue.textContent = data.mediaGeral.toFixed(1);
        }

    } catch (_) {
        // Falha silenciosa — HTML já tem valores placeholder
    }
}

/* =============================================
   CALENDÁRIO
============================================= */

function inicializarCalendario() {
    renderCalendar(
        'admin-calendar',
        adminCalendarDate,
        adminCalendarEvents,
        aoClicarDiaCalendario
    );

    const calendarContainer = document.getElementById('admin-calendar');
    if (!calendarContainer) return;

    calendarContainer.addEventListener('calendarChange', (event) => {
        adminCalendarDate = event.detail.date;
    });
}

function aoClicarDiaCalendario(date, events) {
    if (!events.length) {
        exibirAlerta(`Nenhum evento em ${formatarData(date)}`, 'info');
        return;
    }
    const titulos = events.map(e => e.title).join(', ');
    exibirAlerta(`${formatarData(date)} • ${titulos}`, 'sucesso');
}

/* =============================================
   BOTÕES
============================================= */

function configurarBotoes() {
    configurarAcoesRapidas();
    configurarCardsProvas();
    configurarViewAllLinks();
}

function configurarAcoesRapidas() {
    document.querySelectorAll('.quick-action-btn').forEach(botao => {
        botao.addEventListener('click', () => {
            const mensagens = {
                'novo-aluno':    'Abrindo cadastro de aluno...',
                'nova-prova':    'Abrindo criação de prova...',
                'relatorio':     'Gerando relatório...',
                'configuracoes': 'Abrindo configurações...',
            };
            exibirAlerta(mensagens[botao.dataset.action] || 'Ação não configurada.', 'info');
        });
    });
}

function configurarCardsProvas() {
    document.querySelectorAll('.btn-start').forEach(botao => {
        botao.addEventListener('click', () => {
            const prova = botao.closest('.exam-item')
                ?.querySelector('.exam-name')?.textContent;
            exibirAlerta(`Abrindo ${prova || 'prova'}`, 'info');
        });
    });
}

function configurarViewAllLinks() {
    document.querySelectorAll('.view-all-link').forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const targetPage = link.getAttribute('data-page');
            if (targetPage) {
                exibirAlerta(`Navegando para ${targetPage}... (em desenvolvimento)`, 'info');
            }
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
   BOAS-VINDAS DINÂMICA
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
        inicializarDashboard();
        inicializarCalendario();
        configurarLogout();
    });
});