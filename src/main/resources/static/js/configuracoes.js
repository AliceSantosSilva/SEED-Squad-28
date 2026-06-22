document.addEventListener("DOMContentLoaded", () => {
    carregarConta();
    document.getElementById("salvarConta")?.addEventListener("click", atualizarConta);
    document.getElementById("alterarSenha")?.addEventListener("click", trocarSenha);
});

function getToken() {
    return localStorage.getItem("authToken");
}

// Formata perfil: "PROFESSOR" → "Professor"
function formatarPerfil(perfil) {
    const labels = {
        PROFESSOR:   "Professor",
        ALUNO:       "Aluno",
        COORDENADOR: "Coordenação",
        ADMIN:       "Administrador",
    };
    return labels[perfil] || perfil;
}

// Monta sidebar de acordo com o perfil — ícones idênticos aos dashboards originais
function montarSidebar(perfil) {
    const nav = document.querySelector(".nav");
    if (!nav) return;

    const menus = {
        PROFESSOR: [
            { icon: "bxs-dashboard",     label: "Dashboard",        page: "dashboard"  },
            { icon: "bx-book-open",      label: "Provas",           page: "provas"     },
            { icon: "bx-group",          label: "Turmas",           page: "turmas"     },
            { icon: "bx-library",        label: "Banco de Questões",page: "banco"      },
            { icon: "bx-line-chart",     label: "Resultados",       page: "resultados" },
            { icon: "bx-calendar-event", label: "Calendário",       page: "calendario" },
        ],
        ALUNO: [
            { icon: "bx-grid-alt",       label: "Dashboard",        page: "dashboard"     },
            { icon: "bx-book-open",      label: "Minhas Provas",    page: "minhas-provas" },
            { icon: "bx-line-chart",     label: "Resultados",       page: "resultados"    },
            { icon: "bx-calendar-event", label: "Calendário",       page: "calendario"    },
        ],
        COORDENADOR: [
            { icon: "bxs-dashboard",     label: "Dashboard",   page: "dashboard"   },
            { icon: "bxs-school",        label: "Escolas",     page: "escolas"     },
            { icon: "bxs-user-badge",    label: "Professores", page: "professores" },
            { icon: "bx-group",          label: "Turmas",      page: "turmas"      },
            { icon: "bx-book-open",      label: "Provas",      page: "provas"      },
            { icon: "bx-line-chart",     label: "Relatórios",  page: "relatorios"  },
            { icon: "bx-calendar-event", label: "Calendário",  page: "calendario"  },
        ],
        ADMIN: [
            { icon: "bxs-dashboard",       label: "Dashboard",   page: "dashboard"   },
            { icon: "bxs-user-detail",     label: "Alunos",      page: "alunos"      },
            { icon: "bxs-book-alt",        label: "Professores", page: "professores" },
            { icon: "bxs-file-doc",        label: "Provas",      page: "provas"      },
            { icon: "bxs-bar-chart-alt-2", label: "Relatórios",  page: "relatorios"  },
        ],
    };

    const destinos = {
        PROFESSOR:   "/professor/prof.html",
        ALUNO:       "/aluno/aluno.html",
        COORDENADOR: "/coordenacao/cord.html",
        ADMIN:       "/admin/adm.html",
    };

    const baseUrl = destinos[perfil] || "/login.html";
    const itens   = menus[perfil]   || menus.PROFESSOR;

    nav.innerHTML = itens.map(item => `
        <a href="${baseUrl}" class="nav-item">
            <i class="bx ${item.icon}"></i>
            <span>${item.label}</span>
        </a>
    `).join("");
}

function montarFooter(perfil) {
    const footer = document.querySelector(".sidebar-footer");
    if (!perfil || !footer) return;

    // Itens extras por perfil antes de Configurações
    const extras = {
        ADMIN: `<a href="/admin/adm.html" class="nav-item">
                    <i class="bx bx-help-circle"></i>
                    <span>Ajuda</span>
                </a>`,
    };

    // Reconstrói o footer mantendo Configurações (ativo) e Sair
    footer.innerHTML = `
        ${extras[perfil] || ''}
        <a href="/configuracoes.html" class="nav-item active">
            <i class="bx bx-cog"></i>
            <span>Configurações</span>
        </a>
        <a href="#" class="nav-item" id="logoutBtn">
            <i class="bx bx-log-out-circle"></i>
            <span>Sair</span>
        </a>
    `;

    document.getElementById("logoutBtn")?.addEventListener("click", (e) => {
        e.preventDefault();
        if (confirm("Deseja sair da sua conta?")) {
            localStorage.removeItem("authToken");
            localStorage.removeItem("usuarioPerfil");
            localStorage.removeItem("usuarioNome");
            window.location.href = "/login.html";
        }
    });
}

async function carregarConta() {
    const token = getToken();

    const resposta = await fetch("/api/minha-conta", {
        method: "GET",
        headers: { "Authorization": "Bearer " + token }
    });

    if (!resposta.ok) {
        window.location.href = "/login.html";
        return;
    }

    const usuario = await resposta.json();
    const perfilFormatado = formatarPerfil(usuario.perfil);

    document.getElementById("nomeUsuario").value   = usuario.nome;
    document.getElementById("emailUsuario").value  = usuario.email;
    document.getElementById("perfilUsuario").value = perfilFormatado;

    document.getElementById("nomeHeader").innerText   = usuario.nome;
    document.getElementById("perfilHeader").innerText = perfilFormatado;
    document.getElementById("perfilTexto").innerText  = perfilFormatado;
    document.getElementById("avatarUsuario").innerText = usuario.nome.charAt(0).toUpperCase();

    montarSidebar(usuario.perfil);
    montarFooter(usuario.perfil);
    carregarDadosPerfil(usuario.perfil);
}

async function atualizarConta() {
    const token = getToken();
    const dados = {
        nome:  document.getElementById("nomeUsuario").value,
        email: document.getElementById("emailUsuario").value
    };

    const resposta = await fetch("/api/minha-conta", {
        method: "PUT",
        headers: {
            "Content-Type": "application/json",
            "Authorization": "Bearer " + token
        },
        body: JSON.stringify(dados)
    });

    if (resposta.ok) {
        alert("Dados atualizados com sucesso!");
        carregarConta();
    } else {
        alert("Erro ao atualizar dados");
    }
}

async function trocarSenha() {
    const token = getToken();
    const dados = {
        senhaAtual: document.getElementById("senhaAtual").value,
        novaSenha:  document.getElementById("novaSenha").value
    };

    const resposta = await fetch("/api/minha-conta/senha", {
        method: "PUT",
        headers: {
            "Content-Type": "application/json",
            "Authorization": "Bearer " + token
        },
        body: JSON.stringify(dados)
    });

    const mensagem = await resposta.text();
    alert(mensagem);
}

function carregarDadosPerfil(perfil) {
    const div = document.getElementById("dadosExtras");

    const extras = {
        PROFESSOR:   "<div class=\"info-item\"><strong>Dados profissionais</strong>Serão carregados aqui.</div>",
        ALUNO:       "<div class=\"info-item\"><strong>Dados acadêmicos</strong>Serão carregados aqui.</div>",
        COORDENADOR: "",
        ADMIN:       "",
    };

    div.innerHTML = `
        <div class="info-item">
            <strong>Perfil</strong>${formatarPerfil(perfil)}
        </div>
        ${extras[perfil] || ""}
    `;
}