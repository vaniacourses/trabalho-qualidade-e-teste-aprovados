package br.winxbank.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.winxbank.sistemabancario.Banco;
import br.winxbank.sistemabancario.Cartao;
import br.winxbank.sistemabancario.ContaPoupanca;

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
}
