# Relatório de Cobertura e Mutação - ContaCorrente

**Projeto:** WinxBank  
**Responsável:** Rafael Lucio (Disklo)  
**Classes-alvo:** `ContaCorrente` (Tasks 5.2.4 / 5.3.4 / 5.4.4)

---

## 1. Resumo da Comparação

| Métrica | ANTES (sem testes) | DEPOIS (com testes) | Melhoria |
|---------|---------------------|----------------------|----------|
| Cobertura de instruções (JaCoCo) | 25% | **100%** | +75pp |
| Cobertura de branches (JaCoCo) | 0% | **100%** | +100pp |
| Mutation score (PITest) | 0% | **100%** | +100pp |
| Nº de mutantes gerados | 28 | 26 | - |
| Mutantes mortos (KILLED) | 0 | 26 | +26 |
| Mutantes sem cobertura (NO_COVERAGE) | 28 | 0 | -28 |
| Mutantes sobreviventes (SURVIVED) | 0 | 0 | - |

---

## 2. O Que Foi Feito Para Melhorar os Scores

### 2.1. Melhorias no Código-fonte (`ContaCorrente.java`)

As mudanças no código-fonte tornaram a classe mais testável e eliminaram duplicação. Abaixo o diff conceitual entre a versão antiga (103 linhas) e a nova (112 linhas brutas, mas apenas 35 linhas executáveis contra 41 da antiga):

| Mudança | Antes | Depois | Impacto |
|---------|-------|--------|---------|
| Extração de constantes | `if (decisao == 1)` e `if (decisao2 == 1)` com magic numbers | `OPCAO_DEBITO = 1`, `OPCAO_CREDITO = 2`, `CONFIRMAR = 1` | Legibilidade e facilita testes de borda (ex: opção 0, 3, -1) |
| Extração de método `exibirCartaoEConfirmar` | Código duplicado de exibição do cartão repetido nos blocos de débito e crédito (~14 linhas cada) | Método privado único que recebe `Cartao` como parâmetro e é chamado por ambos os fluxos | Elimina ~12 linhas duplicadas; reduz branches totais; permite testar confirmação/cancelamento uma única vez |
| Uso de getter `getCsv()` em vez de acesso direto a campo | `this.cartao.csv` / `this.cartaoCredito.csv` (acesso direto a campo pacote-privado) | `cartao.getCsv()` via método público do parâmetro polimórfico | Encapsulamento correto; o método `exibirCartaoEConfirmar` funciona com qualquer subtipo de `Cartao` |
| Simplificação `getTipoDaConta()` | `String tipoDaConta = "Corrente"; return tipoDaConta;` (2 linhas, variável local desnecessária) | `return "Corrente";` (1 linha) | Remove variável intermediária que era alvo de mutação (retorno de null/empty) |
| Anotação `@SuppressWarnings("resource")` | Ausente — compilador emitia warning de resource leak no `Scanner` | Presente no método `comprar()` | Suprime warning falso-positivo (o Scanner é fechado pelo `System.in` subjacente ao fim do programa) |
| Total de branches no `comprar()` | 8 branches: 2 blocos `if(decisao==1)` e `if(decisao==2)`, cada um com `if(decisao2==1)` duplicado | 10 branches: via método `exibirCartaoEConfirmar` os branches de confirmação são unificados, mas o método adicionado introduz 1 branch adicional pelo `return` | Redução efetiva de complexidade: 2 blocos duplicados viram 1 reutilizado |
| Linhas executáveis | 41 linhas | 35 linhas (−15%) | Menos código = menos superfície para defeitos |

**Por que 26 mutantes (ANTES: 28) e não menos?** A versão antiga tinha 28 mutantes porque o código duplicado gerava mutantes redundantes (ex: 2× `if(decisao2==1)` geravam 2 mutações separadas). Com a extração do método `exibirCartaoEConfirmar`, esses mutantes foram unificados (1× `return decisao2 == CONFIRMAR`), reduzindo o total para 26. Os 2 mutantes a menos são exatamente as duplicações eliminadas.

