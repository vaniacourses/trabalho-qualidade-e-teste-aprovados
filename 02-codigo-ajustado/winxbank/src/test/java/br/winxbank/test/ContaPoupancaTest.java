package br.winxbank.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.winxbank.sistemabancario.Banco;
import br.winxbank.sistemabancario.Cartao;
import br.winxbank.sistemabancario.ContaPoupanca;
import br.winxbank.sistemabancario.Movimentacao;

public class ContaPoupancaTest {

    private static final int NUMERO_CONTA = 10002;
    private static final int NUMERO_CARTAO = 5678;
    private static final int CSV_PADRAO = 888;
    private static final double RENDIMENTO_MENSAL_POUPANCA = 0.8;

    @BeforeEach
    void setUp() {
        Banco.getInstancia().receitas = 0.0;
        Banco.getInstancia().despesas = 0.0;
    }

    private ContaPoupanca criarContaPoupanca(double saldo) {
        Cartao cartao = new Cartao(NUMERO_CARTAO, CSV_PADRAO);
        return new ContaPoupanca(NUMERO_CONTA, saldo, cartao, 0.0);
    }

    /**
     * Verifica que ao acrescentar rendimento o saldo da conta é atualizado corretamente.
     * O rendimento é calculado como saldo / 0.8, e o valor resultante é somado ao saldo atual.
     * Exemplo: saldo inicial 500.0 → rendimento 625.0 → saldo final 1125.0.
     */
    @Test
    void testAcrescentarRendimentoAumentaSaldoCorretamente() {
        System.out.println("Teste acrescentar rendimento aumenta saldo da conta poupanca corretamente");

        double saldoInicial = 500.0;
        ContaPoupanca conta = criarContaPoupanca(saldoInicial);

        conta.acrescentarRendimento();

        double rendimento = saldoInicial / RENDIMENTO_MENSAL_POUPANCA; // 500 / 0.8 = 625
        double saldoEsperado = saldoInicial + rendimento;             // 500 + 625 = 1125

        double saldoFinal = conta.getSaldo();

        System.out.println("Saldo inicial: " + saldoInicial);
        System.out.println("Rendimento calculado: " + rendimento);
        System.out.println("Saldo esperado: " + saldoEsperado + " | Saldo final: " + saldoFinal);

        assertEquals(saldoEsperado, saldoFinal, 0.001);
    }

    /**
     * Verifica que ao acrescentar rendimento uma movimentação é registrada no informe de rendimento.
     * Cada chamada ao método deve adicionar exatamente uma entrada na lista de movimentações da conta.
     */
    @Test
    void testAcrescentarRendimentoRegistraMovimentacaoNoInformeDeRendimento() {
        System.out.println("Teste acrescentar rendimento registra movimentacao no informe de rendimento");

        double saldoInicial = 200.0;
        ContaPoupanca conta = criarContaPoupanca(saldoInicial);

        conta.acrescentarRendimento();

        int quantidadeMovimentacoes = conta.getInformeDeRendimento().size();

        System.out.println("Movimentacoes registradas no informe: " + quantidadeMovimentacoes);

        assertEquals(1, quantidadeMovimentacoes);
    }

    /**
     * Verifica que a movimentação registrada pelo acrescentarRendimento é do tipo ENTRADA.
     * O rendimento representa um crédito na conta, portanto a movimentação deve classificar
     * o valor como entrada de dinheiro.
     */
    @Test
    void testAcrescentarRendimentoRegistraMovimentacaoDoTipoEntrada() {
        System.out.println("Teste acrescentar rendimento registra movimentacao do tipo ENTRADA");

        ContaPoupanca conta = criarContaPoupanca(300.0);

        conta.acrescentarRendimento();

        Movimentacao.TipoDaMovimentacao tipo = conta.getInformeDeRendimento().get(0).getTipoDaMovimentacao();

        System.out.println("Tipo da movimentacao: " + tipo);

        assertEquals(Movimentacao.TipoDaMovimentacao.ENTRADA, tipo);
    }

