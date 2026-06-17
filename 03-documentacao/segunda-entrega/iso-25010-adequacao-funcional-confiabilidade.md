# ISO 25010 - Adequacao Funcional e Confiabilidade

**Projeto:** WinxBank  
**Responsavel:** Fernando Rene  
**Tarefa:** 3.1

---

## 1. Adequacao Funcional

A adequacao funcional avalia o grau em que o produto de software fornece funcoes que atendem as necessidades declaradas e implicitas quando utilizado sob condicoes especificadas.

| Subcaracteristica | Medida | Escala (1-5) | Valor | Justificativa |
|-|-|-|-|-|
| **Completude Funcional** | Proporcao de funcoes implementadas em relacao as especificadas | 1 = Menos de 20% das funcoes implementadas; 5 = 100% das funcoes implementadas | **4** | O sistema implementa as funcionalidades bancarias essenciais: cadastro de clientes, abertura de contas (corrente e poupanca), deposito, saque, PIX, compras (debito/credito), fatura de cartao de credito, emprestimos, passagem de meses com movimentacoes automaticas (taxa de manutencao, rendimento poupanca, juros de fatura) e sistema de pontos ClienteWinx. Funcionalidades como transferencia entre contas proprias e relatorios detalhados nao estao implementadas. |
| **Correcao Funcional** | Proporcao de resultados corretos em relacao ao total de operacoes executadas | 1 = Menos de 50% de resultados corretos; 5 = 100% dos resultados corretos | **4** | A maioria das operacoes retorna resultados corretos (deposito, saque, PIX, taxa de manutencao). Na revisao da segunda entrega, foram corrigidos problemas em `RegistroDeClientes`: `retornarCliente` passou a retornar `null` corretamente quando o CPF nao existe, e `atualizarCliente`/`removerCliente` passaram a interromper a iteracao apos alterar a lista. Ainda existem riscos em outros pontos, como `printarBanco` alterar despesas como efeito colateral e o calculo de rendimento da poupanca (`saldo / 0.8`) produzir valores acima do esperado. |
| **Adequacao Funcional** | Grau em que as funcoes facilitam a realizacao de tarefas e objetivos do usuario | 1 = Funcoes dificultam o uso; 5 = Funcoes facilitam plenamente o uso | **4** | O sistema oferece uma interface grafica Swing (GUI) com paineis dedicados para login e dashboard, alem da interface de console original. O WinxBankController encapsula toda a logica de negocio de forma acessivel pela GUI, com feedback ao usuario via ActionResult. A persistencia automatica em JSON garante que os dados nao se percam entre sessoes. |

---

## 2. Confiabilidade

A confiabilidade avalia o grau em que um sistema executa funcoes especificadas sob condicoes especificadas por um periodo de tempo especificado.

| Subcaracteristica | Medida | Escala (1-5) | Valor | Justificativa |
|-|-|-|-|-|
| **Maturidade** | Frequencia de falhas do sistema durante operacao normal | 1 = Falhas frequentes; 5 = Nenhuma falha observada | **3** | O sistema funciona de forma estavel para os fluxos principais (cadastro, login, deposito, saque). No entanto, entradas invalidas podem causar excecoes nao tratadas: `Scanner.nextInt()` lanca `InputMismatchException` se o usuario digitar texto onde se espera numero; `abrirNovaConta()` retorna `null` para opcoes invalidas, o que pode causar `NullPointerException` em `cadastrarCliente()` se o retorno nao for verificado. A GUI mitiga parte desses problemas com formularios tipados. |
| **Disponibilidade** | Proporcao de tempo em que o sistema esta operacional e acessivel | 1 = Sistema frequentemente indisponivel; 5 = Sistema sempre disponivel | **4** | Por ser uma aplicacao desktop (Swing/console), o sistema esta disponivel sempre que executado localmente. Nao depende de servicos externos ou conexao de rede para suas operacoes bancarias. A unica dependencia de I/O e a persistencia em arquivos JSON locais, que falha silenciosamente (blocos catch vazios no controller) sem derrubar a aplicacao. |
| **Tolerancia a Falhas** | Capacidade de manter operacao aceitavel apesar de falhas de hardware ou software | 1 = Qualquer falha derruba o sistema; 5 = Sistema continua operando normalmente apos falhas | **2** | O sistema tem tratamento limitado de falhas. Excecoes customizadas existem (`BankAccountNotFoundException`, `YouAreNotLoggedInException`, etc.) e sao usadas no controller, mas o codigo de dominio (Main.java, RegistroDeClientes) nao as trata adequadamente. Nao ha validacao de saldo suficiente antes de saques/PIX no dominio (apenas no controller GUI). O `ConsoleBridge` pode mascarar erros ao redirecionar streams. Singletons mutaveis nao sao thread-safe. |
| **Recuperabilidade** | Capacidade de recuperar dados e restabelecer o estado desejado apos interrupcao ou falha | 1 = Dados perdidos apos qualquer falha; 5 = Recuperacao completa e automatica | **3** | O sistema persiste dados em arquivos JSON (`clientes.json`, `mesAtual.txt`, arquivo do banco) ao final de cada operacao no controller GUI. Em caso de encerramento abrupto, os dados da ultima operacao bem-sucedida sao recuperados no proximo inicio (`bootstrap()`). Porem, nao ha mecanismo de transacao atomica: uma falha durante a gravacao pode corromper o arquivo JSON. Nao ha backup automatico nem log de auditoria para rastrear operacoes perdidas. |

---

## Resumo

| Caracteristica | Media |
|-|-|
| Adequacao Funcional | 4.00 |
| Confiabilidade | 3.00 |
| **Media Geral** | **3.50** |

### Principais Pontos de Melhoria

1. **Correcao funcional**: Corrigir o efeito colateral de `printarBanco` e revisar o calculo de rendimento da poupanca.
2. **Tolerancia a falhas**: Adicionar validacao de entrada no dominio (nao apenas no controller), tratar excecoes de Scanner, e verificar retornos nulos de `abrirNovaConta`.
3. **Recuperabilidade**: Implementar gravacao atomica (escrever em arquivo temporario e renomear) e adicionar backup antes de sobrescrever.
4. **Maturidade**: Aumentar cobertura de testes para identificar e corrigir defeitos latentes.
