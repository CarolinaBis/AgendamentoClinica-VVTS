# Agendamento Clínico (AgendamentoClinica-VVTS)

Sistema backend para gerenciamento e agendamento de consultas médicas e clínicas, desenvolvido em **Java** e **Spring Boot** com foco em arquitetura orientada a domínio (**Domain-Driven Design - DDD**), segurança via tokens **JWT** e garantia de qualidade de software através de **TDD** e suítes estruturadas de testes funcionais.
---

## Principais Características

- **Arquitetura Orientada a Domínio (DDD):** Domínio isolado com agregados, entidades, regras de políticas de negócio e exceções específicas desacopladas da infraestrutura.
- **Segurança & Controle de Acesso:** Fluxo de autenticação e proteção de rotas com JWT persistido e consultado via repositórios de dados.
- **Estratégia de Testes Abrangente:** Suíte robusta incluindo testes guiados por desenvolvimento (TDD), testes funcionais e testes de suporte para validação contínua.
- **Padrão de Camadas Limpo:** Clara segregação entre aplicação, controladores, regras centrais de negócio e camada de infraestrutura/persistência.

---

## Tecnologias Utilizadas

- **Linguagem:** Java 17+ (ou Java 21)
- **Framework:** Spring Boot (Spring Web, Spring Security)
- **Segurança:** Autenticação stateless via JSON Web Token (JWT)
- **Persistência / Acesso a Dados:** Repositórios desacoplados na infraestrutura (JDBC / Spring Data)
- **Testes & Qualidade:** JUnit 5, TDD, Testes Funcionais e Test Suites
- **Gerenciador de Build:** Maven

---

## Arquitetura e Estrutura de Diretórios

O projeto adota a separação de responsabilidades alinhada aos conceitos de DDD e Clean Architecture:

### Código Principal (`src/main/java/br/edu/ifsp/`)
```text
br/edu/ifsp/
├── aplicacao/       # Casos de uso e orquestração de serviços de negócio (US1 a US10)
├── controller/      # Endpoints REST e serialização de dados HTTP
├── dominio/         # Agregados, entidades de domínio, políticas e regras centrais
├── exception/       # Exceções de domínio e tratamentos customizados
├── infraestrutura/  # Implementação de repositórios, persistência e adaptadores
├── security/        # Autenticação JWT, filtros de segurança e credenciais
└── DemoAuthAppApplication.java

br/edu/ifsp/
├── funcional/       # Testes end-to-end e validação de fluxos do usuário
├── suites/          # Agrupamento e execução automatizada de suítes de teste
├── suporte/         # Infraestrutura, fixtures e dados utilitários para testes
└── tdd/             # Testes unitários orientados ao desenvolvimento prévio do domínio
