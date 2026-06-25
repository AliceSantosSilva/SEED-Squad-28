# SEED — Sistema de Gestão de Provas

Sistema web para digitalizar o ciclo completo de avaliação escolar — criação, aplicação e correção de provas — desenvolvido para a Secretaria de Estado da Educação de Sergipe.

> No front-end, o sistema também é apresentado com a marca **Prova Sergipe**.

## Índice

- [Sobre o projeto](#sobre-o-projeto)
- [Funcionalidades](#funcionalidades)
- [Perfis de usuário](#perfis-de-usuário)
- [Tecnologias](#tecnologias)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Pré-requisitos](#pré-requisitos)
- [Configuração e execução](#configuração-e-execução)
- [Documentação da API](#documentação-da-api)
- [Segurança](#segurança)
- [Status do projeto](#status-do-projeto)
- [Autor](#autor)

## Sobre o projeto

Hoje, em muitas escolas da rede pública, a aplicação e correção de provas ainda é um processo manual — o que gera atrasos, erros de correção, falta de transparência para alunos e famílias, e dificulta o acompanhamento do desempenho real por escola, turma e disciplina.

O SEED digitaliza esse ciclo: professores criam provas (manualmente ou por geração automática), alunos respondem online com correção automática instantânea, e coordenadores/administradores acompanham o desempenho em tempo real — tudo isolado por escola dentro de um único banco de dados.

## Funcionalidades

### Autenticação e segurança
- Login com JWT (token com expiração de 8h)
- Senhas criptografadas com BCrypt
- Primeiro acesso obriga a troca de senha
- Acesso às rotas da API controlado por perfil (role-based)
- Isolamento de dados por escola para Coordenador e Professor

### Gestão acadêmica
- CRUD completo das entidades acadêmicas: escolas, turmas, séries, disciplinas, questões, alternativas, provas, usuários
- Geração automática de provas, com sorteio de questões por disciplina/série/quantidade
- Variações de prova por aluno (embaralhamento da ordem das questões e das alternativas)
- Correção automática, com cálculo de nota e situação (aprovado/reprovado)
- Monitoramento antifraude durante a prova (detecta troca de aba / perda de foco da janela)
- Trilha de estudos personalizada — recomenda questões de reforço por disciplina com base no desempenho do aluno
- Calendário acadêmico (períodos de prova, provas específicas e eventos gerais)
- Fórum de discussão entre professores
- Relatórios de desempenho por escola e por rede

### Documentação
- API documentada automaticamente via Swagger / OpenAPI

## Perfis de usuário

| Perfil | Visão no sistema |
|---|---|
| **Administrador** | Toda a rede estadual |
| **Coordenador** | Apenas a própria escola |
| **Professor** | Suas turmas, questões e provas |
| **Aluno** | Suas próprias provas e desempenho |

## Tecnologias

**Backend**
- Java 21
- Spring Boot 3.2.5 (Web, Data JPA, Security, Validation)
- PostgreSQL (atualmente hospedado na Aiven)
- JWT — `io.jsonwebtoken` (jjwt) 0.12.5
- Springdoc OpenAPI / Swagger UI 2.5.0
- Maven

**Frontend**
- HTML5, CSS3 e JavaScript puro (sem frameworks), servidos como recursos estáticos pelo próprio Spring Boot
- Ícones: Boxicons · Fontes: Google Fonts (Inter, Montserrat)

## Estrutura do projeto

```
sistema-escolar/
├── src/main/java/com/projeto/sistema_escolar/
│   ├── config/         # Configurações (CORS, Swagger)
│   ├── controller/     # Endpoints REST
│   ├── dto/            # Objetos de entrada/saída da API
│   ├── exception/      # Tratamento global de erros
│   ├── model/          # Entidades JPA
│   ├── repository/     # Repositórios Spring Data JPA
│   ├── security/       # JWT (geração, filtro, configuração de segurança)
│   ├── service/        # Regras de negócio
│   └── util/           # Utilitários (ex.: filtro de escola por perfil logado)
├── src/main/resources/
│   └── static/         # Frontend (admin/, professor/, aluno/, coordenacao/, css/, js/, *.html)
├── pom.xml
└── mvnw / mvnw.cmd      # Maven Wrapper
```

## Pré-requisitos

- Java 21 (JDK)
- Maven — ou use o wrapper `mvnw` incluso no projeto, sem precisar instalar nada
- Uma instância PostgreSQL (local ou um provedor como a Aiven)

## Configuração e execução

> Os valores abaixo são um modelo de referência — confira os nomes exatos de propriedade no seu `application.properties` real antes de usar em produção.

1. Clone o repositório:
   ```bash
   git clone <url-do-repositorio>
   cd sistema-escolar
   ```

2. Crie o arquivo `src/main/resources/application.properties` (ele **não é versionado** — veja [Segurança](#segurança)) com, no mínimo:
   ```properties
   spring.datasource.url=jdbc:postgresql://<host>:<porta>/<banco>
   spring.datasource.username=<usuario>
   spring.datasource.password=<senha>
   spring.jpa.hibernate.ddl-auto=update

   jwt.secret=<uma-chave-secreta-grande-e-aleatoria>

   server.port=8081
   ```

3. Execute a aplicação:
   ```bash
   ./mvnw spring-boot:run        # Linux / macOS
   mvnw.cmd spring-boot:run      # Windows
   ```

4. Acesse:
   - Sistema: `http://localhost:8081`
   - Documentação da API (Swagger UI): `http://localhost:8081/swagger-ui.html`

## Documentação da API

Toda a API é documentada automaticamente via Swagger/OpenAPI. Para testar rotas protegidas:

1. Faça `POST /api/login` e copie o campo `token` da resposta
2. Clique em **Authorize** no topo da página do Swagger
3. Cole o token e confirme

## Segurança

- O arquivo `application.properties` (credenciais de banco e segredo do JWT) está no `.gitignore` e nunca deve ser commitado
- Senhas de usuários são sempre armazenadas com hash BCrypt, nunca em texto puro
- Toda rota sensível da API exige um token JWT válido e verifica o perfil do usuário antes de liberar o acesso
- Coordenadores e professores têm acesso restrito apenas aos dados da própria escola

## Status do projeto

**Já implementado:** autenticação JWT, CRUD completo das entidades principais, geração automática de provas, correção automática, monitoramento antifraude, trilha de estudos, calendário acadêmico, fórum de professores e painéis dos 4 perfis com dashboards.

**Em desenvolvimento / próximos passos:**
- Consolidação de algumas listagens dos painéis de Coordenador e Professor com o backend
- Geolocalização para provas presenciais
- Geração de certificados em PDF
- Bloqueio automático de aluno reprovado
- Testes automatizados e pipeline de deploy

## Autor

Desenvolvido por **Alice Santos, Davi Leocadio, Gabriel Gomes, João Antônio, João Victor, Larissa Azevedo, Michel Gabriel, Reginaldo Alves**.