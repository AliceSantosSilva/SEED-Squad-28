/**
 * MONITOR DE INCIDENTES DURANTE A PROVA
 * Detecta troca de aba / perda de foco da janela enquanto o aluno
 * está respondendo, registra no backend e avisa o aluno.
 *
 * Uso na tela de prova:
 *   MonitorProva.iniciar(provaId);  // ao abrir a prova
 *   MonitorProva.parar();           // ao enviar/finalizar
 */
const MonitorProva = (() => {
    let provaIdAtual = null;
    let totalIncidentes = 0;
    let ativo = false;
    let ultimoRegistro = 0;
    const INTERVALO_MINIMO_MS = 3000; // evita registrar disparos repetidos

    function getToken() { return localStorage.getItem('authToken'); }

    async function registrarIncidente(tipo, mensagem) {
        const agora = Date.now();
        if (agora - ultimoRegistro < INTERVALO_MINIMO_MS) return;
        ultimoRegistro = agora;
        totalIncidentes++;

        try {
            const res = await fetch(`/api/aplicacao/provas/${provaIdAtual}/incidente`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + getToken() },
                body: JSON.stringify({ tipo, mensagem }),
            });
            if (res.ok) {
                const data = await res.json();
                totalIncidentes = data.totalIncidentes ?? totalIncidentes;
            }
        } catch (e) {
            console.error('[MonitorProva] Falha ao registrar incidente:', e);
        }

        if (typeof exibirAlerta === 'function') {
            exibirAlerta(`Atenção: saída da tela da prova detectada (${totalIncidentes}ª ocorrência). Isso fica registrado no seu resultado.`, 'aviso', 5000);
        }
    }

    function onVisibilityChange() {
        if (ativo && document.hidden) {
            registrarIncidente('SAIU_DA_ABA', 'Aluno saiu da aba/minimizou a janela durante a prova.');
        }
    }
    function onWindowBlur() {
        if (ativo) registrarIncidente('PERDEU_FOCO', 'A janela da prova perdeu o foco.');
    }

    function iniciar(provaId) {
        provaIdAtual = provaId;
        totalIncidentes = 0;
        ativo = true;
        document.addEventListener('visibilitychange', onVisibilityChange);
        window.addEventListener('blur', onWindowBlur);
    }
    function parar() {
        ativo = false;
        document.removeEventListener('visibilitychange', onVisibilityChange);
        window.removeEventListener('blur', onWindowBlur);
    }

    return { iniciar, parar, getTotalIncidentes: () => totalIncidentes };
})();