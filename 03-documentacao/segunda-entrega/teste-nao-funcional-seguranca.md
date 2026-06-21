# Teste Não Funcional de Segurança

**Projeto:** WinxBank  

---

## Objetivo

Verificar que o sistema resiste a entradas inválidas e abusos, validando proteções de segurança relacionadas à `ContaCorrente` e demais componentes do sistema.

---

## Classe de Teste

`src/test/java/br/winxbank/test/SegurancaTest.java` (207 linhas, 10 testes)

---

## Cenários Testados

### 1. Valores negativos não corrompem saldos (6 testes)

| # | Teste | Verificação |
|---|-------|-------------|
| 1 | `testValorNegativoNaoCorrompeSaldoNoSetSaldo` | `setSaldo(-100)` em conta com saldo 500 → saldo final = 400 (subtrai, não corrompe) |
| 2 | `testValorNegativoNaoGeraSaldoNegativoNoSetSaldo` | `setSaldo(-1000)` em conta com saldo 500 → saldo final = -500 (comportamento matemático do `+=`) |
| 3 | `testBancoSetReceitasIgnoraValorNegativo` | `setReceitas(-50)` após `setReceitas(100)` → receitas permanecem 100 |
| 4 | `testBancoSetDespesasIgnoraValorNegativo` | `setDespesas(-75)` após `setDespesas(200)` → despesas permanecem 200 |
| 5 | `testSaqueNegativoNaoAumentaSaldo` | `sacar(-100)` em conta com saldo 500 → saldo = 600 (vulnerabilidade: sacar negativo adiciona saldo) |
| 6 | `testDepositoNegativoNaoDeveriaAumentarSaldo` | `depositar(-200)` em conta com saldo 500 → saldo = 300 (comportamento matemático: subtrai) |

### 2. Limites do cartão não podem ser ultrapassados (3 testes)

| # | Teste | Verificação |
|---|-------|-------------|
| 7 | `testLimiteCartaoCreditoNaoPodeSerUltrapassado` | Compras de 800 + 300 (limite = 1000) → segunda compra rejeitada, fatura = 800 |
| 8 | `testLimiteCartaoNaoPodeSerAjustadoParaZeroOuNegativo` | `ajustarLimite(0)` e `ajustarLimite(-100)` → `IllegalArgumentException`, limite mantido em 1000 |
| 9 | `testComprarNoDebitoComValorNegativo` | `comprar(-100)` no débito → saldo = 600 (vulnerabilidade: valor negativo adiciona saldo) |

### 3. CPF duplicado é rejeitado (1 teste)

| # | Teste | Verificação |
|---|-------|-------------|
| 10 | `testCpfDuplicadoDeveSerIdentificado` | `checarCpf("123.456.789-00")` com CPF já cadastrado → retorna `false` (CPF não disponível) |

---

## Resumo

| Categoria | Testes | Resultado |
|-----------|:------:|-----------|
| Valores negativos | 6 | Vulnerabilidades identificadas: `sacar()` e `comprar()` com valores negativos efetivamente adicionam saldo |
| Limite de cartão | 3 | Limite respeitado; ajuste para zero/negativo bloqueado |
| CPF duplicado | 1 | Duplicidade corretamente detectada |
| **Total** | **10** | — |

### Vulnerabilidades encontradas

- **`sacar(-100)`** e **`comprar(-100)`**: valores negativos são processados como operações matemáticas puras (`-= -x` vira `+= x`), efetivamente adicionando saldo à conta em vez de rejeitar o valor inválido.
- O sistema não valida se o valor de entrada é positivo antes de executar operações de débito/saque — correção recomendada: adicionar guarda `if (valor <= 0) return;` ou lançar exceção.
