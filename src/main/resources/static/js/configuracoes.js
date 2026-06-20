document.addEventListener("DOMContentLoaded", () => {
    carregarConta();

    document.getElementById("salvarConta")?.addEventListener("click", atualizarConta);
    document.getElementById("alterarSenha")?.addEventListener("click", trocarSenha);
});

function getToken() {
    return localStorage.getItem("authToken");
}

// Monta sidebar de acordo com o perfil
function montarSidebar(perfil) {
    const nav = document.querySelector(".nav");
    if (!nav) return;

    const menus = {
        PROFESSOR: [
            { icon: "bxs-dashboard", label: "Dashboard", page: "dashboard" },
            { icon: "bx-book-open",  label: "Provas",    page: "provas"    },
            { icon: "bx-group",      label: "Turmas",    page: "turmas"    },
            { icon: "bx-library",    label: "Banco de Questões", page: "banco" },
            { icon: "bx-line-chart", label: "Resultados", page: "resultados" },
            { icon: "bx-calendar-event", label: "Calendário", page: "calendario" },
        ],
        ALUNO: [
            { icon: "bxs-dashboard",    label: "Dashboard",  page: "dashboard"  },
            { icon: "bx-book-open",     label: "Provas",     page: "provas"     },
            { icon: "bx-bar-chart-alt-2", label: "Resultados", page: "resultados" },
            { icon: "bx-calendar-event", label: "Calendário", page: "calendario" },
        ],
        COORDENADOR: [
            { icon: "bxs-dashboard",    label: "Dashboard",  page: "dashboard"  },
            { icon: "bx-book-open",     label: "Provas",     page: "provas"     },
            { icon: "bx-group",         label: "Turmas",     page: "turmas"     },
            { icon: "bx-line-chart",    label: "Resultados", page: "resultados" },
        ],
        ADMIN: [
            { icon: "bxs-dashboard",  label: "Dashboard", page: "dashboard" },
            { icon: "bx-group",       label: "Usuários",  page: "usuarios"  },
            { icon: "bx-book-open",   label: "Provas",    page: "provas"    },
            { icon: "bx-bar-chart-alt-2", label: "Relatórios", page: "relatorios" },
        ],
    };

    const destinos = {
        PROFESSOR:  "/professor/prof.html",
        ALUNO:      "/aluno/aluno.html",
        COORDENADOR: "/coordenacao/cord.html",
        ADMIN:      "/admin/adm.html",
    };

    const baseUrl = destinos[perfil] || "/login.html";
    const itens = menus[perfil] || menus.PROFESSOR;

    nav.innerHTML = itens.map(item => `
        <a href="${baseUrl}#${item.page}" class="nav-item">
            <i class="bx ${item.icon}"></i>
            <span>${item.label}</span>
        </a>
    `).join("");
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

    document.getElementById("nomeUsuario").value  = usuario.nome;
    document.getElementById("emailUsuario").value = usuario.email;
    document.getElementById("perfilUsuario").value = usuario.perfil;

    document.getElementById("nomeHeader").innerText  = usuario.nome;
    document.getElementById("perfilHeader").innerText = usuario.perfil;
    document.getElementById("perfilTexto").innerText  = usuario.perfil;
    document.getElementById("avatarUsuario").innerText = usuario.nome.charAt(0).toUpperCase();

    montarSidebar(usuario.perfil);
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

    const labels = {
        PROFESSOR:   "Professor",
        ALUNO:       "Aluno",
        COORDENADOR: "Coordenação",
        ADMIN:       "Administrador",
    };

    const extras = {
        PROFESSOR:   "<div class=\"info-item\"><strong>Dados profissionais</strong>Serão carregados aqui.</div>",
        ALUNO:       "<div class=\"info-item\"><strong>Dados acadêmicos</strong>Serão carregados aqui.</div>",
        COORDENADOR: "",
        ADMIN:       "",
    };

    div.innerHTML = `
        <div class="info-item">
            <strong>Perfil</strong>${labels[perfil] || perfil}
        </div>
        ${extras[perfil] || ""}
    `;
}