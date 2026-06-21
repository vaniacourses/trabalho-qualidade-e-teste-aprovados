# Plano de Entrega 2 - Grupo Aprovados - WinxBank

**Disciplina:** Qualidade e Teste de Software  
**Peso:** 5

\---

## Integrantes do Grupo

|#|Nome|GitHub|
|-|-|-|
|1|Fernando Rene|fernandorcafilho|
|2|Joao Omar|joaomar1|
|3|Caio Marcio / Caio Silva|cakocaito|
|4|Rafael Lucio|Disklo|
|5|Joao Victor Amaral|JvamleiteUff|

\---

## Tarefas por Entregavel

### 1\. Melhorar e Aumentar Testes Unitarios (isolando dependencias)

**Objetivo:** Refatorar os testes existentes para usar Mockito corretamente, isolando dependencias externas (Singletons, Scanner, System.in).

|Tarefa|Responsavel|Descricao|
|-|-|-|
|1.1|Fernando Rene|Criar testes unitarios para `RegistroDeClientes`: mockando `Banco.getInstancia()` e isolando chamadas de Scanner. Testar `cadastrarCliente`, `checarCpf`, `retornarCliente`, `atualizarCliente`, `removerCliente`|
|1.2|<br />Caio|Refatorar `BancoTest` para usar mocks nos singletons (`RegistroDeClientes`, `Ano`). Adicionar testes para `abrirNovaConta` tipo 1 e tipo 2 com input simulado|
|1.3|Joao Omar|Ampliar `CartaoCreditoTest`: adicionar testes para `setFatura` com valor que zera fatura (faturaPaga=true), `cobrarJuros` quando indexMes e igual ao atual, `creditar` verificando atribuicao de mes|
|1.4|Rafael|Criar testes unitarios para `ContaCorrente`: `pagarFatura`, `descontarTaxa` (verificar saldo e extrato), `comprar` opcao debito e credito, `getTipoDaConta`|
|1.5|Joao Victor|Ampliar `AnoTest` cobrindo cenários válidos e inválidos de `setMesAtual`, wrap-around Dezembro→Janeiro, overflow do contador, retorno de `fazerMesPassar`, ramos condicionais de count múltiplo e não múltiplo de 5 e integração com Banco.movimentarEntreBancoConta.|

\---

### 2\. Testes de Integracao

**Objetivo:** Testar a interacao real entre componentes do sistema sem mocks.

|Tarefa|Responsavel|Descricao|
|-|-|-|
|2.1|Fernando Rene|Integracao `RegistroDeClientes` + `Banco` + `ContaCorrente`: cadastro de cliente -> abertura de conta corrente -> verificar que cliente e suas contas estao corretamente registrados|
|2.2|Caio|Integracao `Banco.movimentarEntreBancoConta` + `ContaCorrente` + `ContaPoupanca`: verificar que taxa de manutencao e descontada da corrente, rendimento e acrescentado na poupanca, e receitas/despesas do banco sao atualizadas|
|2.3|Joao Omar|Integracao `CartaoCredito` + `ContaCorrente` + `Banco`: creditar no cartao -> cobrar juros -> verificar que receita do banco aumenta corretamente|
|2.4|Rafael|Integracao `ContaCorrente.pagarFatura` + `CartaoCredito`: pagar fatura e verificar que saldo da conta diminui e fatura do cartao diminui proporcionalmente|
|2.5|Joao Victor|Validar a integração entre `Ano`, `Banco` e `ContaCorrente` garantindo que a passagem de mês execute corretamente as movimentações automáticas do sistema bancário.|

\---

### 3\. ISO 25010 - Medidas de Atributos de Qualidade

**Objetivo:** Indicar medidas para cada atributo de qualidade da ISO 25010 seguindo uma escala, com justificativa.

|Tarefa|Responsavel|Atributos a avaliar|
|-|-|-|
|3.1|Fernando Rene|**Adequacao Funcional** (completude, correcao, adequacao) + **Confiabilidade** (maturidade, disponibilidade, tolerancia a falhas, recuperabilidade)|
|3.2|Caio|**Eficiencia de Desempenho** (tempo de resposta, utilizacao de recursos, capacidade) + **Compatibilidade** (coexistencia, interoperabilidade)|
|3.3|Joao Omar|**Usabilidade** (reconhecibilidade, aprendizagem, operabilidade, protecao contra erros, estetica, acessibilidade)|
|3.4|Rafael|**Seguranca** (confidencialidade, integridade, nao-repudio, responsabilizacao, autenticidade)|
|3.5|Joao Victor|**Manutenibilidade** (modularidade, reusabilidade, analisabilidade, modificabilidade, testabilidade) + **Portabilidade** (adaptabilidade, instalabilidade, substituibilidade)|

