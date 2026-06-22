// =============================================
// CALENDÁRIO - calendario.js
// Incluir no prof.html, cord.html, aluno.html
// =============================================

const Calendario = (() => {

    let dataAtual = new Date();
    let eventos = [];
    let perfilUsuario = '';

    const TIPOS = {
        PERIODO_PROVA: { label: 'Período de Prova', cor: '#7c3aed' },
        PROVA:         { label: 'Prova',             cor: '#2563eb' },
        EVENTO:        { label: 'Evento',            cor: '#059669' },
    };

    function getToken() {
        return localStorage.getItem('authToken');
    }

    async function init(perfil) {
        perfilUsuario = perfil;
        await carregarEventos();
        renderizar();
        bindModal();
    }

    async function carregarEventos() {
        try {
            const res = await fetch('/api/eventos', {
                headers: { 'Authorization': 'Bearer ' + getToken() }
            });
            if (res.ok) eventos = await res.json();
        } catch (e) {
            console.error('Erro ao carregar eventos:', e);
        }
    }

    function renderizar() {
        const container = document.getElementById('fullCalendarContainer');
        if (!container) return;

        const ano = dataAtual.getFullYear();
        const mes = dataAtual.getMonth();

        const nomesMeses = ['Janeiro','Fevereiro','Março','Abril','Maio','Junho',
                            'Julho','Agosto','Setembro','Outubro','Novembro','Dezembro'];
        const diasSemana = ['Dom','Seg','Ter','Qua','Qui','Sex','Sáb'];

        const primeiroDia = new Date(ano, mes, 1).getDay();
        const totalDias   = new Date(ano, mes + 1, 0).getDate();

        // Filtra eventos do mês
        const eventosMes = eventos.filter(e => {
            // Aluno não vê períodos de prova, só provas e eventos
            if (perfilUsuario === 'ALUNO' && e.tipo === 'PERIODO_PROVA') return false;
            const inicio = new Date(e.dataInicio + 'T00:00:00');
            const fim    = new Date(e.dataFim    + 'T00:00:00');
            return inicio.getFullYear() === ano && inicio.getMonth() === mes
                || fim.getFullYear()    === ano && fim.getMonth()    === mes
                || (inicio <= new Date(ano, mes, 1) && fim >= new Date(ano, mes, totalDias));
        });

        function eventosNoDia(dia) {
            const d = new Date(ano, mes, dia);
            return eventosMes.filter(e => {
                const inicio = new Date(e.dataInicio + 'T00:00:00');
                const fim    = new Date(e.dataFim    + 'T00:00:00');
                return d >= inicio && d <= fim;
            });
        }

        // Permissões
        const podeAdicionarPeriodo = ['COORDENADOR','ADMIN'].includes(perfilUsuario);
        const podeAdicionarProva   = ['PROFESSOR','COORDENADOR','ADMIN'].includes(perfilUsuario);

        let html = `
        <div class="cal-wrapper">
            <div class="cal-header">
                <button class="cal-nav" id="calPrev"><i class="bx bx-chevron-left"></i></button>
                <h3 class="cal-titulo">${nomesMeses[mes]} ${ano}</h3>
                <button class="cal-nav" id="calNext"><i class="bx bx-chevron-right"></i></button>
                ${podeAdicionarPeriodo || podeAdicionarProva ? `
                <button class="cal-btn-add" id="calBtnAdicionar">
                    <i class="bx bx-plus"></i> Adicionar
                </button>` : ''}
            </div>

            <div class="cal-legenda">
                ${Object.entries(TIPOS).map(([k,v]) => `
                    <span class="cal-leg-item">
                        <span class="cal-leg-dot" style="background:${v.cor}"></span>${v.label}
                    </span>`).join('')}
            </div>

            <div class="cal-grid">
                ${diasSemana.map(d => `<div class="cal-cell cal-head">${d}</div>`).join('')}
                ${Array(primeiroDia).fill('<div class="cal-cell cal-vazio"></div>').join('')}
                ${Array.from({length: totalDias}, (_, i) => {
                    const dia = i + 1;
                    const hoje = new Date();
                    const isHoje = dia === hoje.getDate() && mes === hoje.getMonth() && ano === hoje.getFullYear();
                    const evs = eventosNoDia(dia);
                    return `
                    <div class="cal-cell cal-dia ${isHoje ? 'cal-hoje' : ''}" data-dia="${dia}">
                        <span class="cal-num">${dia}</span>
                        <div class="cal-evs">
                            ${evs.slice(0,3).map(e => `
                                <div class="cal-ev" style="background:${TIPOS[e.tipo]?.cor || '#6b7280'}"
                                     data-id="${e.id}" title="${e.titulo}">
                                    ${e.titulo.length > 14 ? e.titulo.substring(0,14)+'…' : e.titulo}
                                </div>`).join('')}
                            ${evs.length > 3 ? `<div class="cal-ev-more">+${evs.length-3}</div>` : ''}
                        </div>
                    </div>`;
                }).join('')}
            </div>
        </div>

        <!-- MODAL ADICIONAR -->
        <div class="cal-modal-overlay" id="calModalOverlay" style="display:none">
            <div class="cal-modal">
                <div class="cal-modal-header">
                    <h3 id="calModalTitulo">Novo Evento</h3>
                    <button class="cal-modal-close" id="calModalClose"><i class="bx bx-x"></i></button>
                </div>
                <div class="cal-modal-body">
                    <label>Título</label>
                    <input id="calTitulo" placeholder="Ex: Prova de Matemática" />

                    <label>Descrição</label>
                    <textarea id="calDescricao" placeholder="Descrição opcional" rows="2"></textarea>

                    <div class="cal-modal-datas">
                        <div>
                            <label>Data início</label>
                            <input type="date" id="calDataInicio" />
                        </div>
                        <div>
                            <label>Data fim</label>
                            <input type="date" id="calDataFim" />
                        </div>
                    </div>

                    <label>Tipo</label>
                    <select id="calTipo">
                        ${podeAdicionarPeriodo ? '<option value="PERIODO_PROVA">Período de Prova</option>' : ''}
                        ${podeAdicionarProva   ? '<option value="PROVA">Prova</option>' : ''}
                        ${podeAdicionarPeriodo ? '<option value="EVENTO">Evento</option>' : ''}
                    </select>

                    <div id="calAvisoFora" class="cal-aviso" style="display:none">
                        ⚠️ Professores só podem marcar provas dentro de um período definido pela coordenação.
                    </div>

                    <button class="cal-btn-salvar" id="calBtnSalvar">Salvar</button>
                </div>
            </div>
        </div>

        <!-- MODAL DETALHE EVENTO -->
        <div class="cal-modal-overlay" id="calDetalheOverlay" style="display:none">
            <div class="cal-modal cal-modal-sm">
                <div class="cal-modal-header">
                    <h3 id="calDetalheTitulo"></h3>
                    <button class="cal-modal-close" id="calDetalheClose"><i class="bx bx-x"></i></button>
                </div>
                <div class="cal-modal-body" id="calDetalheBody"></div>
            </div>
        </div>`;

        container.innerHTML = html;

        // Navegação
        document.getElementById('calPrev').addEventListener('click', () => {
            dataAtual.setMonth(dataAtual.getMonth() - 1);
            renderizar();
        });
        document.getElementById('calNext').addEventListener('click', () => {
            dataAtual.setMonth(dataAtual.getMonth() + 1);
            renderizar();
        });

        // Clique nos eventos do calendário
        container.querySelectorAll('.cal-ev').forEach(el => {
            el.addEventListener('click', (e) => {
                e.stopPropagation();
                const id = parseInt(el.dataset.id);
                const ev = eventos.find(x => x.id === id);
                if (ev) abrirDetalhe(ev);
            });
        });
    }

    function bindModal() {
        document.addEventListener('click', e => {
            const overlay       = document.getElementById('calModalOverlay');
            const detalheOverlay = document.getElementById('calDetalheOverlay');

            // Abre modal de adicionar
            if (e.target.closest('#calBtnAdicionar')) {
                if (overlay) overlay.style.display = 'flex';
            }

            // Fecha modal de adicionar (X ou clique fora)
            if (e.target.closest('#calModalClose') || e.target === overlay) {
                if (overlay) overlay.style.display = 'none';
            }

            // Fecha modal de detalhe (X ou clique fora)
            if (e.target.closest('#calDetalheClose') || e.target === detalheOverlay) {
                if (detalheOverlay) detalheOverlay.style.display = 'none';
            }

            // Salvar evento
            if (e.target.closest('#calBtnSalvar')) salvarEvento();
        });
    }

    async function salvarEvento() {
        const titulo     = document.getElementById('calTitulo').value.trim();
        const descricao  = document.getElementById('calDescricao').value.trim();
        const dataInicio = document.getElementById('calDataInicio').value;
        const dataFim    = document.getElementById('calDataFim').value;
        const tipo       = document.getElementById('calTipo').value;

        if (!titulo || !dataInicio || !dataFim) {
            alert('Preencha título e datas.');
            return;
        }

        const res = await fetch('/api/eventos', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + getToken()
            },
            body: JSON.stringify({ titulo, descricao, dataInicio, dataFim, tipo })
        });

        if (res.ok) {
            document.getElementById('calModalOverlay').style.display = 'none';
            await carregarEventos();
            renderizar();
        } else {
            const err = await res.json();
            alert(err.erro || 'Erro ao salvar evento.');
        }
    }

    function abrirDetalhe(ev) {
        const overlay = document.getElementById('calDetalheOverlay');
        document.getElementById('calDetalheTitulo').textContent = ev.titulo;

        const podeExcluir = ['COORDENADOR','ADMIN'].includes(perfilUsuario)
            || (perfilUsuario === 'PROFESSOR' && ev.tipo === 'PROVA');

        document.getElementById('calDetalheBody').innerHTML = `
            <p><strong>Tipo:</strong> ${TIPOS[ev.tipo]?.label || ev.tipo}</p>
            <p><strong>Início:</strong> ${formatarData(ev.dataInicio)}</p>
            <p><strong>Fim:</strong> ${formatarData(ev.dataFim)}</p>
            ${ev.descricao ? `<p><strong>Descrição:</strong> ${ev.descricao}</p>` : ''}
            ${podeExcluir ? `<button class="cal-btn-excluir" id="calBtnExcluir" data-id="${ev.id}">
                <i class="bx bx-trash"></i> Excluir evento
            </button>` : ''}
        `;

        overlay.style.display = 'flex';

        const btnExcluir = document.getElementById('calBtnExcluir');
        if (btnExcluir) {
            btnExcluir.addEventListener('click', async () => {
                if (!confirm('Excluir este evento?')) return;
                const res = await fetch('/api/eventos/' + ev.id, {
                    method: 'DELETE',
                    headers: { 'Authorization': 'Bearer ' + getToken() }
                });
                if (res.ok) {
                    overlay.style.display = 'none';
                    await carregarEventos();
                    renderizar();
                } else {
                    alert('Erro ao excluir.');
                }
            });
        }
    }

    function formatarData(str) {
        if (!str) return '';
        const [y, m, d] = str.split('-');
        return `${d}/${m}/${y}`;
    }

    return { init };
})();