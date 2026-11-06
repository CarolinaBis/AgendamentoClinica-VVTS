# Mutation.md

Análise de mutantes com o PIT 1.18.1 nas classes de `br.edu.ifsp.dominio` e `br.edu.ifsp.aplicacao` (16 classes).

## Resultado

| Mutantes gerados | Mortos | Sobreviventes | Equivalentes |
|---|---|---|---|
| 165 | 165 (100%) | 0 | 0 |

## Mutantes equivalentes

Nenhum. Todos os mutantes foram mortos por algum teste, então não há tabela por classe.

O PIT não numera os mutantes: ele os identifica pela classe, pelo método, pela linha e pelo mutador.

## Como chegamos a 100%

A primeira execução matou 156 de 165 mutantes (95%). Os 9 que sobraram não eram equivalentes: faltavam testes (métodos nunca executados ou retornos que nenhum teste conferia). Cada um foi morto com um teste novo em `src/test/java/br/edu/ifsp/mutacao/`, e a execução final deu 165/165.