**Entregavel:** Documento com tabela contendo: subcaracteristica, medida, escala (1-5), valor atribuido, justificativa.

\---

### 4.Testes de Sistema

\---

### 4.1 Testes de Sistema (Requisitos Funcionais)

**Objetivo:** Validar fluxos completos do sistema do ponto de vista do usuario.

|Tarefa|Responsavel|Fluxo testado|
|-|-|-|
|4.1.1|Fernando Rene|**Fluxo de cadastro e login:** criar usuario -> logar -> verificar dados do cliente logado -> apagar usuario -> verificar remocao|
|4.1.2|Caio|**Fluxo de operacoes bancarias basicas:** logar -> depositar -> sacar -> verificar saldo e extrato|
|4.1.3|Joao Omar|**Fluxo de cartao de credito:** logar -> comprar no credito -> verificar fatura -> pagar fatura -> verificar saldo|
|4.1.4|Rafael|**Fluxo de Pix:** logar usuario A -> logar usuario B -> fazer pix de A para B -> verificar saldos de ambos|
|4.1.5|Joao Victor|**Fluxo ClienteWinx:** criar usuario com saldo >= 100k -> verificar promocao a ClienteWinx -> comprar -> verificar pontos -> converter pontos em saldo|

\---

### 4.2. Testes E2E com Selenium (minimo 1 por integrante)

**Observacao:** O WinxBank e uma aplicacao de console (CLI). Para utilizar Selenium, sera necessario criar uma interface web minima (ex: com Spring Boot + Thymeleaf) OU adaptar os testes E2E para usar outra abordagem equivalente aceita pelo professor (ex: testes E2E de terminal com ProcessBuilder simulando interacao do usuario).

**Alternativa recomendada:** Caso o professor aceite, usar `ProcessBuilder` para executar o JAR e simular inputs/outputs do terminal como testes E2E.

|Tarefa|Responsavel|Cenario E2E|
|-|-|-|
|4.2.1|Fernando Rene|E2E: Cadastro completo de usuario com conta corrente via interface|
|4.2.2|Caio|E2E: Deposito e saque com verificacao de saldo via interface|
|4.2.3|Joao Omar|E2E: Compra com cartao de credito e pagamento de fatura via interface|
|4.2.4|Rafael|E2E: Transferencia Pix entre dois clientes via interface|
|4.2.5|Joao Victor|E2E: Conversao de pontos ClienteWinx em saldo via interface|

**Tarefa compartilhada:** Se Selenium for o caminho, criar a camada web sera responsabilidade dividida:

* Fernando Rene + Joao Omar: setup do Spring Boot e endpoints REST
* Caio + Rafael + Joao Victor: paginas HTML e integracao dos formularios

\---

### 4.3. Teste de Requisito Nao Funcional (opcional mas recomendado)

|Tarefa|Responsavel|Descricao|
|-|-|-|
|4.3.1|Joao Omar|**Desempenho:** medir tempo de resposta de `movimentarEntreBancoConta` com 100, 1000 e 10000 clientes. Usar JMH ou `System.nanoTime()`. Definir baseline aceitavel|
|4.3.2|Rafael|**Seguranca:** verificar que valores negativos nao corrompem saldos, que limites do cartao nao podem ser ultrapassados, que CPF duplicado e rejeitado|

\---

### 5\. Projetar e Melhorar Conjunto de Casos de Teste

\---

### 5.1. Teste Funcional (Tecnica Caixa-Preta)

**Objetivo:** Projetar casos de teste usando particao de equivalencia e analise de valor limite.