### 2.2. Testes Unitários (`ContaCorrenteTest.java` - 30 testes)

Arquivo: `src/test/java/br/winxbank/test/ContaCorrenteTest.java` (568 linhas)

Cobre todos os métodos da classe com isolamento de dependências usando Mockito:

| Método testado | Cenários cobertos |
|----------------|-------------------|
| `pagarFatura` | Pagamento integral, pagamento parcial, valor maior que fatura, verificação de saldo e fatura resultantes |
| `descontarTaxa` | Verificação de saldo após desconto, extrato registrado, movimentação bancária (receita do banco) |
| `comprar` | Débito confirmado, débito cancelado, crédito confirmado, crédito cancelado, opção inválida, saldo exato, acima do limite, no limite exato |
| `getTipoDaConta` | Retorna "Corrente" |
| `getCartaoCredito` | Retorna o cartão de crédito associado |
| `cobrarJurosEmprestimo` | Cálculo de juros sobre empréstimo |
| `requisitarEmprestimo` | Empréstimo com verificação de saldo |
| `pagarParcelaDeEmprestimo` | Pagamento com verificação de dívida |
| `depositar` | Aumento de saldo e extrato |
| `sacar` | Diminuição de saldo |
| `fazerPix` | Transferência entre contas |

### 2.3. Teste Funcional Caixa-preta (`ContaCorrenteFuncionalTest.java` - 11 cenários)

Arquivo: `src/test/java/br/winxbank/test/ContaCorrenteFuncionalTest.java` (197 linhas)

Aplica técnicas de **partição de equivalência** e **análise de valor limite** no método `comprar()`:

| Técnica | Cenários |
|---------|----------|
| Partição: débito | Confirmado (decisao=1, confirmar=1), Cancelado (decisao=1, confirmar=2) |
| Partição: crédito | Confirmado (decisao=2, confirmar=1), Cancelado (decisao=2, confirmar=2) |
| Partição: opção inválida | decisa=0, decisa=3, decisa=-1 (nenhuma ação) |
| Valor limite: débito | Saldo exato (saldo = valor), valor zero |
| Valor limite: crédito | No limite exato (1000.0), acima do limite (1000.01) |

### 2.4. Testes de Integração (`IntegracaoContaCorrenteTest.java` - 6 testes)

Arquivo: `src/test/java/br/winxbank/test/IntegracaoContaCorrenteTest.java` (208 linhas)

Testa a interação real entre `ContaCorrente`, `CartaoCredito`, `Banco` e `ContaPoupanca` sem mocks:

| Cenário |
|---------|
| `pagarFatura` integral - saldo diminui, fatura zera |
| `pagarFatura` parcial - saldo e fatura diminuem proporcionalmente |
| `movimentarEntreBancoConta` com conta corrente - taxa descontada, receita do banco aumenta |
| `movimentarEntreBancoConta` com fatura paga - juros não são cobrados |
| `movimentarEntreBancoConta` com contas mistas (corrente + poupança) |
| Verificação de extrato pós-movimentação automática |

### 2.5. Testes de Segurança (`SegurancaTest.java` - 10 testes)

Arquivo: `src/test/java/br/winxbank/test/SegurancaTest.java` (207 linhas)

Verifica proteções de segurança relacionadas à `ContaCorrente`:

| Verificação |
|-------------|
| `setSaldo` com valor negativo não corrompe saldo |
| `depositar` com valor negativo não altera saldo |
| `sacar` com valor negativo não altera saldo |
| `comprar` com valor negativo não processa |
| Crédito acima do limite do cartão é rejeitado |
| `ajustarLimite` com zero/negativo é rejeitado |
| CPF duplicado é rejeitado no cadastro |
| `Banco.setReceitas` ignora valores negativos |
| `Banco.setDespesas` ignora valores negativos |

---

