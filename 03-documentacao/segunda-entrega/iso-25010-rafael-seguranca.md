# ISO 25010 - Segurança

**Projeto:** WinxBank  
**Responsável:** Rafael Lucio (Disklo)  
**Tarefa:** 3.4

---

## Segurança

A segurança avalia o grau em que o sistema protege informações e dados, garantindo que pessoas ou sistemas tenham o nível de acesso adequado aos dados.

> **Enfoque:** as medidas abaixo indicam **como o sistema deveria ser** — ou seja, são **requisitos/metas de qualidade** que o WinxBank deveria atingir, e **não** uma medição do que já está implementado. Para cada subcaracterística há: a **medida**, uma **escala (1-5)**, o **valor-alvo desejado** e a **justificativa** dessa meta para o contexto do sistema (aplicação bancária).

## Escala (genérica)

| Valor | Significado |
|------|-------------|
| 1 | Muito abaixo do desejável |
| 2 | Abaixo do desejável |
| 3 | Aceitável (mínimo desejável) |
| 4 | Bom |
| 5 | Excelente (ideal) |

---

## 1. Confidencialidade

| Subcaracterística | Medida | Escala (1-5) | Valor | Justificativa |
|-|-|-|-|-|
| **Confidencialidade** | Grau em que o sistema assegura que dados são acessíveis apenas por usuários autorizados | 1 = Dados expostos a qualquer usuário; 3 = Dados protegidos por autenticação básica; **5 = Dados protegidos com autenticação robusta, sessão isolada e senhas nunca expostas** | **3** | O sistema **deveria** garantir que apenas o cliente autenticado acesse seus próprios dados bancários. Atualmente o WinxBank utiliza um mecanismo de login por CPF com verificação no `RegistroDeClientes`, isolando os dados de cada cliente (`ClienteLogado` como singleton). No entanto, não há criptografia de senhas (armazenadas em texto plano no JSON), qualquer instância da aplicação pode ler o arquivo `clientes.json`, e o extrato (`Movimentacao`) fica em memória sem proteção. Para um sistema bancário real, seria desejável que as senhas fossem armazenadas com hash (ex: bcrypt) e os arquivos de persistência fossem criptografados. Para fins acadêmicos (protótipo), o valor 3 (aceitável) é o mínimo esperado — autenticação por senha funciona, mas sem criptografia. |

---

## 2. Integridade

| Subcaracterística | Medida | Escala (1-5) | Valor | Justificativa |
|-|-|-|-|-|
| **Integridade** | Grau em que o sistema previne acesso ou modificação não autorizada de dados | 1 = Dados podem ser corrompidos por qualquer operação; 3 = Validações básicas previnem corrupção acidental; **5 = Validações completas com transações atômicas e detecção de adulteração** | **3** | O sistema **deveria** impedir que valores inválidos (negativos, nulos, inconsistentes) corrompam saldos e registros. O `SegurancaTest` verifica que valores negativos não corrompem saldos (`setSaldo`, `depositar`, `sacar`, `comprar`), que limites do cartão não podem ser ultrapassados, e que `Banco.setReceitas`/`setDespesas` ignoram valores negativos. No entanto, não há mecanismo de transação atômica (uma falha durante operação pode deixar dados inconsistentes), os arquivos JSON podem ser adulterados externamente sem detecção, e CPFs duplicados são rejeitados apenas no `cadastrarCliente` (mas o JSON pode ser editado manualmente). Para um protótipo acadêmico, o valor 3 é aceitável — validações básicas existem mas não há proteção contra adulteração externa. |

---

## 3. Não-repúdio

| Subcaracterística | Medida | Escala (1-5) | Valor | Justificativa |
|-|-|-|-|-|
| **Não-repúdio** | Grau em que ações ou eventos podem ser provados como tendo ocorrido, de modo que não possam ser repudiados posteriormente | 1 = Nenhuma trilha de auditoria; 3 = Registro básico de operações (extrato por conta); **5 = Log de auditoria imutável com identificação do usuário e timestamp de cada operação** | **2** | O sistema **deveria** manter registros que permitam comprovar que uma operação foi realizada por determinado cliente. Atualmente o WinxBank registra operações no extrato (`Movimentacao`) de cada conta com valor e tipo (ENTRADA/SAÍDA), mas não armazena timestamp, não registra qual cliente realizou a operação (apenas afeta a conta), e o extrato é volátil (em memória, perdido ao reiniciar se não houver persistência anterior). Além disso, não há log de operações administrativas (ex: quem criou/apagou um cliente). Para um sistema bancário, seria desejável que cada movimentação tivesse registro de data/hora, cliente de origem/destino, e que esses registros fossem imutáveis. O valor 2 reflete que existe um esboço (extrato) mas muito aquém do desejável. |

