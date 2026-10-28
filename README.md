# Agendamento Clínico (AgendamentoClinica-VVTS)

Sistema backend para gerenciamento e agendamento de consultas médicas e clínicas, desenvolvido em **Java** e **Spring Boot** com foco em arquitetura orientada a domínio (**Domain-Driven Design - DDD**), segurança via tokens **JWT** e garantia de qualidade de software através de **TDD** e suítes estruturadas de testes funcionais[cite: 6, 7].

---

## Principais Características

- **Arquitetura Orientada a Domínio (DDD):** Domínio isolado com agregados, entidades, regras de políticas de negócio e exceções específicas desacopladas da infraestrutura[cite: 6].
- **Segurança & Controle de Acesso:** Fluxo de autenticação e proteção de rotas com JWT persistido e consultado via repositórios de dados[cite: 6].
- **Estratégia de Testes Abrangente:** Suíte robusta incluindo testes guiados por desenvolvimento (TDD), testes funcionais e testes de suporte para validação contínua.
- **Padrão de Camadas Limpo:** Clara segregação entre aplicação, controladores, regras centrais de negócio e camada de infraestrutura/persistência[cite: 6].

---

## Tecnologias Utilizadas

- **Linguagem:** Java 17+ (ou Java 21)
- **Framework:** Spring Boot (Spring Web, Spring Security)[cite: 6]
- **Segurança:** Autenticação stateless via JSON Web Token (JWT)[cite: 6]
- **Persistência / Acesso a Dados:** Repositórios desacoplados na infraestrutura (JDBC / Spring Data)[cite: 6]
- **Testes & Qualidade:** JUnit 5, TDD, Testes Funcionais e Test Suites[cite: 7]
- **Gerenciador de Build:** Maven

---

## Arquitetura e Estrutura de Diretórios

O projeto adota a separação de responsabilidades alinhada aos conceitos de DDD e Clean Architecture[cite: 6, 7]:

### Código Principal (`src/main/java/br/edu/ifsp/`)
```text
br/edu/ifsp/[cite: 6]
├── aplicacao/       # Casos de uso e orquestração de serviços de negócio (US1 a US10)[cite: 6]
├── controller/      # Endpoints REST e serialização de dados HTTP[cite: 6]
├── dominio/         # Agregados, entidades de domínio, políticas e regras centrais[cite: 6]
├── exception/       # Exceções de domínio e tratamentos customizados[cite: 6]
├── infraestrutura/  # Implementação de repositórios, persistência e adaptadores[cite: 6]
├── security/        # Autenticação JWT, filtros de segurança e credenciais[cite: 6]
└── DemoAuthAppApplication.java[cite: 6]
