# ISO/IEC 25010 – Medidas de Qualidade

**Integrante:** João Victor Amaral

**Atributos avaliados:** Manutenibilidade e Portabilidade

| Subcaracterística | Medida                                                                          | Escala (1-5)                                                                                  | Valor atribuído | Justificativa                                                                                                                                                                               |
| ----------------- | ------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------- | --------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Analisabilidade   | Facilidade de compreender a lógica da classe Ano e identificar causas de falhas | 1 = muito difícil, 3 = moderado, 5 = muito fácil                                              | 5               | A classe possui poucos métodos, responsabilidades bem definidas e lógica simples para controle de meses, facilitando a compreensão e análise de problemas.                                  |
| Modificabilidade  | Facilidade para alterar regras da classe sem impactar outras partes do sistema  | 1 = alterações complexas, 3 = alterações moderadas, 5 = alterações simples                    | 3               | Alterações em regras de calendário são simples, porém existe acoplamento direto com Banco através de chamadas estáticas, o que reduz a facilidade de modificação.                           |
| Testabilidade     | Facilidade de criar e executar testes para validar o comportamento da classe    | 1 = difícil testar, 3 = parcialmente testável, 5 = totalmente testável                        | 4               | Foi possível criar testes unitários e de integração cobrindo cenários válidos, inválidos, mudança de mês e overflow. O uso de atributos estáticos reduz parcialmente a facilidade de teste. |
| Reusabilidade     | Capacidade de reutilizar a lógica da classe em outros contextos                 | 1 = não reutilizável, 3 = reutilização limitada, 5 = altamente reutilizável                   | 3               | A lógica de controle de meses pode ser reutilizada, porém a dependência direta do Banco limita seu uso em outros projetos.                                                                  |
| Adaptabilidade    | Facilidade de adaptação para diferentes ambientes de execução                   | 1 = dependente de ambiente específico, 3 = adaptável com ajustes, 5 = totalmente independente | 5               | A implementação utiliza apenas recursos padrão da linguagem Java, sem dependências específicas de sistema operacional ou plataforma.                                                        |
| Instalabilidade   | Facilidade de implantação e configuração da funcionalidade                      | 1 = configuração complexa, 3 = configuração moderada, 5 = configuração simples                | 5               | A classe faz parte do projeto Maven e não exige configuração adicional para utilização.                                                                                                     |
| Substituibilidade | Facilidade de substituir a implementação por outra equivalente                  | 1 = difícil substituir, 3 = substituição com adaptações, 5 = substituição transparente        | 3               | O uso do padrão Singleton e o acoplamento com Banco exigiriam adaptações caso outra implementação de calendário fosse utilizada.                                                            |

## Resumo

| Característica   | Subcaracterística | Valor |
| ---------------- | ----------------- | ----- |
| Manutenibilidade | Analisabilidade   | 4     |
| Manutenibilidade | Modificabilidade  | 3     |
| Manutenibilidade | Testabilidade     | 4     |
| Manutenibilidade | Reusabilidade     | 3     |
| Portabilidade    | Adaptabilidade    | 5     |
| Portabilidade    | Instalabilidade   | 5     |
| Portabilidade    | Substituibilidade | 3     |