|Tarefa|Responsavel|Classe/Metodo|Tecnicas|
|-|-|-|-|
|5.1.1|Fernando Rene|`RegistroDeClientes.cadastrarCliente`|Particao de equivalencia (CPF valido/duplicado/vazio), valor limite (saldo = 99999.99, 100000, 100000.01)|
|5.1.2|Caio|`Banco.movimentarEntreBancoConta`|Particao (lista vazia, so corrente, so poupanca, mista), valor limite (saldo = 0, fatura = 0)|
|6.1.3|Joao Omar|`CartaoCredito.setFatura / creditar`|Particao (dentro do limite, igual ao limite, acima do limite), valor limite (limite exato, limite+0.01)|
|5.1.4|Rafael|`ContaCorrente.comprar`|Particao (debito, credito, opcao invalida), valor limite (saldo exato, saldo+0.01)|
|5.1.5|Joao Victor|`Ano.fazerMesPassar`|Particao (count nao multiplo de 5, multiplo de 5, count = Long.MAX\_VALUE), valor limite (indexMesAtual = 10, 11, 0)|

\---

### 5.2. Teste Estrutural (>=80% cobertura todas-arestas)

**Ferramenta:** JaCoCo (plugin Maven)

**Objetivo:** Atingir no minimo 80% de cobertura de branch (todas-arestas) na classe-alvo de cada membro.

|Tarefa|Responsavel|Classe-alvo|Foco|
|-|-|-|-|
|5.2.1|Fernando Rene|`RegistroDeClientes`|Cobrir todos os branches de `cadastrarCliente`, `checarCpf`, `retornarCliente`, `visualizarDetalhesDoCliente`|
|5.2.2|Caio|`Banco`|Cobrir branches de `movimentarEntreBancoConta` (ContaPoupanca vs ContaCorrente, fatura > 0), `setReceitas`/`setDespesas` (positivo/negativo), `printarBanco`|
|5.2.3|Joao Omar|`CartaoCredito`|Cobrir branches de `setFatura` (dentro/acima limite, fatura <= 0), `cobrarJuros` (faturaPaga + indexMes), `creditar`|
|5.2.4|Rafael|`ContaCorrente`|Cobrir branches de `comprar` (debito decisao1=1/outro, credito decisao1=2/outro, decisao2=1/outro)|
|5.2.5|Joao Victor|`Ano`|Cobrir branches de `fazerMesPassar` (count%5==0, indexMes==11, count==MAX\_VALUE), `setMesAtual` (mes encontrado/nao encontrado)|

**Tarefa compartilhada (setup):**

* Joao Omar: configurar plugin JaCoCo no `pom.xml` e gerar relatorio de cobertura

\---

## 5.3. Classes-alvo para Teste Estrutural e Mutacao

Cada membro deve ter **pelo menos uma classe nao-CRUD com alta complexidade** para teste estrutural (>=80% cobertura todas-arestas) e teste de mutacao (>=80% escore de mutacao). As mesmas classes serao usadas para correcao do SonarQube.

|Tarefa|Membro|Classe-alvo|Justificativa|
|-|-|-|-|
|5.3.1|Fernando Rene|`RegistroDeClientes`|Alta complexidade ciclomatica: cadastro com promocao a ClienteWinx, checagem de CPF, atualizacao/remocao com iteracao e condicoes, visualizacao com type-checking|
|5.3.2|Caio|`Banco`|Logica de movimentacao entre banco e contas (iteracao aninhada com type-checking), abertura de conta com Scanner, singleton, printarBanco com logica condicional|
|5.3.3|Joao Omar|`CartaoCredito`|Logica de fatura com limite, cobranca de juros condicional (fatura paga + index de mes), movimentacao bancaria, creditar com validacao|
|5.3.4|Rafael|`ContaCorrente`|Metodo `comprar()` com fluxo de decisao debito/credito, confirmacao de cartao, pagamento de fatura, desconto de taxa com movimentacao|
|5.3.5|Joao Victor|`Ano`|Logica de passagem de meses com contadores, wrap-around (mes 11 -> 0), overflow de long, integracao com Banco.movimentarEntreBancoConta, setMesAtual com busca em array|

\---

### 5.4. Teste Baseado em Defeitos (>=80% escore de mutacao)

**Ferramenta:** PITest (plugin Maven)

**Objetivo:** Atingir no minimo 80% de mutation score nas mesmas classes do teste estrutural.

|Tarefa|Responsavel|Classe-alvo|
|-|-|-|
|5.4.1|Fernando Rene|`RegistroDeClientes`|
|5.4.2||`Banco`|
|5.4.3|Joao Omar<br />Caio|`CartaoCredito`|
|5.4.4|Rafael|`ContaCorrente`|
|5.4.5|Joao Victor|`Ano`|

**Tarefa compartilhada (setup):**

* Caio: configurar plugin PITest no `pom.xml` e gerar relatorio de mutacao

