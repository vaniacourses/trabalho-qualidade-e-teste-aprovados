# Relatorio PITest - Segunda Entrega

## Escopo

- Ferramenta: `PITest`
- Objetivo do plano: secao `5.4 Teste Baseado em Defeitos (>=80% escore de mutacao)`
- Relatorio atual usado: `02-codigo-ajustado/winxbank/target/pit-reports/index.html`
- Baseline usada: commit `fefb4da`, medido em copia temporaria da primeira entrega
- Classes-alvo medidas:
  - `RegistroDeClientes`
  - `Banco`
  - `CartaoCredito`
  - `ContaCorrente`
  - `Ano`

## Score por tarefa do plano

| Tarefa | Responsavel | Classe-alvo | Score anterior | Acoes / medidas tomadas | Score atual | Meta | Status |
|---|---|---|---:|---|---:|---:|---|
| 5.4.1 | Fernando Rene | `RegistroDeClientes` | Mutation coverage: `6%` | Testes para condicionais, retornos, CPF duplicado/inexistente, saldo limite, `ClienteWinx`, atualizacao, remocao e efeitos de integracao | Mutation coverage: `87%` | `>= 80%` | Atingida |
| 5.4.2 | Caio | `Banco` | Mutation coverage: `44%` | Testes para movimentacao entre banco e contas, receitas/despesas, abertura de conta e carteira mista | Mutation coverage: `100%` | `>= 80%` | Atingida |
| 5.4.3 | Joao Omar / Caio | `CartaoCredito` | Mutation coverage: `56%` | Testes para limite, fatura, credito, juros e integracao com banco/conta | Mutation coverage: `88%` | `>= 80%` | Atingida |
| 5.4.4 | Rafael | `ContaCorrente` | Mutation coverage: `14%` | Testes para compra debito/credito, confirmacao/cancelamento, taxa, fatura e integracao com cartao | Mutation coverage: `88%` | `>= 80%` | Atingida |
| 5.4.5 | Joao Victor | `Ano` | Mutation coverage: `87%` | Testes para passagem de mes, wrap-around e alteracao de mes atual | Mutation coverage: `87%` | `>= 80%` | Atingida |

## Comparativo completo por classe

| Classe | Line coverage anterior | Line coverage atual | Mutation coverage anterior | Mutation coverage atual | Test strength anterior | Test strength atual |
|---|---:|---:|---:|---:|---:|---:|
| `RegistroDeClientes` | 9% (8/87) | 100% (82/82) | 6% (3/54) | 87% (41/47) | 75% (3/4) | 87% (41/47) |
| `Banco` | 58% (41/71) | 100% (73/73) | 44% (17/39) | 100% (40/40) | 65% (17/26) | 100% (40/40) |
| `CartaoCredito` | 84% (27/32) | 89% (40/45) | 56% (9/16) | 88% (22/25) | 60% (9/15) | 92% (22/24) |
| `ContaCorrente` | 27% (11/41) | 100% (35/35) | 14% (4/28) | 88% (23/26) | 80% (4/5) | 88% (23/26) |
| `Ano` | 96% (22/23) | 96% (22/23) | 87% (13/15) | 87% (13/15) | 87% (13/15) | 87% (13/15) |

## Resultado geral atual

| Indicador | Score |
|---|---:|
| Line coverage nas classes mutadas | 98% (252/258) |
| Mutation coverage | 91% (139/153) |
| Test strength | 91% (139/152) |
| Mutacoes geradas | 153 |
| Mutacoes mortas | 139 |
| Mutacoes sem cobertura | 1 |

## Leitura do resultado

- Todas as tarefas de mutacao ficaram acima da meta de `80%`.
- Maiores evolucoes:
  - `Banco`: `44% -> 100%`
  - `ContaCorrente`: `14% -> 88%`
  - `RegistroDeClientes`: `6% -> 87%`
  - `CartaoCredito`: `56% -> 88%`
- `Ano` ja estava acima da meta e manteve o mesmo score.
