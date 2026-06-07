/**
 * PROVA SERGIPE — global.js
 * Funções utilitárias reutilizáveis por todos os perfis.
 * NÃO contém lógica específica de nenhum perfil.
 */

'use strict';
const API_BASE = 'http://localhost:8081';

/* =============================================
   1. FETCH CENTRALIZADO
   ============================================= */

/**
 * Centraliza chamadas fetch com tratamento de erro padronizado.
 * @param {string}   url       - Endpoint da API.
 * @param {object}   options   - Opções do fetch (method, body, headers, etc.).
 * @param {function} onSuccess - Callback chamado com os dados parseados em caso de sucesso.
 * @param {function} onError   - Callback chamado com o objeto Error em caso de falha.
 */
async function fetchAPI(url, options = {}, onSuccess, onError) {
    const fullUrl = url.startsWith('http') ? url : `${API_BASE}${url}`;

    const defaultOptions = {
        credentials: 'include',
        headers: {
            'Content-Type': 'application/json',
            ...options.headers,
        },
        ...options,
    };

    try {
        const response = await fetch(fullUrl, defaultOptions);

        if (!response.ok) {
            let mensagemErro = `Erro ${response.status}: ${response.statusText}`;
            try {
                const errData = await response.json();
                if (errData.message) mensagemErro = errData.message;
            } catch (_) { /* ignora erro de parse */ }
            throw new Error(mensagemErro);
        }

        const data = await response.json();
        if (typeof onSuccess === 'function') onSuccess(data);
        return data;

    } catch (error) {
        console.error('[fetchAPI] Falha na requisição:', error);
        if (typeof onError === 'function') {
            onError(error);
        } else {
            exibirAlerta(error.message || 'Erro de comunicação com o servidor.', 'erro');
        }
        throw error;
    }
}

/* =============================================
   2. ALERTAS FLUTUANTES
   ============================================= */

/**
 * Exibe uma notificação flutuante no canto inferior direito.
 * @param {string} mensagem - Texto da notificação.
 * @param {'sucesso'|'erro'|'aviso'|'info'} tipo - Tipo da notificação.
 * @param {number} duracao  - Duração em ms antes de sumir (padrão: 3500).
 */
function exibirAlerta(mensagem, tipo = 'info', duracao = 3500) {
    const cores = {
        sucesso: { bg: '#00B34D', icon: 'bx-check-circle' },
        erro:    { bg: '#e74c3c', icon: 'bx-x-circle'     },
        aviso:   { bg: '#f59e0b', icon: 'bx-error'        },
        info:    { bg: '#3b82f6', icon: 'bx-info-circle'  },
    };

    const config = cores[tipo] || cores.info;

    let wrapper = document.getElementById('ps-alert-wrapper');
    if (!wrapper) {
        wrapper = document.createElement('div');
        wrapper.id = 'ps-alert-wrapper';
        Object.assign(wrapper.style, {
            position:      'fixed',
            bottom:        '24px',
            right:         '24px',
            display:       'flex',
            flexDirection: 'column',
            gap:           '10px',
            zIndex:        '9999',
        });
        document.body.appendChild(wrapper);
    }

    const alerta = document.createElement('div');
    alerta.innerHTML = `<i class='bx ${config.icon}'></i><span>${mensagem}</span>`;
    Object.assign(alerta.style, {
        display:      'flex',
        alignItems:   'center',
        gap:          '10px',
        background:   config.bg,
        color:        'white',
        padding:      '12px 18px',
        borderRadius: '12px',
        fontSize:     '13px',
        fontWeight:   '500',
        boxShadow:    '0 4px 16px rgba(0,0,0,0.15)',
        opacity:      '0',
        transform:    'translateX(60px)',
        transition:   'opacity 0.3s ease, transform 0.3s ease',
        maxWidth:     '320px',
        lineHeight:   '1.4',
    });

    wrapper.appendChild(alerta);

    requestAnimationFrame(() => {
        requestAnimationFrame(() => {
            alerta.style.opacity   = '1';
            alerta.style.transform = 'translateX(0)';
        });
    });

    setTimeout(() => {
        alerta.style.opacity   = '0';
        alerta.style.transform = 'translateX(60px)';
        alerta.addEventListener('transitionend', () => alerta.remove(), { once: true });
    }, duracao);
}

