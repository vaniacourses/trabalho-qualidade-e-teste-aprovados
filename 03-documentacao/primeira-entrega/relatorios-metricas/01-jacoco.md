# Relatorio JaCoCo - Primeira Entrega

## Escopo medido

- Versao medida: commit `fefb4da`
- Marco usado: ultima versao antes dos commits de planejamento/organizacao da segunda entrega
- Caminho medido na copia temporaria: `/private/tmp/winxbank-primeira-fefb4da/02-codigo-ajustado/winxbank`
- Observacao: a primeira entrega nao tinha JaCoCo configurado. O plugin foi adicionado apenas na copia temporaria para medir, sem alterar codigo de producao ou testes.

## Estado da suite antiga

| Indicador | Valor |
|---|---:|
| Classes de teste | 5 |
| Testes executados | 44 |
| Falhas | 4 |
| Erros | 0 |
| Testes ignorados | 0 |

Falhas funcionais observadas:

- `BancoTest.testAbrirNovaContaComOpcaoInvalidaNaoDeveRetornarNull`
- `BancoTest.testAbrirNovaContaComOpcaoZeroNaoDeveRetornarNull`
- `BancoTest.testPrintarBancoNaoDeveModificarDespesasQuandoDespesasMaiorQueReceitas`
- `CartaoCreditoTest.testNaoDeveAjustarLimiteComValorNegativo`

## Score geral

| Metrica | Score | Coberto/total |
|---|---:|---:|
| Instruction coverage | 23,63% | 818/3461 |
| Branch coverage | 16,23% | 50/308 |
| Line coverage | 23,62% | 219/927 |
| Method coverage | 48,91% | 67/137 |

## Baseline por tarefa do plano

| Tarefa | Responsavel | Classe | Instruction coverage | Branch coverage | Line coverage |
|---|---|---|---:|---:|---:|
| 5.2.1 | Fernando Rene | `RegistroDeClientes` | 5,04% (23/456) | 3,85% (2/52) | 9,20% (8/87) |
| 5.2.2 | Caio | `Banco` | 70,74% (191/270) | 84,62% (22/26) | 69,01% (49/71) |
| 5.2.3 | Joao Omar | `CartaoCredito` | 100% (110/110) | 87,50% (7/8) | 100% (32/32) |
| 5.2.4 | Rafael | `ContaCorrente` | 25,35% (36/142) | 0% (0/8) | 26,83% (11/41) |
| 5.2.5 | Joao Victor | `Ano` | 97,83% (135/138) | 91,67% (11/12) | 95,65% (22/23) |

## Outras classes medidas

| Classe | Instruction coverage | Branch coverage | Line coverage |
|---|---:|---:|---:|
| `ContaPoupanca` | 51,85% (56/108) | 0% (0/2) | 51,61% (16/31) |
| `ClienteWinx` | 71,64% (48/67) | n/a | 72,22% (13/18) |

## Leitura do resultado

- A cobertura geral da primeira entrega era baixa: 16,23% de branches.
- A classe-alvo `RegistroDeClientes`, posteriormente trabalhada na segunda entrega, praticamente nao era exercitada:
  - apenas 3,85% de branch coverage
  - apenas 9,20% de line coverage
- Mesmo com algumas classes pontualmente bem cobertas, a suite ainda nao cobria o sistema de forma equilibrada.
- A suite antiga tambem nao era verde, pois havia 4 falhas funcionais.

## Conclusao para slide

- Antes da segunda entrega, a cobertura estrutural geral era baixa e desigual.
- `RegistroDeClientes` tinha cobertura estrutural quase inexistente.
- Isso justificou a criacao de testes unitarios, funcionais e de integracao focados nessa classe.
