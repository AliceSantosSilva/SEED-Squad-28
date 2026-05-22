/**
 * =============================================
 * PROVA SERGIPE — adm.js
 * Lógica exclusiva do painel administrativo
 * =============================================
 */

'use strict';

/* =============================================
   ESTADO GLOBAL DA PÁGINA
============================================= */

let adminCalendarDate = new Date();

/* =============================================
   EVENTOS DO CALENDÁRIO
============================================= */

const adminCalendarEvents = [

    {
        date: '2026-05-20',
        type: 'exam',
        title: 'Prova Matemática'
    },

    {
        date: '2026-05-22',
        type: 'meeting',
        title: 'Reunião Pedagógica'
    },

    {
        date: '2026-05-25',
        type: 'deadline',
        title: 'Prazo lançamento de notas'
    },

    {
        date: '2026-05-28',
        type: 'exam',
        title: 'Simulado Estadual'
    }

];

/* =============================================
   INICIALIZAÇÃO
============================================= */

document.addEventListener('DOMContentLoaded', () => {

    // Usa a função do global.js (já existe)
    if (typeof inicializarMenuMobile === 'function') {
        inicializarMenuMobile();
    }

    inicializarCalendario();

    inicializarDashboard();

    configurarLogout();

});

/* =============================================
   DASHBOARD
============================================= */

function inicializarDashboard() {

    atualizarBoasVindas();

    configurarBotoes();

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

/**
 * Callback executado ao clicar em um dia do calendário
 */
function aoClicarDiaCalendario(date, events) {

    if (!events.length) {

        exibirAlerta(
            `Nenhum evento em ${formatarData(date)}`,
            'info'
        );

        return;
    }

    const titulos = events.map(event => event.title).join(', ');

    exibirAlerta(
        `${formatarData(date)} • ${titulos}`,
        'sucesso'
    );

}

/* =============================================
   BOTÕES
============================================= */

function configurarBotoes() {

    configurarAcoesRapidas();

    configurarCardsProvas();

    configurarViewAllLinks();

}

/**
 * Ações rápidas do dashboard
 */
function configurarAcoesRapidas() {

    const botoes = document.querySelectorAll('.quick-action-btn');

    botoes.forEach(botao => {

        botao.addEventListener('click', () => {

            const acao = botao.dataset.action;

            switch (acao) {

                case 'novo-aluno':

                    exibirAlerta(
                        'Abrindo cadastro de aluno...',
                        'info'
                    );

                    break;

                case 'nova-prova':

                    exibirAlerta(
                        'Abrindo criação de prova...',
                        'info'
                    );

                    break;

                case 'relatorio':

                    exibirAlerta(
                        'Gerando relatório...',
                        'sucesso'
                    );

                    break;

                case 'configuracoes':

                    exibirAlerta(
                        'Abrindo configurações...',
                        'info'
                    );

                    break;

                default:

                    exibirAlerta(
                        'Ação não configurada.',
                        'aviso'
                    );

            }

        });

    });

}

/**
 * Botões dos cards de prova
 */
function configurarCardsProvas() {

    const botoesAbrir = document.querySelectorAll('.btn-start');

    botoesAbrir.forEach(botao => {

        botao.addEventListener('click', () => {

            const prova = botao
                .closest('.exam-item')
                ?.querySelector('.exam-name')
                ?.textContent;

            exibirAlerta(
                `Abrindo ${prova || 'prova'}`,
                'info'
            );

        });

    });

}

/**
 * Botões "Ver todas" de navegação
 */
function configurarViewAllLinks() {

    const viewAllLinks = document.querySelectorAll('.view-all-link');

    viewAllLinks.forEach(link => {

        link.addEventListener('click', (e) => {

            e.preventDefault();

            const targetPage = link.getAttribute('data-page');

            if (targetPage) {

                exibirAlerta(
                    `Navegando para ${targetPage}... (em desenvolvimento)`,
                    'info'
                );

            }

        });

    });

}

/* =============================================
   LOGOUT (usa função do global.js)
============================================= */

function configurarLogout() {

    const logoutBtn = document.getElementById('logout');

    if (logoutBtn) {

        logoutBtn.addEventListener('click', (e) => {

            e.preventDefault();

            if (confirm('Deseja sair da sua conta?')) {

                if (typeof logout === 'function') {

                    logout(); // função do global.js

                } else {

                    alert('Função de logout não disponível');

                }

            }

        });

    }

}

/* =============================================
   BOAS-VINDAS DINÂMICA
============================================= */

function atualizarBoasVindas() {

    const titulo = document.querySelector('.welcome h1');

    if (!titulo) return;

    const horaAtual = new Date().getHours();

    let saudacao = 'Bem-vindo';

    if (horaAtual >= 5 && horaAtual < 12) {

        saudacao = 'Bom dia';

    } else if (horaAtual >= 12 && horaAtual < 18) {

        saudacao = 'Boa tarde';

    } else {

        saudacao = 'Boa noite';

    }

    titulo.textContent = `${saudacao}, Administrador`;

}

/* =============================================
   API EXEMPLO
============================================= */

/**
 * Exemplo de carregamento futuro do dashboard
 */
async function carregarDadosDashboard() {

    try {

        const dados = await fetchAPI('/api/admin/dashboard');

        console.log('Dados dashboard:', dados);

    } catch (error) {

        console.error(error);

    }

}

/* =============================================
   HELPERS
============================================= */

/**
 * Simula carregamento
 */
function delay(ms) {

    return new Promise(resolve => {

        setTimeout(resolve, ms);

    });

}