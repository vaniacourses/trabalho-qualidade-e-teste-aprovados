# Relatorio JaCoCo - Segunda Entrega

## Escopo

- Ferramenta: `JaCoCo`
- Objetivo do plano: secao `5.2 Teste Estrutural (>=80% cobertura todas-arestas)`
- Relatorio atual usado: `02-codigo-ajustado/winxbank/target/site/jacoco/jacoco.csv`
- Baseline usada: commit `fefb4da`, medido em copia temporaria da primeira entrega

## Score por tarefa do plano

| Tarefa | Responsavel | Classe-alvo | Score anterior | Acoes / medidas tomadas | Score atual | Meta | Status |
|---|---|---|---:|---|---:|---:|---|
| 5.2.1 | Fernando Rene | `RegistroDeClientes` | Branch coverage: `3,85%` | Testes unitarios, funcionais e de integracao; cobertura de cadastro, CPF duplicado, busca, atualizacao, remocao e visualizacao; ajuste de testabilidade em `Banco.abrirNovaConta(Scanner)` | Branch coverage: `93,18%` | `>= 80%` | Atingida |
| 5.2.2 | Caio | `Banco` | Branch coverage: `84,62%` | Testes de movimentacao banco-conta, abertura de conta, receitas/despesas e cenarios de integracao | Branch coverage: `95,83%` | `>= 80%` | Atingida |
| 5.2.3 | Joao Omar | `CartaoCredito` | Branch coverage: `87,50%` | Testes de limite, fatura, credito, cobranca de juros e integracao com conta/banco | Branch coverage: `100%` | `>= 80%` | Atingida |
| 5.2.4 | Rafael | `ContaCorrente` | Branch coverage: `0%` | Testes de compra no debito/credito, confirmacao/cancelamento, taxa, fatura, pix e integracao com cartao | Branch coverage: `100%` | `>= 80%` | Atingida |
| 5.2.5 | Joao Victor | `Ano` | Branch coverage: `91,67%` | Testes de passagem de mes, wrap-around, contador e `setMesAtual` | Branch coverage: `91,67%` | `>= 80%` | Atingida |

## Comparativo completo por classe

| Classe | Instruction coverage anterior | Instruction coverage atual | Branch coverage anterior | Branch coverage atual | Line coverage anterior | Line coverage atual | Method coverage atual |
|---|---:|---:|---:|---:|---:|---:|---:|
| `RegistroDeClientes` | 5,04% (23/456) | 100% (415/415) | 3,85% (2/52) | 93,18% (41/44) | 9,20% (8/87) | 100% (82/82) | 100% (13/13) |
| `Banco` | 70,74% (191/270) | 100% (274/274) | 84,62% (22/26) | 95,83% (23/24) | 69,01% (49/71) | 100% (73/73) | 100% (13/13) |
| `CartaoCredito` | 100% (110/110) | 90,00% (135/150) | 87,50% (7/8) | 100% (10/10) | 100% (32/32) | 88,89% (40/45) | 93,75% (15/16) |
| `ContaCorrente` | 25,35% (36/142) | 100% (130/130) | 0% (0/8) | 100% (10/10) | 26,83% (11/41) | 100% (35/35) | 100% (8/8) |
| `Ano` | 97,83% (135/138) | 97,83% (135/138) | 91,67% (11/12) | 91,67% (11/12) | 95,65% (22/23) | 95,65% (22/23) | 100% (7/7) |

## Resultado geral atual das classes-alvo

- Todas as tarefas estruturais ficaram acima da meta de `80%` de branch coverage.
- Maiores evolucoes:
  - `RegistroDeClientes`: `3,85% -> 93,18%`
  - `ContaCorrente`: `0% -> 100%`
  - `Banco`: `84,62% -> 95,83%`
  - `CartaoCredito`: `87,50% -> 100%`
- `Ano` ja estava acima da meta na primeira entrega e permaneceu acima da meta.
