# Relatorio SonarQube - Primeira Entrega

## Escopo medido

- Versao medida: commit `fefb4da`
- Caminho medido na copia temporaria: `/private/tmp/winxbank-primeira-fefb4da/02-codigo-ajustado/winxbank`
- Projeto SonarQube gerado para baseline: `winxbank-primeira-entrega`
- Observacao: a primeira entrega nao tinha instrumentacao SonarQube no `pom.xml` original. O plugin foi adicionado apenas na copia temporaria para medir a baseline.

## Resultado geral do projeto

| Metrica | Score |
|---|---:|
| Quality Gate | OK |
| Bugs | 6 |
| Vulnerabilities | 0 |
| Code smells | 181 |
| Security hotspots | 16 |
| Coverage | 21,8% |
| Line coverage | 23,6% |
| Branch coverage | 16,2% |
| Testes | 44 |
| Falhas de teste | 4 |
| Erros de teste | 0 |
| Linhas duplicadas | 6,3% |
| Complexity | 287 |
| Cognitive complexity | 319 |
| Reliability rating | D |
| Security rating | A |
| Maintainability rating | A |

## Dados da primeira entrega por classe das tarefas

| Classe | Coverage | Line coverage | Branch coverage | Code smells | Security hotspots | Duplicated lines | Complexity | Cognitive complexity | Bugs | Vulnerabilities | Maintainability | Reliability | Security |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---|---|---|
| `RegistroDeClientes` | 7,2% | 9,2% | 3,8% | 21 | 0 | 0,0% | 38 | 40 | 0 | 0 | B | A | A |
| `Banco` | 73,2% | 69,0% | 84,6% | 14 | 1 | 0,0% | 23 | 23 | 1 | 0 | A | D | A |
| `CartaoCredito` | 97,5% | 100% | 87,5% | 4 | 0 | 0,0% | 12 | 5 | 0 | 0 | A | A | A |
| `ContaCorrente` | 22,4% | 26,8% | 0,0% | 15 | 0 | 10,6% | 11 | 8 | 0 | 0 | B | A | A |
| `Ano` | 94,3% | 95,7% | 91,7% | 2 | 0 | 0,0% | 11 | 10 | 0 | 0 | A | A | A |

## Leitura da baseline por classe

- `RegistroDeClientes`
  - cobertura muito baixa
  - manutencao B
  - concentrava muitos `code smells`
- `Banco`
  - boa cobertura de branches
  - ainda tinha `1 bug` aberto
  - `reliability rating` em D
- `CartaoCredito`
  - baseline ja forte em cobertura
  - poucos `code smells`
- `ContaCorrente`
  - baseline fraca em cobertura
  - `0,0%` de branch coverage
  - manutencao B
- `Ano`
  - baseline ja forte
  - poucas pendencias

## Principais conclusoes da primeira entrega

- A baseline da primeira entrega era desigual entre as classes.
- `RegistroDeClientes` e `ContaCorrente` estavam entre os pontos mais fracos em cobertura.
- `Banco` tinha um problema real de confiabilidade medido pelo SonarQube:
  - `1 bug`
  - `reliability rating D`
- `CartaoCredito` e `Ano` ja partiam de uma baseline mais madura.

## Conclusao para slide

- O relatorio da primeira entrega deve ser lido como baseline completa das classes das tarefas.
- Os piores pontos da baseline foram:
  - `RegistroDeClientes`: `3,8%` de branch coverage
  - `ContaCorrente`: `0,0%` de branch coverage
  - `Banco`: `1 bug` e confiabilidade D