    /**
     * Verifica que o valor armazenado na movimentação é o ganho líquido, e não o total do saldo.
     * O ganho líquido é a diferença entre o rendimento calculado e o saldo original.
     * Exemplo: saldo 200.0 → rendimento 250.0 → ganho líquido registrado 50.0.
     */
    @Test
    void testAcrescentarRendimentoRegistraValorCorretoDaMovimentacao() {
        System.out.println("Teste acrescentar rendimento registra o valor correto (ganho liquido) na movimentacao");

        double saldoInicial = 200.0;
        ContaPoupanca conta = criarContaPoupanca(saldoInicial);

        conta.acrescentarRendimento();

        double ganhoLiquido = (saldoInicial / RENDIMENTO_MENSAL_POUPANCA) - saldoInicial; // 250 - 200 = 50
        double valorMovimentacao = conta.getInformeDeRendimento().get(0).getDinheiroMovimentado();

        System.out.println("Ganho liquido esperado: " + ganhoLiquido + " | Valor na movimentacao: " + valorMovimentacao);

        assertEquals(ganhoLiquido, valorMovimentacao, 0.001);
    }

    /**
     * Verifica que ao acrescentar rendimento as despesas do banco são atualizadas corretamente.
     * O banco registra o valor total do rendimento calculado (saldo / 0.8) como despesa,
     * representando o custo do banco ao pagar o rendimento ao correntista.
     */
    @Test
    void testAcrescentarRendimentoAtualizaDespesasDoBanco() {
        System.out.println("Teste acrescentar rendimento atualiza despesas do banco");

        double saldoInicial = 300.0;
        ContaPoupanca conta = criarContaPoupanca(saldoInicial);

        conta.acrescentarRendimento();

        double despesasEsperadas = saldoInicial / RENDIMENTO_MENSAL_POUPANCA; // 375.0
        double despesasReais = Banco.getInstancia().despesas;

        System.out.println("Despesas esperadas: " + despesasEsperadas + " | Despesas do banco: " + despesasReais);

        assertEquals(despesasEsperadas, despesasReais, 0.001);
    }

    /**
     * Verifica que chamadas repetidas ao acrescentarRendimento acumulam movimentações distintas no informe.
     * Cada invocação deve adicionar uma nova entrada, garantindo o histórico completo de rendimentos
     * ao longo do tempo.
     */
    @Test
    void testAcrescentarRendimentoMultiplasVezesAcumulaMovimentacoes() {
        System.out.println("Teste acrescentar rendimento multiplas vezes acumula movimentacoes no informe");

        ContaPoupanca conta = criarContaPoupanca(100.0);

        conta.acrescentarRendimento();
        conta.acrescentarRendimento();
        conta.acrescentarRendimento();

        int quantidadeMovimentacoes = conta.getInformeDeRendimento().size();

        System.out.println("Movimentacoes registradas: " + quantidadeMovimentacoes);

        assertEquals(3, quantidadeMovimentacoes);
    }

    @Test
    void testGetTipoDaContaRetornaPoupanca() {
        System.out.println("Teste getTipoDaConta retorna Poupanca");

        ContaPoupanca conta = criarContaPoupanca(100.0);

        String tipo = conta.getTipoDaConta();

        System.out.println("Tipo da conta: " + tipo);

        assertEquals("Poupanca", tipo);
    }

    @Test
    void testDepositarAumentaSaldo() {
        System.out.println("Teste depositar aumenta o saldo da conta poupanca");

        double saldoInicial = 100.0;
        double valorDeposito = 50.0;
        ContaPoupanca conta = criarContaPoupanca(saldoInicial);

        conta.depositar(valorDeposito);

        double saldoEsperado = saldoInicial + valorDeposito;
        double saldoFinal = conta.getSaldo();

        System.out.println("Saldo esperado: " + saldoEsperado + " | Saldo final: " + saldoFinal);

        assertEquals(saldoEsperado, saldoFinal, 0.001);
    }

    @Test
    void testSacarDiminuiSaldo() {
        System.out.println("Teste sacar diminui o saldo da conta poupanca");

        double saldoInicial = 200.0;
        double valorSaque = 80.0;
        ContaPoupanca conta = criarContaPoupanca(saldoInicial);

        conta.sacar(valorSaque);

        double saldoEsperado = saldoInicial - valorSaque;
        double saldoFinal = conta.getSaldo();

        System.out.println("Saldo esperado: " + saldoEsperado + " | Saldo final: " + saldoFinal);

        assertEquals(saldoEsperado, saldoFinal, 0.001);
    }

    @Test
    void testGetNumeroContaRetornaNumeroCorreto() {
        System.out.println("Teste getNumeroConta retorna o numero correto da conta");

        ContaPoupanca conta = criarContaPoupanca(100.0);

        int numeroConta = conta.getNumeroConta();

        System.out.println("Numero esperado: " + NUMERO_CONTA + " | Numero retornado: " + numeroConta);

        assertEquals(NUMERO_CONTA, numeroConta);
    }
}
