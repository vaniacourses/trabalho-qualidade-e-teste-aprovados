# Inspeção com SonarQube — classe `Banco` (Caio)

## Ambiente

- **SonarQube** 9.9 LTS Community (container Docker local, `http://localhost:9000`).
- Plugin `sonar-maven-plugin` configurado no `pom.xml`.
- Comando de análise (com cobertura do JaCoCo):

```bash
# Java 17 é obrigatório (Mockito/JaCoCo quebram em JDK > 22)
JAVA_HOME=$(/usr/libexec/java_home -v 17) \
  mvn clean test sonar:sonar -Dsonar.login=SEU_TOKEN
```

> Para os **prints** do enunciado: abrir `http://localhost:9000/dashboard?id=winxbank`,
> navegar até `src/main/java/.../sistemabancario/Banco.java` e capturar a tela
> **antes** e **depois** das correções (os números abaixo correspondem a essas telas).

---

## ANTES das correções — `Banco.java`: 15 issues

| Severidade | Qtd | Regra | Descrição |
|---|---|---|---|
| **CRITICAL (BUG)** | 1 | S2119 | `Random` instanciado a cada chamada de `printarBanco` — deve ser reutilizado |
| **CRITICAL** | 1 | S3776 | Complexidade cognitiva de `movimentarEntreBancoConta` = 16 (limite 15) |
| MAJOR | 11 | S106 | Uso de `System.out` em vez de logger |
| MINOR | 2 | S1104 | Campos `receitas`/`despesas` públicos |

---

## Correções aplicadas (os 2 CRÍTICOS)

1. **S2119 (BUG)** — `Random` movido de variável local de `printarBanco` para um campo
   `private final transient Random randomNum`, reutilizado entre chamadas.
2. **S3776 (complexidade cognitiva)** — extraído o corpo do laço de
   `movimentarEntreBancoConta` para o método privado `processarMovimentacaoDaConta(Conta)`
   e removido o `if (!isEmpty())` redundante (o laço sobre lista vazia já não executa).
   A complexidade caiu abaixo do limite, **sem alterar o comportamento**.

Todos os **139 testes continuam passando** após o refactor.

---

## DEPOIS das correções — `Banco.java`: 13 issues

| Severidade | Qtd | Regra |
|---|---|---|
| ~~CRITICAL~~ | **0** | — (os 2 críticos foram resolvidos) |
| MAJOR | 11 | S106 |
| MINOR | 2 | S1104 |

**Resultado:** 15 → 13 issues; **os 2 problemas de maior severidade (1 BUG + 1 CRITICAL) foram eliminados.**

---

## Issues remanescentes — análise e justificativa

Os 13 issues restantes foram avaliados e **mantidos conscientemente**, pois corrigi-los
impacta código compartilhado de toda a equipe e/ou o design da aplicação:

- **S106 — `System.out` (11 ocorrências):** o WinxBank é uma **aplicação de console**;
  `System.out` é a própria **interface com o usuário**. Trocar por logger alteraria a saída
  da aplicação e exigiria reescrever os testes que verificam essas mensagens. (Observação:
  caso a equipe deseje, a classe `CartaoCredito` já usa `java.util.logging.Logger` como
  referência para essa migração.)
- **S1104 — campos `receitas`/`despesas` públicos (2):** esses campos do *singleton* `Banco`
  são reiniciados diretamente (`Banco.getInstancia().receitas = 0.0`) no `@BeforeEach` de
  **praticamente toda a suíte de testes** (vários integrantes). Encapsulá-los exigiria um
  método de reinício e a alteração coordenada dos testes de todo o grupo.

Essas decisões estão alinhadas ao espírito do SonarQube (avaliar e justificar), priorizando
a correção do que tem maior severidade e menor risco para a entrega.