/* =============================================
   3. FORMATADORES DE DATA E HORA
   ============================================= */

/**
 * Converte data ISO (yyyy-mm-dd) para dd/mm/yyyy.
 */
function formatarData(dataISO) {
    if (!dataISO) return '—';
    const [ano, mes, dia] = dataISO.split('-');
    return `${dia}/${mes}/${ano}`;
}

/**
 * Converte hora ISO (HH:MM:SS ou HH:MM) para HH:MM.
 */
function formatarHora(horaISO) {
    if (!horaISO) return '—';
    return horaISO.substring(0, 5);
}

/**
 * Retorna a data local no formato yyyy-mm-dd (sem problemas de fuso horário).
 * Usada por admin.js e outros perfis para eventos do calendário.
 */
function getDataLocal(date) {
    const year  = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day   = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

/* =============================================
   4. CALENDÁRIO GENÉRICO
   ============================================= */

const DIAS_SEMANA = ['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb'];
const MESES = [
    'Janeiro', 'Fevereiro', 'Março', 'Abril', 'Maio', 'Junho',
    'Julho', 'Agosto', 'Setembro', 'Outubro', 'Novembro', 'Dezembro',
];

/**
 * Renderiza um calendário mensal interativo dentro de um container.
 *
 * @param {string}   containerId  - ID do elemento onde o calendário será inserido.
 * @param {Date}     currentDate  - Mês/ano a exibir.
 * @param {Array}    eventsData   - Array de objetos { date, type, title }.
 * @param {function} onDayClick   - Callback opcional chamado com (dateString, eventsNoDia[]).
 */
function renderCalendar(containerId, currentDate, eventsData = [], onDayClick) {
    const container = document.getElementById(containerId);
    if (!container) {
        console.warn(`[renderCalendar] Container #${containerId} não encontrado.`);
        return;
    }

    const ano       = currentDate.getFullYear();
    const mes       = currentDate.getMonth();
    const hoje      = new Date();
    const ehHoje    = (d) =>
        d === hoje.getDate() && mes === hoje.getMonth() && ano === hoje.getFullYear();

    const primeiroDia = new Date(ano, mes, 1).getDay();
    const totalDias   = new Date(ano, mes + 1, 0).getDate();

    const eventosPorData = {};
    eventsData.forEach(ev => {
        if (!eventosPorData[ev.date]) eventosPorData[ev.date] = [];
        eventosPorData[ev.date].push(ev);
    });

    const diasHTML = [];

    for (let i = 0; i < primeiroDia; i++) {
        diasHTML.push('<span></span>');
    }

    for (let d = 1; d <= totalDias; d++) {
        const mesStr   = String(mes + 1).padStart(2, '0');
        const diaStr   = String(d).padStart(2, '0');
        const dateKey  = `${ano}-${mesStr}-${diaStr}`;
        const eventos  = eventosPorData[dateKey] || [];
        const tiposUnicos = [...new Set(eventos.map(e => e.type))];
        const classeEvento = eventos.length ? `has-event ${tiposUnicos.join(' ')}` : '';
        const classeHoje   = ehHoje(d) ? 'today' : '';

        diasHTML.push(
            `<span class="calendar-day ${classeHoje} ${classeEvento}"
                   data-date="${dateKey}"
                   title="${eventos.map(e => e.title).join(', ')}">${d}</span>`
        );
    }

    container.innerHTML = `
        <div class="cal-header" style="display:flex;align-items:center;justify-content:space-between;margin-bottom:6px;">
            <button class="cal-nav" id="${containerId}-prev" aria-label="Mês anterior">
                <i class='bx bx-chevron-left'></i>
            </button>
            <span style="font-weight:700;font-size:14px;color:#0f172a;">
                ${MESES[mes]} ${ano}
            </span>
            <button class="cal-nav" id="${containerId}-next" aria-label="Próximo mês">
                <i class='bx bx-chevron-right'></i>
            </button>
        </div>

        <div class="calendar-weekdays">
            ${DIAS_SEMANA.map(d => `<span>${d}</span>`).join('')}
        </div>

        <div class="calendar-days">
            ${diasHTML.join('')}
        </div>

        <div class="calendar-legend">
            <span class="legend"><span class="dot exam"></span>Prova</span>
            <span class="legend"><span class="dot deadline"></span>Prazo</span>
            <span class="legend"><span class="dot meeting"></span>Reunião</span>
        </div>
    `;

    if (typeof onDayClick === 'function') {
        container.querySelectorAll('.calendar-day[data-date]').forEach(el => {
            el.addEventListener('click', () => {
                const dateKey = el.dataset.date;
                const eventos = eventosPorData[dateKey] || [];
                onDayClick(dateKey, eventos);
            });
        });
    }

    document.getElementById(`${containerId}-prev`)?.addEventListener('click', () => {
        const nova = new Date(ano, mes - 1, 1);
        renderCalendar(containerId, nova, eventsData, onDayClick);
        container.dispatchEvent(new CustomEvent('calendarChange', { detail: { date: nova } }));
    });

    document.getElementById(`${containerId}-next`)?.addEventListener('click', () => {
        const nova = new Date(ano, mes + 1, 1);
        renderCalendar(containerId, nova, eventsData, onDayClick);
        container.dispatchEvent(new CustomEvent('calendarChange', { detail: { date: nova } }));
    });
}

/* =============================================
   5. LOGOUT
   ============================================= */

async function logout() {
    try {
        await fetchAPI('/api/logout', { method: 'POST' });
    } catch (_) {
    } finally {
        window.location.href = `${API_BASE}/login.html`;
    }
}

/* =============================================
   6. MENU MOBILE (sidebar toggle)
   ============================================= */

function inicializarMenuMobile() {
    const menuBtn = document.querySelector('.menu-btn');
    const sidebar = document.querySelector('.sidebar');
    if (!menuBtn || !sidebar) return;

    menuBtn.addEventListener('click', () => sidebar.classList.toggle('open'));

    document.addEventListener('click', (e) => {
        if (!sidebar.contains(e.target) && !menuBtn.contains(e.target)) {
            sidebar.classList.remove('open');
        }
    });
}

/* =============================================
   7. VERIFICAÇÃO DE SESSÃO
   ============================================= */

/**
 * Verifica se há sessão ativa no backend.
 * Se não houver, redireciona para login.html.
 *
 * @param {function} [onSucesso] - Callback com os dados do usuário logado.
 */
async function verificarSessao(onSucesso) {
    try {
        const response = await fetch(`${API_BASE}/api/usuario/logado`, {
            credentials: 'include'
        });

        if (!response.ok) {
            window.location.href = '/login.html';
            return;
        }

        const usuario = await response.json();

        if (typeof onSucesso === 'function') {
            onSucesso(usuario);
        }

    } catch (_) {
        window.location.href = '/login.html';
    }
}

/**
 * Preenche os dados do usuário no header da página.
 */
function preencherHeaderUsuario(usuario) {
    const avatarEl = document.querySelector('.user-avatar');
    const nomeEl   = document.querySelector('.user-name');
    const roleEl   = document.querySelector('.user-role');

    if (avatarEl) avatarEl.textContent = (usuario.nome || 'U').charAt(0).toUpperCase();
    if (nomeEl)   nomeEl.textContent   = usuario.nome || 'Usuário';
    if (roleEl)   roleEl.textContent   = capitalizarPerfil(usuario.perfil);
}

/**
 * Capitaliza o nome do perfil para exibição.
 */
function capitalizarPerfil(perfil) {
    const mapa = {
        ADMIN:       'Administrador',
        PROFESSOR:   'Professor',
        ALUNO:       'Aluno',
        COORDENADOR: 'Coordenador'
    };
    return mapa[perfil?.toUpperCase()] || perfil || 'Usuário';
}