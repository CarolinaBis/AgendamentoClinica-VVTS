# Front-end — Agendamento de Clínica

Interface web de **um único arquivo** (`index.html`: HTML + CSS + JavaScript puro, sem build e sem dependências),
integrada à API REST do projeto. Inclui **registro e login** (JWT) e as funções principais do sistema.

## Como executar

Requisitos: JDK 21 e o back-end deste repositório.

1. Suba o back-end: rode `DemoAuthAppApplication` no IntelliJ, ou no terminal `.\mvnw.cmd spring-boot:run` (Windows) / `./mvnw spring-boot:run`.
2. Abra no navegador: **http://localhost:8080/**
3. Em **Registrar usuário**, crie uma conta e depois use **Entrar**. O token fica só na aba do navegador (`sessionStorage`) e some ao fechá-la.

O próprio Spring Boot serve a pasta `frontend/`

## Configuração

| Onde | O quê | Para quê |
|---|---|---|
| `application.properties` | `spring.web.resources.static-locations=file:frontend/,classpath:/static/` | O Spring Boot passa a servir `frontend/` |
| `SecurityConfiguration.java` | `"/"` e `"/index.html"` na lista `WHITE_LIST_URL` | Libera a página sem token (a API continua protegida) |
| `frontend/index.html` | `window.API_BASE = ""` | Endereço do back-end. Vazio = o mesmo de onde a página foi aberta |

## Como usar

1. **Cadastros:** cadastre um procedimento (ex.: "Consulta clínica") e um paciente. O aviso mostra o `id` criado.
2. **Agendar consulta:** informe o id do paciente, um id de profissional (qualquer número, ex.: 10), início e fim e o(s) id(s) do procedimento. Regras: segunda a sexta, das 08:00 às 18:00, em data futura.
3. **Agenda do profissional:** informe o id do profissional e a data (vazio = hoje).
4. **Histórico / faltas:** consulte o histórico do paciente (com filtros opcionais) e o total de faltas. Com 3 faltas vigentes o paciente fica **bloqueado**.
5. **Ações em um agendamento:** informe o id e cancele, remarque, registre falta, confirme a realização ou mexa nos procedimentos.

Os erros do servidor aparecem na faixa do topo, em vermelho (ex.: `422` fora do horário de funcionamento, `409` conflito de agenda, `404` não encontrado).
Se o token expirar, a página volta sozinha para o login.

## Hospedagem (Vercel)

**Endereço publicado:** https://agendamentoclinica-vvts-frontend.vercel.app 

O Vercel serve apenas o front-end (site estático). Ele **não executa o Spring Boot**.

### Ligar ao back-end

- **Sem back-end público:** a página abre, mas *Registrar* e *Entrar* mostram "O servidor da API não respondeu". Para usar o sistema, rode o back-end local e abra http://localhost:8080/.