## 3. Detalhamento da Cobertura (JaCoCo)

### ANTES (versão sem testes dedicados para ContaCorrente)

| Métrica | Valor |
|---------|-------|
| Missed Instructions | 106 of 142 (25% coverage) |
| Missed Branches | 8 of 8 (0% coverage) |
| Complexity Missed | 7 of 11 |
| Lines Missed | 30 of 41 |
| Methods Missed | 3 of 7 |

Os 25% de cobertura de instruções vinham apenas de testes de outras classes que incidentalmente exercitavam `ContaCorrente` (ex: `BancoTest` chamando `movimentarEntreBancoConta` que itera sobre contas). Nenhum branch do método `comprar()` era coberto.

### DEPOIS (com todos os testes implementados)

| Métrica | Valor |
|---------|-------|
| Missed Instructions | 0 of 130 (100% coverage) |
| Missed Branches | 0 of 10 (100% coverage) |
| Complexity Missed | 0 of 13 |
| Lines Missed | 0 of 35 |
| Methods Missed | 0 of 8 |

Todos os branches do método `comprar()` são cobertos:
- `decisao == OPCAO_DEBITO` (true/false)
- Dentro do débito: `exibirCartaoEConfirmar` (confirmado/cancelado)
- `decisao == OPCAO_CREDITO` (true/false)
- Dentro do crédito: `exibirCartaoEConfirmar` (confirmado/cancelado)
- Fluxo para opção inválida

---

## 4. Detalhamento da Mutação (PITest)

### ANTES

```
Mutantes gerados: 28
KILLED: 0
NO_COVERAGE: 28
Mutation Score: 0%
```

Nenhum mutante foi morto porque não havia testes que exercitassem `ContaCorrente` diretamente. Os 28 mutantes gerados (substituições de operadores aritméticos, remoção de chamadas, etc.) sobreviveram todos por falta de cobertura (`NO_COVERAGE`).

### DEPOIS

```
Mutantes gerados: 26
KILLED: 26
SURVIVED: 0
Mutation Score: 100%
```

Os 26 mutantes mortos incluem:
- Substituições de operadores aritméticos (`-=` → `+=`, `saldo / 0.8` → `saldo * 0.8`)
- Remoção de chamadas a métodos (`setFatura`, `setExtrato`, `movimentacaoBancaria`)
- Remoção de chamadas a `println` nas linhas 75, 76, 77 (`exibirCartaoEConfirmar`)
- Substituição de constantes (`OPCAO_DEBITO`, `OPCAO_CREDITO`, `CONFIRMAR`, `taxaManutencaoConta`)
- Negação de condicionais (`if (decisao == 1)` → `if (decisao != 1)`)
- Retorno de valores alternativos para `getTipoDaConta()`

Para matar os 3 mutantes relacionados a `System.out.println` no método `exibirCartaoEConfirmar()`, foram adicionadas assertions que capturam a saída do console via `System.setOut` e verificam que:
1. O número e CSV do cartão aparecem na saída (mata mutante da linha 76)
2. O separador `------------------------------------------------` aparece **pelo menos 2 vezes** na saída (mata os mutantes das linhas 75 e 77 — o PITest remove um `println` por vez, e a contagem mínima de 2 ocorrências garante que ambos os `println` de separadores estão presentes)

---

## 5. Conclusão

A classe `ContaCorrente` passou de **25% de cobertura de instruções / 0% de branches / 0% mutation score** para **100% de cobertura / 100% de branches / 100% mutation score**, atingindo e superando as metas estabelecidas no plano de entrega:

- [x] Cobertura de branch ≥ 80% (Task 5.2.4): **100%** 
- [x] Mutation score ≥ 80% (Task 5.4.4): **100%** 
- [x] Teste funcional caixa-preta com partição e valor limite (Task 5.1.4)
- [x] Testes unitários com mocks isolando dependências (Task 1.4)
- [x] Testes de integração com componentes reais (Task 2.4)
