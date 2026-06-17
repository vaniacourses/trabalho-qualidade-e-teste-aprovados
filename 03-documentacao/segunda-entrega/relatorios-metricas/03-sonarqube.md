# Relatorio SonarQube - Segunda Entrega

## Escopo

- Ferramenta: `SonarQube`
- Objetivo do plano: secao `6. Relatorio de Inspecao com SonarQube`
- Este relatorio cobre as cinco tarefas de correcao por classe definidas no plano.
- Baseline usada: projeto SonarQube `winxbank-primeira-entrega`
- Medicao atual usada: projeto SonarQube `winxbank`

## Score por tarefa do plano

| Fase | Tarefa do plano | Responsavel | Antes | Depois | Status |
|---|---|---|---|---|---|
| Correcao | Corrigir issues de `RegistroDeClientes` | Fernando Rene | Coverage: `7,2%`; Line: `9,2%`; Branch: `3,8%`; Smells: `21`; Hotspots: `0`; Duplicacao: `0,0%`; Complexity: `38`; Cognitive: `40`; Bugs: `0`; Vulnerabilities: `0`; Maintainability: `B`; Reliability: `A`; Security: `A` | Coverage: `97,6%`; Line: `100%`; Branch: `93,2%`; Smells: `22`; Hotspots: `0`; Duplicacao: `0,0%`; Complexity: `34`; Cognitive: `34`; Bugs: `0`; Vulnerabilities: `0`; Maintainability: `B`; Reliability: `A`; Security: `A` | Concluida |
| Correcao | Corrigir issues de `Banco` | Joao Omar | Coverage: `73,2%`; Line: `69,0%`; Branch: `84,6%`; Smells: `14`; Hotspots: `1`; Duplicacao: `0,0%`; Complexity: `23`; Cognitive: `23`; Bugs: `1`; Vulnerabilities: `0`; Maintainability: `A`; Reliability: `D`; Security: `A` | Coverage: `99,0%`; Line: `100%`; Branch: `95,8%`; Smells: `13`; Hotspots: `1`; Duplicacao: `0,0%`; Complexity: `24`; Cognitive: `14`; Bugs: `0`; Vulnerabilities: `0`; Maintainability: `A`; Reliability: `A`; Security: `A` | Concluida |
| Correcao | Corrigir issues de `CartaoCredito` | Caio | Coverage: `97,5%`; Line: `100%`; Branch: `87,5%`; Smells: `4`; Hotspots: `0`; Duplicacao: `0,0%`; Complexity: `12`; Cognitive: `5`; Bugs: `0`; Vulnerabilities: `0`; Maintainability: `A`; Reliability: `A`; Security: `A` | Coverage: `90,9%`; Line: `88,9%`; Branch: `100%`; Smells: `0`; Hotspots: `0`; Duplicacao: `0,0%`; Complexity: `20`; Cognitive: `6`; Bugs: `0`; Vulnerabilities: `0`; Maintainability: `A`; Reliability: `A`; Security: `A` | Concluida |
| Correcao | Corrigir issues de `ContaCorrente` | Rafael | Coverage: `22,4%`; Line: `26,8%`; Branch: `0,0%`; Smells: `15`; Hotspots: `0`; Duplicacao: `10,6%`; Complexity: `11`; Cognitive: `8`; Bugs: `0`; Vulnerabilities: `0`; Maintainability: `B`; Reliability: `A`; Security: `A` | Coverage: `100%`; Line: `100%`; Branch: `100%`; Smells: `10`; Hotspots: `0`; Duplicacao: `0,0%`; Complexity: `12`; Cognitive: `8`; Bugs: `0`; Vulnerabilities: `0`; Maintainability: `A`; Reliability: `A`; Security: `A` | Concluida |
| Correcao | Corrigir issues de `Ano` | Joao Victor | Coverage: `94,3%`; Line: `95,7%`; Branch: `91,7%`; Smells: `2`; Hotspots: `0`; Duplicacao: `0,0%`; Complexity: `11`; Cognitive: `10`; Bugs: `0`; Vulnerabilities: `0`; Maintainability: `A`; Reliability: `A`; Security: `A` | Coverage: `94,3%`; Line: `95,7%`; Branch: `91,7%`; Smells: `2`; Hotspots: `0`; Duplicacao: `0,0%`; Complexity: `11`; Cognitive: `10`; Bugs: `0`; Vulnerabilities: `0`; Maintainability: `A`; Reliability: `A`; Security: `A` | Sem variacao mensuravel |

