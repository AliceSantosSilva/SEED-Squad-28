/**
 * PROVA SERGIPE — global.js
 * Base compartilhada por todos os perfis.
 * Autenticação via JWT armazenado no localStorage.
 */

'use strict';

/* =============================================
   1. GERENCIAMENTO DO TOKEN JWT
   ============================================= */

function getToken() {
    return localStorage.getItem('token');
}

function setToken(token) {
    localStorage.setItem('token', token);
}

function removerToken() {
    localStorage.removeItem('token');
    localStorage.removeItem('usuarioNome');
    localStorage.removeItem('usuarioPerfil');
    localStorage.removeItem('usuarioId');
}

function salvarDadosUsuario(usuario) {
    localStorage.setItem('usuarioNome',   usuario.nome   || '');
    localStorage.setItem('usuarioPerfil', usuario.perfil || '');
    localStorage.setItem('usuarioId',     String(usuario.id || ''));
}

function getDadosUsuario() {
    return {
        nome:   localStorage.getItem('usuarioNome')   || 'Usuário',
        perfil: localStorage.getItem('usuarioPerfil') || '',
        id:     localStorage.getItem('usuarioId')     || '',
    };
}

/* =============================================
   2. FETCH CENTRALIZADO COM JWT
   ============================================= */

async function fetchAPI(url, options = {}, onSuccess, onError) {
    const token = getToken();

    const headers = {
        'Content-Type': 'application/json',
        ...(token ? { 'Authorization': `Bearer ${token}` } : {}),
        ...(options.headers || {}),
    };

    const config = { ...options, headers };

    try {
        const response = await fetch(url, config);

        if (response.status === 401) {
            removerToken();
            window.location.href = '/login.html';
            return;
        }

        if (!response.ok) {
            let mensagemErro = `Erro ${response.status}: ${response.statusText}`;
            try {
                const errData = await response.json();
                mensagemErro = errData.erro || errData.mensagem || errData.message || mensagemErro;
            } catch (_) {}
            throw new Error(mensagemErro);
        }

        const contentType = response.headers.get('content-type');
        if (!contentType || !contentType.includes('application/json')) {
            if (typeof onSuccess === 'function') onSuccess(null);
            return null;
        }

        const data = await response.json();
        if (typeof onSuccess === 'function') onSuccess(data);
        return data;

    } catch (error) {
        console.error('[fetchAPI] Falha:', error);
        if (typeof onError === 'function') {
            onError(error);
        } else {
            exibirAlerta(error.message || 'Erro de comunicação com o servidor.', 'erro');
        }
        throw error;
    }
}

/* =============================================
   3. ALERTAS FLUTUANTES
   ============================================= */

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
            position: 'fixed', bottom: '24px', right: '24px',
            display: 'flex', flexDirection: 'column', gap: '10px', zIndex: '9999',
        });
        document.body.appendChild(wrapper);
    }

    const alerta = document.createElement('div');
    alerta.innerHTML = `<i class='bx ${config.icon}'></i><span>${mensagem}</span>`;
    Object.assign(alerta.style, {
        display: 'flex', alignItems: 'center', gap: '10px',
        background: config.bg, color: 'white', padding: '12px 18px',
        borderRadius: '12px', fontSize: '13px', fontWeight: '500',
        boxShadow: '0 4px 16px rgba(0,0,0,0.15)', opacity: '0',
        transform: 'translateX(60px)', transition: 'opacity 0.3s ease, transform 0.3s ease',
        maxWidth: '320px', lineHeight: '1.4',
    });

    wrapper.appendChild(alerta);
    requestAnimationFrame(() => requestAnimationFrame(() => {
        alerta.style.opacity = '1';
        alerta.style.transform = 'translateX(0)';
    }));
    setTimeout(() => {
        alerta.style.opacity = '0';
        alerta.style.transform = 'translateX(60px)';
        alerta.addEventListener('transitionend', () => alerta.remove(), { once: true });
    }, duracao);
}

/* =============================================
   4. FORMATADORES
   ============================================= */

function formatarData(dataISO) {
    if (!dataISO) return '—';
    const [ano, mes, dia] = dataISO.split('-');
    return `${dia}/${mes}/${ano}`;
}

function formatarHora(horaISO) {
    if (!horaISO) return '—';
    return horaISO.substring(0, 5);
}