\---

### 6\. Relatorio de Inspecao com SonarQube

**Objetivo:** Executar SonarQube, coletar prints, e corrigir problemas.

|Fase|Tarefa|Responsavel|
|-|-|-|
|Setup|Instalar e configurar SonarQube localmente|Joao Omar|
|Setup|Configurar `sonar-maven-plugin` no `pom.xml`|Joao Omar|
|Execucao|Rodar analise e tirar print ANTES das correcoes|Todos|
|Correcao|Corrigir issues de `RegistroDeClientes`|Fernando Rene|
|Correcao|Corrigir issues de `Banco`|Joao Omar|
|Correcao|Corrigir issues de `CartaoCredito`|Caio|
|Correcao|Corrigir issues de `ContaCorrente`|Rafael|
|Correcao|Corrigir issues de `Ano`|Joao Victor|
|Evidencia|Rodar analise novamente e tirar print DEPOIS das correcoes|Todos|

\---

## Configuracoes Necessarias no pom.xml

Plugins a adicionar para suportar a Entrega 2:

1. **JaCoCo** - cobertura de codigo (branch coverage)
2. **PITest** - teste de mutacao
3. **SonarQube Maven Plugin** - inspecao de codigo
4. **Selenium WebDriver** (ou alternativa) - testes E2E

\---

## Resumo de Entregas por Membro

### Fernando Rene

* \[  ] Testes unitarios de `RegistroDeClientes` com mocks (1.1)
* \[  ] Teste de integracao: cadastro + conta (2.1)
* \[  ] Teste de sistema: fluxo cadastro/login (3.1)
* \[  ] Teste E2E com Selenium (4.1)
* \[  ] Teste funcional caixa-preta: `cadastrarCliente` (6.1)
* \[  ] Teste estrutural >=80% branch: `RegistroDeClientes` (7.1)
* \[  ] Teste mutacao >=80%: `RegistroDeClientes` (8.1)
* \[  ] ISO 25010: Adequacao Funcional + Confiabilidade (9)
* \[  ] Correcao SonarQube: `RegistroDeClientes` (10)

### Caio

* \[  ] Refatorar `BancoTest` com mocks (1.2)
* \[  ] Teste de integracao: movimentacao banco-conta (2.2)
* \[  ] Teste de sistema: depositar/sacar (3.2)
* \[  ] Teste E2E com Selenium (4.2)
* \[  ] Teste funcional caixa-preta: `movimentarEntreBancoConta` (6.2)
* \[  ] Teste estrutural >=80% branch: `Banco` (7.2)
* \[  ] Teste mutacao >=80%: `Banco` (8.2)
* \[  ] ISO 25010: Eficiencia de Desempenho + Compatibilidade (9)
* \[  ] Setup JaCoCo + SonarQube (10)
* \[  ] Correcao SonarQube: `Banco` (10)
* \[  ] Teste nao funcional: desempenho (5.1)

### Joao Omar

* \[  ]
* \[  ]
* \[  ]
* \[  ]
* \[  ]
* \[  ]
* \[  ]
* \[  ]
* \[  ]
* \[  ]

### Rafael Lucio

* \[  ] Testes unitarios de `ContaCorrente` (1.4)
* \[  ] Teste de integracao: pagarFatura + cartao (2.4)
* \[  ] Teste de sistema: fluxo de Pix (3.4)
* \[  ] Teste E2E com Selenium (4.4)
* \[  ] Teste funcional caixa-preta: `comprar` (6.4)
* \[  ] Teste estrutural >=80% branch: `ContaCorrente` (7.4)
* \[  ] Teste mutacao >=80%: `ContaCorrente` (8.4)
* \[  ] ISO 25010: Seguranca (9)
* \[  ] Correcao SonarQube: `ContaCorrente` (10)
* \[  ] Teste nao funcional: seguranca (5.2)

### Joao Victor Amaral

* \[X] Ampliar `AnoTest` (1.5)
* \[x] Teste de integracao: passagem de meses + movimentacoes (2.5)
* \[x] Teste de sistema: fluxo ClienteWinx (3.5)
* \[x] Teste E2E com ProcessBuilder (4.5)
* \[x] Teste estrutural >=80% branch: `Ano` (7.5)
* \[x] Teste mutacao >=80%: `Ano` (8.5)
* \[x] Correcao SonarQube: `Ano` (10)