## Comparativo completo por classe

| Classe | Coverage anterior | Coverage atual | Branch anterior | Branch atual | Smells anterior | Smells atual | Hotspots anterior | Hotspots atual | Duplicacao anterior | Duplicacao atual | Bugs anterior | Bugs atual | Reliability anterior | Reliability atual | Maintainability anterior | Maintainability atual |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---|---|---|---|
| `RegistroDeClientes` | 7,2% | 97,6% | 3,8% | 93,2% | 21 | 22 | 0 | 0 | 0,0% | 0,0% | 0 | 0 | A | A | B | B |
| `Banco` | 73,2% | 99,0% | 84,6% | 95,8% | 14 | 13 | 1 | 1 | 0,0% | 0,0% | 1 | 0 | D | A | A | A |
| `CartaoCredito` | 97,5% | 90,9% | 87,5% | 100% | 4 | 0 | 0 | 0 | 0,0% | 0,0% | 0 | 0 | A | A | A | A |
| `ContaCorrente` | 22,4% | 100% | 0,0% | 100% | 15 | 10 | 0 | 0 | 10,6% | 0,0% | 0 | 0 | A | A | B | A |
| `Ano` | 94,3% | 94,3% | 91,7% | 91,7% | 2 | 2 | 0 | 0 | 0,0% | 0,0% | 0 | 0 | A | A | A | A |

## Acoes por classe

| Classe | Acoes executadas |
|---|---|
| `RegistroDeClientes` | Criacao de cobertura real; correcao de `retornarCliente`; interrupcao correta de iteracao em `atualizarCliente` e `removerCliente`; ajuste de testabilidade em `Banco.abrirNovaConta(Scanner)` |
| `Banco` | Ampliacao de testes sobre movimentacao banco-conta, abertura de conta, receitas/despesas e cenarios de integracao |
| `CartaoCredito` | Ampliacao de testes sobre limite, fatura, credito, juros e integracao com conta/banco |
| `ContaCorrente` | Ampliacao de testes sobre compra debito/credito, confirmacao/cancelamento, taxa, fatura e pix |
| `Ano` | Manutencao da cobertura ja acima da meta; testes de passagem de mes e wrap-around |

## Ajuste de instrumentacao

- O `pom.xml` foi ajustado para o Sonar importar o `jacoco.xml` corretamente.
- O `maven-surefire-plugin` passou a usar `@{argLine}`.

## Leitura do resultado

- O ganho mensuravel ficou concentrado em cobertura e complexidade.
- Nao houve regressao em bugs ou vulnerabilidades.
- `Code smells` nao reduziram porque permaneceram pendencias de manutencao:
  - `System.out.println`
  - literais duplicados
  - uso de `ArrayList` em vez de interface

## Conclusao

- `RegistroDeClientes`
  - `coverage`: `7,2% -> 97,6%`
  - `branch coverage`: `3,8% -> 93,2%`
- `Banco`
  - removeu o unico bug aberto
  - `reliability rating`: `D -> A`
  - `coverage`: `73,2% -> 99,0%`
- `CartaoCredito`
  - zerou `code smells`: `4 -> 0`
  - `branch coverage`: `87,5% -> 100%`
- `ContaCorrente`
  - `coverage`: `22,4% -> 100%`
  - `branch coverage`: `0,0% -> 100%`
  - `maintainability`: `B -> A`
- `Ano`
  - sem variacao mensuravel entre baseline e versao atual
