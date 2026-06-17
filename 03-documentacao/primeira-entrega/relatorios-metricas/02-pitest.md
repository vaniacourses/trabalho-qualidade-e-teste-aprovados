# Relatorio PITest - Primeira Entrega

## Escopo medido

- Versao medida: commit `fefb4da`
- Caminho medido na copia temporaria: `/private/tmp/winxbank-primeira-fefb4da/02-codigo-ajustado/winxbank`
- Observacao: a primeira entrega nao tinha PITest configurado. O plugin foi adicionado apenas na copia temporaria para medicao.
- Classes-alvo configuradas para a medicao:
  - `RegistroDeClientes`
  - `Banco`
  - `CartaoCredito`
  - `ContaCorrente`
  - `Ano`

## Estado da suite usada pelo PITest

- A suite da primeira entrega tinha 44 testes.
- A execucao normal da suite apresentava 4 falhas funcionais.
- O PITest conseguiu gerar relatorio, mas o score deve ser lido como baseline tecnico, nao como quality gate aprovado, porque a suite antiga nao estava 100% verde.

## Score geral

| Indicador | Score |
|---|---:|
| Classes analisadas | 5 |
| Line coverage nas classes mutadas | 43% (109/254) |
| Mutation coverage | 30% (46/152) |
| Test strength | 71% (46/65) |
| Mutacoes geradas | 152 |
| Mutacoes mortas | 46 |
| Mutacoes sem cobertura | 87 |

## Score por pacote

| Pacote | Classes | Line coverage | Mutation coverage | Test strength |
|---|---:|---:|---:|---:|
| `br.winxbank.sistemabancario` | 3 | 55% (79/144) | 36% (30/83) | 65% (30/46) |
| `br.winxbank.sistemaclientes` | 1 | 9% (8/87) | 6% (3/54) | 75% (3/4) |
| `br.winxbank.tempo` | 1 | 96% (22/23) | 87% (13/15) | 87% (13/15) |

## Baseline por tarefa do plano

| Tarefa | Responsavel | Classe | Line coverage | Mutation coverage | Test strength |
|---|---|---|---:|---:|---:|
| 5.4.1 | Fernando Rene | `RegistroDeClientes` | 9% (8/87) | 6% (3/54) | 75% (3/4) |
| 5.4.2 | Caio | `Banco` | 58% (41/71) | 44% (17/39) | 65% (17/26) |
| 5.4.3 | Joao Omar / Caio | `CartaoCredito` | 84% (27/32) | 56% (9/16) | 60% (9/15) |
| 5.4.4 | Rafael | `ContaCorrente` | 27% (11/41) | 14% (4/28) | 80% (4/5) |
| 5.4.5 | Joao Victor | `Ano` | 96% (22/23) | 87% (13/15) | 87% (13/15) |

## Leitura do resultado

- O mutation score geral da primeira entrega era 30%, bem abaixo da meta de 80%.
- `RegistroDeClientes` tinha apenas 6% de mutation coverage.
- A maior parte dos mutantes de `RegistroDeClientes` ficou sem cobertura, indicando ausencia de testes efetivos para:
  - cadastro de cliente
  - CPF duplicado
  - retorno por CPF
  - atualizacao
  - remocao
  - visualizacao de detalhes
- `Ano` era a unica classe-alvo acima de 80% em mutacao.

## Conclusao para slide

- Antes da segunda entrega, a suite nao era forte contra defeitos artificiais.
- O maior gap identificado era `RegistroDeClientes`: 6% de mutation coverage.
- A segunda entrega precisava criar testes capazes de matar mutantes em condicionais, retornos e efeitos colaterais.
