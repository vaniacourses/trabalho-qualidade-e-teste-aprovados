# ISO/IEC 25010 — Medidas de Atributos de Qualidade (WinxBank)

**Integrante:** Caio (cakocaito)
**Atributos avaliados:** Eficiência de Desempenho e Compatibilidade

> **Enfoque (conforme o enunciado):** as medidas abaixo indicam **como o sistema
> *deveria ser*** — ou seja, são **requisitos/metas de qualidade** que o WinxBank
> deveria atingir, e **não** uma medição do que já está implementado. Para cada
> subcaracterística há: a **medida** (o que medir), uma **escala 1–5** (o que cada
> nível significa), o **valor-alvo desejado** e a **justificativa** dessa meta para
> o contexto do sistema (aplicação bancária interativa).

## Escala (genérica)

| Valor | Significado |
|------|-------------|
| 1 | Muito abaixo do desejável |
| 2 | Abaixo do desejável |
| 3 | Aceitável (mínimo desejável) |
| 4 | Bom |
| 5 | Excelente (ideal) |

---

## 1. Eficiência de Desempenho

> O quanto o sistema **deveria** desempenhar suas funções dentro de limites de tempo e de recursos.

### 1.1 Comportamento em relação ao tempo
- **Medida:** tempo de resposta de uma operação interativa (login, depósito, saque, movimentação) para a carga esperada.
- **Escala:** 1 = > 5 s · 2 = 2–5 s · 3 = 1–2 s · 4 = 200 ms–1 s · **5 = < 200 ms**.
- **Valor-alvo desejado: 5** — o sistema **deveria** responder a cada operação em **menos de 200 ms**.
- **Justificativa:** é uma aplicação **interativa**; o usuário espera resposta praticamente imediata a cada comando. Como as operações são locais (sem dependências de rede), o desejável é a resposta quase instantânea.

### 1.2 Utilização de recursos
- **Medida:** crescimento do uso de memória/CPU em função do volume de dados (clientes e contas) e ausência de vazamentos.
- **Escala:** 1 = consumo descontrolado/vazamentos · 3 = crescimento aceitável com alguns recursos não liberados · **5 = consumo linear e previsível, sem vazamentos, liberando recursos (arquivos, Scanners)**.
- **Valor-alvo desejado: 5** — o consumo **deveria** crescer de forma **linear** com o número de clientes e **não vazar** recursos.
- **Justificativa:** o sistema **deveria** liberar recursos de E/S (ex.: fechar `Scanner`/arquivos) e não acumular memória, garantindo execução estável em sessões longas.

### 1.3 Capacidade
- **Medida:** número máximo de clientes/contas que o sistema **deveria** suportar mantendo o tempo-alvo (< 200 ms).
- **Escala:** 1 = < 100 · 2 = 100–1 mil · **3 = 1 mil–10 mil** · 4 = 10 mil–100 mil · 5 = > 100 mil.
- **Valor-alvo desejado: 3** — o sistema **deveria** atender pelo menos **~10 mil clientes** sem degradar o desempenho.
- **Justificativa:** para o contexto (uma agência / uso acadêmico), suportar ~10 mil clientes é suficiente. Capacidades maiores exigiriam persistência fora da memória (banco de dados), o que está além do escopo pretendido do sistema.

---

## 2. Compatibilidade

> O quanto o sistema **deveria** coexistir com outros sistemas e trocar informações com eles.

### 2.1 Coexistência
- **Medida:** capacidade de executar ao mesmo tempo que outras aplicações (ou outra instância dele) sem conflito de recursos compartilhados (arquivos de dados).
- **Escala:** 1 = conflita sempre (recursos fixos) · 3 = coexiste com outras aplicações, mas não com outra instância · **5 = nunca conflita (local de dados configurável/isolado por instância)**.
- **Valor-alvo desejado: 4** — o sistema **deveria** coexistir com outras aplicações e permitir mais de uma instância, com o **local dos arquivos de dados configurável** (em vez de nomes fixos como `banco.txt`/`clientes.json`).
- **Justificativa:** sendo um app local, o mínimo desejável é não corromper dados ao rodar junto de outros programas; permitir parametrizar o diretório de dados evita conflito entre instâncias.

### 2.2 Interoperabilidade
- **Medida:** capacidade de trocar/exportar dados em formato padrão consumível por outros sistemas.
- **Escala:** 1 = formato proprietário fechado · **3 = persistência/exportação em formato padrão (JSON) bem definido** · 5 = API documentada + contrato de dados estável.
- **Valor-alvo desejado: 3** — o sistema **deveria**, no mínimo, persistir e **exportar dados em formato padrão (JSON)** bem documentado, permitindo consumo por outras ferramentas.
- **Justificativa:** para uma aplicação de console, uma API completa de integração não é requisito; porém o uso de um formato aberto e documentado (JSON) já é desejável e suficiente para a interoperabilidade esperada.

---

## Resumo (metas de qualidade)

| Característica | Subcaracterística | Medida | Valor-alvo |
|---|---|---|---|
| Eficiência de Desempenho | Comportamento no tempo | tempo de resposta | 5 (< 200 ms) |
| Eficiência de Desempenho | Utilização de recursos | crescimento/vazamento | 5 (linear, sem vazar) |
| Eficiência de Desempenho | Capacidade | nº de clientes suportados | 3 (~10 mil) |
| Compatibilidade | Coexistência | conflito de recursos | 4 (dados configuráveis) |
| Compatibilidade | Interoperabilidade | troca de dados | 3 (JSON documentado) |

> Estes valores expressam o **nível de qualidade que o WinxBank deveria atingir** em
> cada atributo (requisitos de qualidade), servindo de referência para especificar
> requisitos, definir medidas e avaliar o produto — conforme o uso das características
> de qualidade na ISO/IEC 25010.