---

## 4. Responsabilização (Accountability)

| Subcaracterística | Medida | Escala (1-5) | Valor | Justificativa |
|-|-|-|-|-|
| **Responsabilização** | Grau em que as ações de uma entidade podem ser rastreadas de forma única até a entidade | 1 = Nenhum rastreamento de ações; 3 = Ações rastreáveis ao cliente logado na sessão atual; **5 = Histórico completo e permanente de ações de cada usuário com identificação única** | **2** | O sistema **deveria** permitir rastrear cada operação ao cliente que a executou. Atualmente, o `ClienteLogado` (singleton) mantém referência ao cliente da sessão, permitindo associar operações ao usuário logado durante a execução. Porém, após logout ou reinício, essa associação é perdida. Operações como `fazerPix`, `comprar`, `depositar` e `sacar` não registram qual cliente as executou no extrato da conta de destino — apenas o valor e tipo. Além disso, não há um log centralizado que permita auditoria de todas as operações do sistema. O valor 2 indica que há rastreamento apenas em sessão (frágil) e sem persistência de auditoria. |

---

## 5. Autenticidade

| Subcaracterística | Medida | Escala (1-5) | Valor | Justificativa |
|-|-|-|-|-|
| **Autenticidade** | Grau em que a identidade de um sujeito ou recurso pode ser provada como sendo a reivindicada | 1 = Sem verificação de identidade; 3 = Verificação por senha sem políticas de segurança; **5 = Autenticação multifator com políticas de senha forte** | **3** | O sistema **deveria** verificar de forma confiável a identidade do usuário. O WinxBank implementa autenticação por CPF + senha no `RegistroDeClientes.login()`. O CPF é validado (11 dígitos, formato `xxx.xxx.xxx-xx`), e a senha é comparada com a armazenada. No entanto: senhas não têm política de complexidade (tamanho mínimo, caracteres especiais), não há limite de tentativas de login (um atacante pode fazer força bruta), e as senhas são armazenadas em texto plano no JSON. Para um sistema real, seria desejável: hash de senha com salt, política de senha forte, bloqueio após N tentativas, e autenticação de dois fatores. Para fins acadêmicos, o valor 3 é aceitável — o mecanismo de login funciona, mas não atende padrões de segurança reais. |

---

## Resumo (metas de qualidade)

| Subcaracterística | Medida | Valor-alvo |
|---|---|---|
| Confidencialidade | Acesso restrito a usuários autorizados | 3 |
| Integridade | Prevenção de modificação não autorizada | 3 |
| Não-repúdio | Trilha de auditoria de operações | 2 |
| Responsabilização | Rastreamento de ações ao usuário | 2 |
| Autenticidade | Verificação de identidade do usuário | 3 |

| Característica | Média |
|-|-|
| Segurança | **2.60** |

### Principais Pontos de Melhoria

1. **Confidencialidade**: Armazenar senhas com hash (bcrypt/scrypt) em vez de texto plano; criptografar arquivos de persistência JSON.
2. **Integridade**: Implementar transações atômicas para operações que envolvem múltiplas contas (ex: PIX); adicionar checksum ou assinatura digital nos arquivos JSON para detectar adulteração externa.
3. **Não-repúdio**: Adicionar timestamp e identificação do cliente em cada `Movimentacao`; persistir extrato junto com os dados da conta.
4. **Responsabilização**: Criar um log de auditoria centralizado que registre todas as operações com identificação do usuário, data/hora e resultado.
5. **Autenticidade**: Implementar limite de tentativas de login, bloqueio temporário após falhas consecutivas, e política de senha forte (mínimo 8 caracteres, incluindo maiúsculas, minúsculas, números e símbolos).

> Estes valores expressam o **nível de qualidade que o WinxBank deveria atingir** em cada subcaracterística de segurança, servindo de referência para especificar requisitos, definir medidas e avaliar o produto — conforme o uso das características de qualidade na ISO/IEC 25010.
