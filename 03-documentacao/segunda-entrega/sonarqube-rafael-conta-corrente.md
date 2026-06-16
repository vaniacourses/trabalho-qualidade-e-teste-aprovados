# Inspeção com SonarQube — classe `ContaCorrente` (Rafael)

## Ambiente

- **SonarQube** 9.9 LTS Community (container Docker local, `http://localhost:9000`).
- Plugin `sonar-maven-plugin` configurado no `pom.xml`.
- Java 21 (via `JAVA_HOME=/usr/lib/jvm/java-21-openjdk`).

```bash
mvn clean test sonar:sonar -Dsonar.login=TOKEN -Dsonar.host.url=http://localhost:9000
```

---

## ANTES das correções — `ContaCorrente.java`: 10 issues

| Severidade | Qtd | Regra | Descrição |
|-----------|-----|-------|-----------|
| MAJOR | 9 | S106 | Uso de `System.out.println` em vez de logger |
| MINOR | 1 | S3400 | `getTipoDaConta()` retorna uma constante — sugere substituir o método por constante |

**Métricas do arquivo:**

| Métrica | Valor |
|---------|-------|
| Code smells | 10 |
| Bugs | 0 |
| Vulnerabilidades | 0 |
| Complexidade | 12 |
| Complexidade cognitiva | 8 |
| Maintainability Rating | A |

**Distribuição dos issues S106:**

| Linha | Método |
|-------|--------|
| 75 | `exibirCartaoEConfirmar()` — separador |
| 76 | `exibirCartaoEConfirmar()` — dados do cartão |
| 77 | `exibirCartaoEConfirmar()` — separador |
| 78 | `exibirCartaoEConfirmar()` — mensagem de confirmação |
| 91 | `comprar()` — pergunta débito/crédito |
| 96 | `comprar()` — "Valor debitado." |
| 99 | `comprar()` — "Compra cancelada." |
| 105 | `comprar()` — "Valor creditado." |
| 108 | `comprar()` — "Compra cancelada." |

---

## Correções aplicadas

Todos os 10 issues foram analisados e marcados como **Falso Positivo** no SonarQube, com as seguintes justificativas:

### S106 — `System.out.println` (9 ocorrências)

> **Falso positivo.** O WinxBank é uma aplicação CLI interativa. `System.out` É o canal de I/O pretendido para comunicação com o usuário. Substituir por um logger alteraria a saída da aplicação, quebraria a experiência do usuário final e exigiria reescrever os 4 testes de verificação de saída (`testComprar*VerificaSaida`) que validam as mensagens exibidas no console.

### S3400 — `getTipoDaConta()` retorna constante (1 ocorrência)

> **Falso positivo.** O método `getTipoDaConta()` sobrescreve um método abstrato da superclasse `Conta`. Não pode ser removido nem substituído por uma constante de classe, pois o polimorfismo depende da sobrescrita para diferenciar `ContaCorrente` ("Corrente") de `ContaPoupanca` ("Poupanca").

---

## DEPOIS das correções — `ContaCorrente.java`: 0 issues

| Severidade | Qtd |
|-----------|-----|
| ~~MAJOR~~ | **0** (9 S106 marcados como falso positivo) |
| ~~MINOR~~ | **0** (1 S3400 marcado como falso positivo) |

**Resultado:** 10 → 0 issues. **Code smells zerados** no arquivo.

---

## Prints (evidências antes/depois)

| Print | Arquivo | O que mostra |
|-------|---------|-------------|
| **ANTES** | `prints-sonarqube/03-antes-contacorrente-issues.png` | Issues list — 10 code smells (9× S106 + 1× S3400) |
| **DEPOIS** | `prints-sonarqube/04-depois-contacorrente-issues.png` | Issues list — 0 issues após marcar como falso positivo |
