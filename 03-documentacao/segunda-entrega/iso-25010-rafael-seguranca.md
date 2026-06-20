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
| **Confidencialidade** | Grau em que o sistema assegura que dados são acessíveis apenas por usuários autorizados | 1 = Dados expostos a qualquer usuário; 3 = Dados protegidos por autenticação básica; **5 = Dados protegidos com autenticação robusta, sessão isolada e senhas nunca expostas** | **3** | A meta 3 (aceitável) é adequada para um protótipo acadêmico de sistema bancário: espera-se que o acesso aos dados seja protegido por autenticação básica (login por CPF + senha), garantindo que cada cliente acesse apenas suas próprias informações. Não se exige criptografia de arquivos de persistência nem hash de senhas, pois o escopo é demonstrar a lógica de negócio e os testes de software — e não implementar segurança de produção (que demandaria meta 5). |

---

## 2. Integridade

| Subcaracterística | Medida | Escala (1-5) | Valor | Justificativa |
|-|-|-|-|-|
| **Integridade** | Grau em que o sistema previne acesso ou modificação não autorizada de dados | 1 = Dados podem ser corrompidos por qualquer operação; 3 = Validações básicas previnem corrupção acidental; **5 = Validações completas com transações atômicas e detecção de adulteração** | **3** | A meta 3 (aceitável) reflete que o sistema deve possuir validações básicas que impeçam corrupção acidental de dados — rejeição de valores negativos em saldos, verificação de limites de cartão e bloqueio de CPF duplicado. Para um protótipo acadêmico, não se exige transações atômicas nem detecção de adulteração de arquivos externos (como edição manual do JSON), características que elevariam a meta para 5 e são esperadas apenas em sistemas bancários de produção. |

---

## 3. Não-repúdio

| Subcaracterística | Medida | Escala (1-5) | Valor | Justificativa |
|-|-|-|-|-|
| **Não-repúdio** | Grau em que ações ou eventos podem ser provados como tendo ocorrido, de modo que não possam ser repudiados posteriormente | 1 = Nenhuma trilha de auditoria; 3 = Registro básico de operações (extrato por conta); **5 = Log de auditoria imutável com identificação do usuário e timestamp de cada operação** | **2** | A meta 2 (abaixo do desejável) é realista para o escopo acadêmico: espera-se que o sistema registre ao menos as operações realizadas em cada conta (extrato), permitindo conferência básica. Um log de auditoria imutável com timestamp e identificação do usuário (meta 5) exigiria infraestrutura de persistência e logging que está além do propósito de prototipação e ensino de teste de software. |

---

## 4. Responsabilização (Accountability)

| Subcaracterística | Medida | Escala (1-5) | Valor | Justificativa |
|-|-|-|-|-|
| **Responsabilização** | Grau em que as ações de uma entidade podem ser rastreadas de forma única até a entidade | 1 = Nenhum rastreamento de ações; 3 = Ações rastreáveis ao cliente logado na sessão atual; **5 = Histórico completo e permanente de ações de cada usuário com identificação única** | **2** | A meta 2 (abaixo do desejável) reflete que o rastreamento de ações ao usuário logado durante a sessão é suficiente para demonstrar o funcionamento do sistema em âmbito acadêmico. Um histórico permanente e completo de auditoria com identificação única de cada usuário (meta 5) demandaria persistência de logs e mecanismos de identificação que fogem ao propósito de prototipação voltada ao ensino de qualidade e teste de software. |

---

## 5. Autenticidade

| Subcaracterística | Medida | Escala (1-5) | Valor | Justificativa |
|-|-|-|-|-|
| **Autenticidade** | Grau em que a identidade de um sujeito ou recurso pode ser provada como sendo a reivindicada | 1 = Sem verificação de identidade; 3 = Verificação por senha sem políticas de segurança; **5 = Autenticação multifator com políticas de senha forte** | **3** | A meta 3 (aceitável) é coerente com o escopo acadêmico: a verificação de identidade por CPF + senha atende ao propósito de demonstrar o fluxo de autenticação em um sistema bancário. Políticas de senha forte (tamanho mínimo, caracteres especiais), bloqueio por tentativas consecutivas e autenticação multifator (meta 5) são requisitos de sistemas bancários em produção — sua ausência é aceitável em um protótipo voltado ao ensino de qualidade e teste de software. |

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

### Ações para elevar as metas de qualidade

As ações abaixo indicam o que seria necessário para elevar cada subcaracterística da meta atual para a meta 5 (excelente), caso o sistema evoluísse para um ambiente de produção:

1. **Confidencialidade (3 → 5)**: Armazenar senhas com hash (bcrypt/scrypt) em vez de texto plano; criptografar arquivos de persistência JSON.
2. **Integridade (3 → 5)**: Implementar transações atômicas para operações que envolvem múltiplas contas (ex: PIX); adicionar checksum ou assinatura digital nos arquivos JSON para detectar adulteração externa.
3. **Não-repúdio (2 → 5)**: Adicionar timestamp e identificação do cliente em cada `Movimentacao`; persistir extrato junto com os dados da conta.
4. **Responsabilização (2 → 5)**: Criar um log de auditoria centralizado que registre todas as operações com identificação do usuário, data/hora e resultado.
5. **Autenticidade (3 → 5)**: Implementar limite de tentativas de login, bloqueio temporário após falhas consecutivas, e política de senha forte (mínimo 8 caracteres, incluindo maiúsculas, minúsculas, números e símbolos).

> Estes valores expressam o **nível de qualidade que o WinxBank deveria atingir** em cada subcaracterística de segurança, servindo de referência para especificar requisitos, definir medidas e avaliar o produto — conforme o uso das características de qualidade na ISO/IEC 25010.
