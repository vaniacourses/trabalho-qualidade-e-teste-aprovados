# ISO/IEC 25010 — Medidas de Atributos de Qualidade (WinxBank)

**Integrante:** Caio (cakocaito)
**Atributos avaliados (tarefa 3.2):** Eficiência de Desempenho e Compatibilidade

## Escala utilizada

| Valor | Significado |
|------|-------------|
| 1 | Muito insatisfatório |
| 2 | Insatisfatório |
| 3 | Aceitável |
| 4 | Bom |
| 5 | Excelente |

A coluna **Medida** indica *o que* foi medido; a coluna **Valor** é a nota atribuída na escala 1–5; a **Justificativa** baseia-se no código do WinxBank e nos testes implementados nesta entrega.

---

## 1. Eficiência de Desempenho

> Capacidade de o software desempenhar suas funções dentro dos requisitos de tempo e de recursos.

| Subcaracterística | Medida | Escala | Valor | Justificativa |
|---|---|---|---|---|
| **Comportamento em relação ao tempo** | Tempo de resposta de `Banco.movimentarEntreBancoConta` para 10.000 clientes (`System.nanoTime`) | 1–5 | **5** | Medido em **~5 ms** para 10.000 clientes no `DesempenhoBancoTest`, muito abaixo do baseline definido (≤ 2.000 ms). As operações são O(n) e simples (aritmética sobre saldos), sem I/O na operação. |
| **Utilização de recursos** | Uso de memória/CPU durante a operação | 1–5 | **4** | Consumo proporcional ao número de clientes/contas (listas `ArrayList` em memória), sem vazamentos observados. Não há estruturas custosas nem concorrência; CPU baixa. Perde ponto por manter **todo o estado em memória**. |
| **Capacidade** | Número máximo de clientes/contas suportado | 1–5 | **3** | Limitado pela memória da JVM, pois clientes e contas ficam **inteiramente em RAM** (`ArrayList`), sem paginação nem persistência incremental. Atende bem a volumes pequenos/médios (uso acadêmico), mas não escala para grandes bases. |

**Nota de evidência:** o valor de *Comportamento em relação ao tempo* é sustentado pela execução do teste `DesempenhoBancoTest` (saída: `movimentarEntreBancoConta com 10000 clientes: 5 ms`).

---

## 2. Compatibilidade

> Capacidade de o software coexistir com outros sistemas e trocar informações com eles.

| Subcaracterística | Medida | Escala | Valor | Justificativa |
|---|---|---|---|---|
| **Coexistência** | Capacidade de executar junto a outras aplicações sem conflito de recursos | 1–5 | **3** | A aplicação é *standalone* (não disputa portas/serviços), mas persiste estado em **arquivos locais de nomes fixos** (`banco.txt`, `clientes.json`, `mesAtual.txt`) no diretório de trabalho. Duas instâncias no mesmo diretório entrariam em conflito de escrita. Sem isso, a coexistência seria boa. |
| **Interoperabilidade** | Capacidade de trocar e usar dados com outros sistemas | 1–5 | **2** | A persistência usa **JSON** (formato aberto, via `json-simple`) e arquivos `.txt`, o que é um ponto positivo; porém **não há API, interface de integração ou contrato de dados** documentado. Os dados são acoplados ao formato interno da aplicação, dificultando o consumo por terceiros. |

---

## Resumo

| Característica | Subcaracterística | Valor |
|---|---|---|
| Eficiência de Desempenho | Comportamento em relação ao tempo | 5 |
| Eficiência de Desempenho | Utilização de recursos | 4 |
| Eficiência de Desempenho | Capacidade | 3 |
| Compatibilidade | Coexistência | 3 |
| Compatibilidade | Interoperabilidade | 2 |

**Conclusão:** o WinxBank é **eficiente em tempo** para a escala do projeto (operações rápidas e O(n)), com uso de recursos adequado, mas **limitado em capacidade e em compatibilidade** por manter todo o estado em memória e por persistir em arquivos locais fixos, sem uma camada de integração/API. Em linha com a ISO 25010, esses são *trade-offs* aceitáveis para o contexto (aplicação acadêmica de console), e não comprometem o uso pretendido.
