Vou analisar o projeto completo antes de reescrever.Já tenho todo o código nos documentos do contexto. Segue a análise refeita para o projeto inteiro:

---

# ISO/IEC 25010 – Medidas de Qualidade

**Integrante:** João Victor Amaral

**Atributos avaliados:** Manutenibilidade e Portabilidade

---

## Tabela de Avaliação

| Subcaracterística | Medida | Escala (1–5) | Valor atribuído | Justificativa |
|---|---|---|---|---|
| Analisabilidade | Facilidade de compreender a lógica do sistema como um todo e identificar causas de falhas | 1 = muito difícil, 3 = moderado, 5 = muito fácil | 3 | A separação em pacotes (sistemabancario, sistemaclientes, repository, gui, exception) organiza bem as responsabilidades. Porém, a classe `Main` concentra toda a lógica de fluxo em um único bloco `switch` com cerca de 350 linhas, e `ArquivoDeClientes` mistura parsing de JSON, lógica de negócio e persistência, dificultando o rastreio de falhas nessas áreas centrais. |
| Modificabilidade | Facilidade para alterar regras de negócio ou adicionar funcionalidades sem impactar múltiplas partes do sistema | 1 = alterações complexas, 3 = alterações moderadas, 5 = alterações simples | 2 | O uso generalizado do padrão Singleton (`Banco`, `RegistroDeClientes`, `Ano`, `ArquivoDeClientes`, etc.) cria acoplamento global entre as classes. Adicionar um novo tipo de conta, por exemplo, exigiria alterações em `Banco`, `ArquivoDeClientes`, `Main`, `WinxBankController` e possivelmente nos painéis da GUI. O Singleton de `Ano` chamando diretamente `Banco.getInstancia()` é um exemplo concreto de acoplamento bidirecional que complica qualquer modificação. |
| Testabilidade | Facilidade de criar e executar testes unitários e de integração para validar o comportamento das classes | 1 = difícil testar, 3 = parcialmente testável, 5 = totalmente testável | 2 | A estrutura de testes (JUnit 5, Mockito, PIT, JaCoCo) está bem configurada no `pom.xml`. Contudo, a dependência de `Scanner(System.in)` espalhada por `Banco.abrirNovaConta()`, `ContaCorrente.comprar()` e `CartaoCredito.ajustarLimite()` exigiu a criação da classe `ConsoleBridge` apenas para viabilizar testes via GUI. Os Singletons com estado global também dificultam o isolamento entre testes, podendo causar interferência entre casos de teste se o estado não for zerado manualmente. |
| Reusabilidade | Capacidade de reutilizar módulos ou classes do sistema em outros contextos ou projetos | 1 = não reutilizável, 3 = reutilização limitada, 5 = altamente reutilizável | 2 | As classes de domínio (`Conta`, `Cliente`, `Movimentacao`) poderiam ser reutilizáveis em isolamento, mas estão acopladas aos Singletons globais. `ContaCorrente.comprar()` e `ContaPoupanca.comprar()` instanciam `Scanner` internamente, tornando-as inutilizáveis fora de um contexto de console sem adaptação. `Banco` e `Ano` dependem um do outro, impedindo o reuso independente de qualquer um dos dois. |
| Adaptabilidade | Facilidade de adaptar o sistema para diferentes ambientes de execução (diferentes SOs, JVMs, configurações) | 1 = dependente de ambiente específico, 3 = adaptável com ajustes, 5 = totalmente independente | 3 | O projeto usa apenas Java 17 padrão e Maven, sem bibliotecas nativas ou dependências de SO. No entanto, os caminhos de arquivo (`"clientes.json"`, `"banco.txt"`, `"mesAtual.txt"`) são relativos ao diretório de execução sem qualquer configuração, o que pode causar falhas dependendo do ambiente. O método `ConsoleBridge.localizedNumber()` trata corretamente o separador decimal conforme o locale, o que é um ponto positivo de adaptabilidade. |
| Instalabilidade | Facilidade de implantação e configuração do sistema em um novo ambiente | 1 = configuração complexa, 3 = configuração moderada, 5 = configuração simples | 4 | O `pom.xml` usa o `maven-shade-plugin` para gerar um fat JAR autocontido, e o `maven-compiler-plugin` define Java 17 como alvo. A execução se resume a `mvn install` seguido de `java -jar`. O único ponto de atenção é a necessidade dos arquivos de dados (`clientes.json`, `mesAtual.txt`, `banco.txt`) no diretório de trabalho, sem geração automática deles na primeira execução, o que pode gerar erros silenciosos (tratados com `e.printStackTrace()` sem fallback claro ao usuário). |
| Substituibilidade | Facilidade de substituir um componente por outro equivalente sem impacto no restante do sistema | 1 = difícil substituir, 3 = substituição com adaptações, 5 = substituição transparente | 2 | A ausência de interfaces de domínio (não há `IBanco`, `IRegistroDeClientes`, etc.) e o uso massivo de Singletons acessados diretamente via `getInstancia()` em todo o código tornam a substituição de qualquer componente central uma operação de alto impacto. Substituir, por exemplo, a persistência em JSON por um banco de dados exigiria alterar `ArquivoDeClientes`, `ArquivoBanco`, `Main` e `WinxBankController` simultaneamente, sem nenhuma abstração de repositório que isolasse essa responsabilidade. |

---

## Resumo

| Característica | Subcaracterística | Valor |
|---|---|---|
| Manutenibilidade | Analisabilidade | 3 |
| Manutenibilidade | Modificabilidade | 2 |
| Manutenibilidade | Testabilidade | 2 |
| Manutenibilidade | Reusabilidade | 2 |
| Portabilidade | Adaptabilidade | 3 |
| Portabilidade | Instalabilidade | 4 |
| Portabilidade | Substituibilidade | 2 |