function getDataLocal(date) {
    const year  = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day   = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

/* =============================================
   5. CALENDÁRIO GENÉRICO
   ============================================= */

const DIAS_SEMANA = ['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb'];
const MESES = [
    'Janeiro','Fevereiro','Março','Abril','Maio','Junho',
    'Julho','Agosto','Setembro','Outubro','Novembro','Dezembro',
];

function renderCalendar(containerId, currentDate, eventsData = [], onDayClick) {
    const container = document.getElementById(containerId);
    if (!container) return;

    const ano       = currentDate.getFullYear();
    const mes       = currentDate.getMonth();
    const hoje      = new Date();
    const ehHoje    = (d) => d === hoje.getDate() && mes === hoje.getMonth() && ano === hoje.getFullYear();
    const primeiroDia = new Date(ano, mes, 1).getDay();
    const totalDias   = new Date(ano, mes + 1, 0).getDate();

    const eventosPorData = {};
    eventsData.forEach(ev => {
        if (!eventosPorData[ev.date]) eventosPorData[ev.date] = [];
        eventosPorData[ev.date].push(ev);
    });

    const diasHTML = [];
    for (let i = 0; i < primeiroDia; i++) diasHTML.push('<span></span>');
    for (let d = 1; d <= totalDias; d++) {
        const dateKey     = `${ano}-${String(mes+1).padStart(2,'0')}-${String(d).padStart(2,'0')}`;
        const eventos     = eventosPorData[dateKey] || [];
        const tiposUnicos = [...new Set(eventos.map(e => e.type))];
        const dotsHTML    = tiposUnicos.map(t => `<span class="cal-dot ${t}"></span>`).join('');
        diasHTML.push(
            `<span class="calendar-day ${ehHoje(d)?'today':''} ${eventos.length?'has-event':''}"
                   data-date="${dateKey}" title="${eventos.map(e=>e.title).join(', ')}">${d}${dotsHTML ? `<span class="cal-dots">${dotsHTML}</span>` : ''}</span>`
        );
    }

    container.innerHTML = `
        <div class="cal-header" style="display:flex;align-items:center;justify-content:space-between;margin-bottom:6px;">
            <button class="cal-nav" id="${containerId}-prev"><i class='bx bx-chevron-left'></i></button>
            <span style="font-weight:700;font-size:14px;color:#0f172a;">${MESES[mes]} ${ano}</span>
            <button class="cal-nav" id="${containerId}-next"><i class='bx bx-chevron-right'></i></button>
        </div>
        <div class="calendar-weekdays">${DIAS_SEMANA.map(d=>`<span>${d}</span>`).join('')}</div>
        <div class="calendar-days">${diasHTML.join('')}</div>
        <div class="calendar-legend">
            <span class="legend"><span class="dot exam"></span>Prova</span>
            <span class="legend"><span class="dot deadline"></span>Período de Prova</span>
            <span class="legend"><span class="dot meeting"></span>Evento</span>
        </div>`;

    if (typeof onDayClick === 'function') {
        container.querySelectorAll('.calendar-day[data-date]').forEach(el => {
            el.addEventListener('click', () => {
                onDayClick(el.dataset.date, eventosPorData[el.dataset.date] || []);
            });
        });
    }
    document.getElementById(`${containerId}-prev`)?.addEventListener('click', () => {
        const nova = new Date(ano, mes-1, 1);
        renderCalendar(containerId, nova, eventsData, onDayClick);
        container.dispatchEvent(new CustomEvent('calendarChange', { detail: { date: nova } }));
    });
    document.getElementById(`${containerId}-next`)?.addEventListener('click', () => {
        const nova = new Date(ano, mes+1, 1);
        renderCalendar(containerId, nova, eventsData, onDayClick);
        container.dispatchEvent(new CustomEvent('calendarChange', { detail: { date: nova } }));
    });
}

/* =============================================
   6. LOGOUT
   ============================================= */

async function logout() {
    try {
        await fetchAPI('/api/logout', { method: 'POST' });
    } catch (_) {}
    finally {
        removerToken();
        window.location.href = '/login.html';
    }
}

/* =============================================
   7. MENU MOBILE
   ============================================= */

function inicializarMenuMobile() {
    const menuBtn = document.querySelector('.menu-btn');
    const sidebar = document.querySelector('.sidebar');
    if (!menuBtn || !sidebar) return;
    menuBtn.addEventListener('click', () => sidebar.classList.toggle('open'));
    document.addEventListener('click', (e) => {
        if (!sidebar.contains(e.target) && !menuBtn.contains(e.target))
            sidebar.classList.remove('open');
    });
}

/* =============================================
   8. VERIFICAÇÃO DE AUTENTICAÇÃO JWT
   ============================================= */

async function verificarSessao(onSucesso) {
    const token = getToken();

    if (!token) {
        window.location.href = '/login.html';
        return;
    }

    try {
        const response = await fetch('/api/usuario/logado', {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!response.ok) {
            removerToken();
            window.location.href = '/login.html';
            return;
        }

        const usuario = await response.json();
        salvarDadosUsuario(usuario);

        if (typeof onSucesso === 'function') onSucesso(usuario);

    } catch (_) {
        removerToken();
        window.location.href = '/login.html';
    }
}

/* =============================================
   9. HEADER DO USUÁRIO
   ============================================= */

function preencherHeaderUsuario(usuario) {
    const avatarEl = document.querySelector('.user-avatar');
    const nomeEl   = document.querySelector('.user-name');
    const roleEl   = document.querySelector('.user-role');
    if (avatarEl) avatarEl.textContent = (usuario.nome || 'U').charAt(0).toUpperCase();
    if (nomeEl)   nomeEl.textContent   = usuario.nome || 'Usuário';
    if (roleEl)   roleEl.textContent   = capitalizarPerfil(usuario.perfil);
}

function capitalizarPerfil(perfil) {
    const mapa = {
        ADMIN: 'Administrador', PROFESSOR: 'Professor',
        ALUNO: 'Aluno', COORDENADOR: 'Coordenador',
    };
    return mapa[perfil?.toUpperCase()] || perfil || 'Usuário';
}

/* =============================================
   10. NOTIFICAÇÕES (modal/dropdown)
   ============================================= */

let _notificacoesCache = [];

async function buscarNotificacoes() {
    try {
        const token = localStorage.getItem('authToken');
        const res = await fetch('/api/notificacoes', {
            headers: token ? { 'Authorization': `Bearer ${token}` } : {},
        });
        if (res.ok) {
            const data = await res.json();
            if (Array.isArray(data)) return data;
        }
    } catch (_) { /* endpoint ainda não existe no backend — usa mock abaixo */ }

    return [
        { id: 1, tipo: 'aviso',   lida: false, titulo: 'Prazo de lançamento de notas',
          mensagem: 'O prazo para lançamento de notas encerra em 3 dias.',
          dataHora: new Date().toISOString() },
        { id: 2, tipo: 'info',    lida: false, titulo: 'Novo período de provas',
          mensagem: 'A coordenação abriu um novo período de provas para o bimestre.',
          dataHora: new Date(Date.now() - 86400000).toISOString() },
        { id: 3, tipo: 'sucesso', lida: true,  titulo: 'Prova corrigida automaticamente',
          mensagem: 'Os resultados da última prova já estão disponíveis.',
          dataHora: new Date(Date.now() - 2 * 86400000).toISOString() },
    ];
}

function tempoRelativo(dataISO) {
    const min = Math.floor((Date.now() - new Date(dataISO).getTime()) / 60000);
    if (min < 1) return 'agora mesmo';
    if (min < 60) return `há ${min} min`;
    const h = Math.floor(min / 60);
    if (h < 24) return `há ${h}h`;
    return `há ${Math.floor(h / 24)}d`;
}

const ICONES_NOTIFICACAO = {
    info: 'bx-info-circle', sucesso: 'bx-check-circle',
    aviso: 'bx-error', erro: 'bx-x-circle',
};

function renderizarNotificacoes() {
    const lista = document.getElementById('notification-list');
    const badge = document.querySelector('.notification-badge');
    if (!lista) return;

    const naoLidas = _notificacoesCache.filter(n => !n.lida).length;
    if (badge) badge.style.display = naoLidas > 0 ? 'flex' : 'none';
    if (badge && naoLidas > 0) badge.textContent = naoLidas > 9 ? '9+' : naoLidas;

    if (!_notificacoesCache.length) {
        lista.innerHTML = '<div class="notification-empty">Nenhuma notificação por aqui.</div>';
        return;
    }

    lista.innerHTML = _notificacoesCache.map(n => `
        <div class="notification-item ${n.lida ? '' : 'unread'}" data-id="${n.id}">
            <div class="notification-item-icon ${n.tipo}"><i class='bx ${ICONES_NOTIFICACAO[n.tipo] || 'bx-bell'}'></i></div>
            <div class="notification-item-body">
                <div class="notification-item-title">${n.titulo}</div>
                <div class="notification-item-text">${n.mensagem}</div>
                <div class="notification-item-time">${tempoRelativo(n.dataHora)}</div>
            </div>
        </div>`).join('');

    lista.querySelectorAll('.notification-item').forEach(item => {
        item.addEventListener('click', () => {
            const notif = _notificacoesCache.find(n => n.id === parseInt(item.dataset.id));
            if (notif) notif.lida = true;
            renderizarNotificacoes();
        });
    });
}

function criarPainelNotificacoes() {
    if (document.getElementById('notification-panel')) return;
    const painel = document.createElement('div');
    painel.id = 'notification-panel';
    painel.className = 'notification-panel';
    painel.innerHTML = `
        <div class="notification-panel-header">
            <h3>Notificações</h3>
            <button class="notification-mark-all" id="notification-mark-all">Marcar todas como lidas</button>
        </div>
        <div class="notification-list" id="notification-list"></div>`;
    document.body.appendChild(painel);

    document.getElementById('notification-mark-all').addEventListener('click', () => {
        _notificacoesCache.forEach(n => n.lida = true);
        renderizarNotificacoes();
    });
}

function inicializarNotificacoes() {
    const btn = document.querySelector('.notification-btn');
    if (!btn) return;

    criarPainelNotificacoes();
    buscarNotificacoes().then(lista => { _notificacoesCache = lista; renderizarNotificacoes(); });

    const painel = document.getElementById('notification-panel');
    btn.addEventListener('click', (e) => { e.stopPropagation(); painel.classList.toggle('open'); });
    document.addEventListener('click', (e) => {
        if (!painel.contains(e.target) && !btn.contains(e.target)) painel.classList.remove('open');
    });
}

document.addEventListener('DOMContentLoaded', inicializarNotificacoes);