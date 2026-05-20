/**
 * PROVA SERGIPE — global.js
 * Funções utilitárias reutilizáveis por todos os perfis.
 * NÃO contém lógica específica de nenhum perfil.
 */
 
'use strict';
 
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
    const defaultOptions = {
        credentials: 'include', // Envia cookies de sessão automaticamente
        headers: {
            'Content-Type': 'application/json',
            ...options.headers,
        },
        ...options,
    };
 
    try {
        const response = await fetch(url, defaultOptions);
 
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
 
    // Cria container de alertas se não existir
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
 
    // Anima entrada
    requestAnimationFrame(() => {
        requestAnimationFrame(() => {
            alerta.style.opacity   = '1';
            alerta.style.transform = 'translateX(0)';
        });
    });
 
    // Remove após duração
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
 * @param {string} dataISO
 * @returns {string}
 */
function formatarData(dataISO) {
    if (!dataISO) return '—';
    const [ano, mes, dia] = dataISO.split('-');
    return `${dia}/${mes}/${ano}`;
}
 
/**
 * Converte hora ISO (HH:MM:SS ou HH:MM) para HH:MM.
 * @param {string} horaISO
 * @returns {string}
 */
function formatarHora(horaISO) {
    if (!horaISO) return '—';
    return horaISO.substring(0, 5);
}
 
/* =============================================
   4. CALENDÁRIO GENÉRICO
   ============================================= */
 
const DIAS_SEMANA  = ['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb'];
const MESES        = [
    'Janeiro', 'Fevereiro', 'Março', 'Abril', 'Maio', 'Junho',
    'Julho', 'Agosto', 'Setembro', 'Outubro', 'Novembro', 'Dezembro',
];
 
/**
 * Renderiza um calendário mensal interativo dentro de um container.
 *
 * @param {string}   containerId  - ID do elemento onde o calendário será inserido.
 * @param {Date}     currentDate  - Mês/ano a exibir.
 * @param {Array}    eventsData   - Array de objetos { date: "yyyy-mm-dd", type: string, title: string }.
 * @param {function} onDayClick   - Callback opcional chamado com (dateString, eventsNoDia[]).
 *
 * Tipos de evento suportados: "exam" | "deadline" | "meeting"
 * (correspondem às classes CSS .dot.exam, .dot.deadline, .dot.meeting)
 */
function renderCalendar(containerId, currentDate, eventsData = [], onDayClick) {
    const container = document.getElementById(containerId);
    if (!container) {
        console.warn(`[renderCalendar] Container #${containerId} não encontrado.`);
        return;
    }
 
    const ano        = currentDate.getFullYear();
    const mes        = currentDate.getMonth();      // 0-indexed
    const hoje       = new Date();
    const ehHoje     = (d) => d === hoje.getDate() && mes === hoje.getMonth() && ano === hoje.getFullYear();
 
    const primeiroDia      = new Date(ano, mes, 1).getDay();   // dia da semana do 1º dia
    const totalDias        = new Date(ano, mes + 1, 0).getDate();
 
    // Indexa eventos por data para acesso O(1)
    const eventosPorData = {};
    eventsData.forEach(ev => {
        if (!eventosPorData[ev.date]) eventosPorData[ev.date] = [];
        eventosPorData[ev.date].push(ev);
    });
 
    // Constrói HTML
    const diasHTML = [];
 
    // Células vazias antes do 1º dia
    for (let i = 0; i < primeiroDia; i++) {
        diasHTML.push('<span></span>');
    }
 
    // Dias do mês
    for (let d = 1; d <= totalDias; d++) {
        const mesStr  = String(mes + 1).padStart(2, '0');
        const diaStr  = String(d).padStart(2, '0');
        const dateKey = `${ano}-${mesStr}-${diaStr}`;
        const eventos = eventosPorData[dateKey] || [];
 
        // Classes de evento (pode ter vários tipos no mesmo dia)
        const tiposUnicos = [...new Set(eventos.map(e => e.type))];
        const classeEvento = eventos.length ? `has-event ${tiposUnicos.join(' ')}` : '';
 
        const classeHoje = ehHoje(d) ? 'today' : '';
 
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
 
    // Clique nos dias
    if (typeof onDayClick === 'function') {
        container.querySelectorAll('.calendar-day[data-date]').forEach(el => {
            el.addEventListener('click', () => {
                const dateKey  = el.dataset.date;
                const eventos  = eventosPorData[dateKey] || [];
                onDayClick(dateKey, eventos);
            });
        });
    }
 
    // Navegação mês anterior / próximo
    // Retorna o novo Date para que o chamador possa atualizar seu estado
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
 
/**
 * Encerra a sessão chamando /api/logout e redireciona para login.html.
 */
async function logout() {
    try {
        await fetchAPI('/api/logout', { method: 'POST' });
    } catch (_) {
        // Mesmo em caso de erro, redireciona
    } finally {
        window.location.href = '/login.html';
    }
}
 
/* =============================================
   6. MENU MOBILE (sidebar toggle)
   ============================================= */
 
/**
 * Inicializa o botão hamburger para abrir/fechar sidebar em mobile.
 * Deve ser chamado uma vez por página.
 */
function inicializarMenuMobile() {
    const menuBtn = document.querySelector('.menu-btn');
    const sidebar = document.querySelector('.sidebar');
    if (!menuBtn || !sidebar) return;
 
    menuBtn.addEventListener('click', () => sidebar.classList.toggle('open'));
 
    // Fecha sidebar ao clicar fora
    document.addEventListener('click', (e) => {
        if (!sidebar.contains(e.target) && !menuBtn.contains(e.target)) {
            sidebar.classList.remove('open');
        }
    });
}