package br.winxbank.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.winxbank.sistemabancario.Banco;
import br.winxbank.sistemabancario.Cartao;
import br.winxbank.sistemabancario.CartaoCredito;
import br.winxbank.sistemabancario.ContaCorrente;
import br.winxbank.tempo.Ano;

/**
 * Testes funcionais (caixa-preta) para ContaCorrente.comprar().
 * Tecnicas: particao de equivalencia e analise de valor limite.
 */
public class ContaCorrenteFuncionalTest {

    private static final int NUMERO_CONTA = 2001;
    private static final int NUMERO_CARTAO = 6001;
    private static final int CSV_PADRAO = 456;
    private static final double SALDO_INICIAL = 500.0;

    @BeforeEach
    void setUp() {
        Banco.getInstancia().receitas = 0.0;
        Banco.getInstancia().despesas = 0.0;
        Ano.getInstancia().setMesAtual("Janeiro");
    }

    private ContaCorrente criarContaCorrente(double saldo) {
        Cartao cartao = new Cartao(NUMERO_CARTAO, CSV_PADRAO);
        CartaoCredito cartaoCredito = new CartaoCredito(NUMERO_CARTAO, CSV_PADRAO);
        return new ContaCorrente(NUMERO_CONTA, saldo, cartao, 0, cartaoCredito);
    }

    // ---------- Particionamento por forma de pagamento ----------

    @Test
    void testCenario1DebitoConfirmado() {
        System.out.println("Cenario 1 (Funcional): comprar no debito confirmado");

        ContaCorrente conta = criarContaCorrente(SALDO_INICIAL);
        double valorCompra = 100.0;

        System.setIn(new ByteArrayInputStream("1\n1\n".getBytes()));
        conta.comprar(valorCompra);

        System.out.println("Saldo esperado: 400.0 | Saldo final: " + conta.getSaldo());
        assertEquals(SALDO_INICIAL - valorCompra, conta.getSaldo(), 0.001);
    }

    @Test
    void testCenario2DebitoCancelado() {
        System.out.println("Cenario 2 (Funcional): comprar no debito cancelado");

        ContaCorrente conta = criarContaCorrente(SALDO_INICIAL);
        double valorCompra = 100.0;

        System.setIn(new ByteArrayInputStream("1\n2\n".getBytes()));
        conta.comprar(valorCompra);

        System.out.println("Saldo esperado: 500.0 | Saldo final: " + conta.getSaldo());
        assertEquals(SALDO_INICIAL, conta.getSaldo(), 0.001);
    }

    @Test
    void testCenario3CreditoConfirmado() {
        System.out.println("Cenario 3 (Funcional): comprar no credito confirmado");

        ContaCorrente conta = criarContaCorrente(SALDO_INICIAL);
        double valorCompra = 200.0;

        System.setIn(new ByteArrayInputStream("2\n1\n".getBytes()));
        conta.comprar(valorCompra);

        System.out.println("Fatura esperada: 200.0 | Fatura final: " + conta.getCartaoCredito().getFatura());
        System.out.println("Saldo nao deve mudar: 500.0 | Saldo final: " + conta.getSaldo());
        assertEquals(valorCompra, conta.getCartaoCredito().getFatura(), 0.001);
        assertEquals(SALDO_INICIAL, conta.getSaldo(), 0.001);
    }

    @Test
    void testCenario4CreditoCancelado() {
        System.out.println("Cenario 4 (Funcional): comprar no credito cancelado");

        ContaCorrente conta = criarContaCorrente(SALDO_INICIAL);
        double valorCompra = 200.0;

        System.setIn(new ByteArrayInputStream("2\n2\n".getBytes()));
        conta.comprar(valorCompra);

        System.out.println("Fatura esperada: 0.0 | Fatura final: " + conta.getCartaoCredito().getFatura());
        assertEquals(0.0, conta.getCartaoCredito().getFatura(), 0.001);
    }

    @Test
    void testCenario5OpcaoInvalida() {
        System.out.println("Cenario 5 (Funcional): comprar com opcao invalida");

        ContaCorrente conta = criarContaCorrente(SALDO_INICIAL);

        System.setIn(new ByteArrayInputStream("5\n".getBytes()));
        conta.comprar(200.0);

        System.out.println("Saldo esperado: 500.0 | Saldo final: " + conta.getSaldo());
        System.out.println("Fatura esperada: 0.0 | Fatura final: " + conta.getCartaoCredito().getFatura());
        assertEquals(SALDO_INICIAL, conta.getSaldo(), 0.001);
        assertEquals(0.0, conta.getCartaoCredito().getFatura(), 0.001);
    }

    // ---------- Analise de Valor Limite ----------

    @Test
    void testCenario6DebitoComSaldoExato() {
        System.out.println("Cenario 6 (Funcional - Valor Limite): debito com saldo exato");

        double saldo = 150.0;
        ContaCorrente conta = criarContaCorrente(saldo);

        System.setIn(new ByteArrayInputStream("1\n1\n".getBytes()));
        conta.comprar(saldo);

        System.out.println("Saldo esperado: 0.0 | Saldo final: " + conta.getSaldo());
        assertEquals(0.0, conta.getSaldo(), 0.001);
    }

    @Test
    void testCenario7CreditoNoLimiteExato() {
        System.out.println("Cenario 7 (Funcional - Valor Limite): credito no limite exato (1000.0)");

        ContaCorrente conta = criarContaCorrente(SALDO_INICIAL);
        double valorLimite = 1000.0;

        System.setIn(new ByteArrayInputStream("2\n1\n".getBytes()));
        conta.comprar(valorLimite);

        System.out.println("Fatura esperada: 1000.0 | Fatura final: " + conta.getCartaoCredito().getFatura());
        assertEquals(valorLimite, conta.getCartaoCredito().getFatura(), 0.001);
    }

    @Test
    void testCenario8CreditoAcimaDoLimite() {
        System.out.println("Cenario 8 (Funcional - Valor Limite): credito acima do limite (1000.01)");

        ContaCorrente conta = criarContaCorrente(SALDO_INICIAL);
        double valorAcimaDoLimite = 1000.01;

        System.setIn(new ByteArrayInputStream("2\n1\n".getBytes()));
        conta.comprar(valorAcimaDoLimite);

        System.out.println("Fatura esperada: 0.0 | Fatura final: " + conta.getCartaoCredito().getFatura());
        assertEquals(0.0, conta.getCartaoCredito().getFatura(), 0.001);
    }

    @Test
    void testCenario9DebitoComValorZero() {
        System.out.println("Cenario 9 (Funcional - Valor Limite): debito com valor zero");

        ContaCorrente conta = criarContaCorrente(SALDO_INICIAL);

        System.setIn(new ByteArrayInputStream("1\n1\n".getBytes()));
        conta.comprar(0.0);

        System.out.println("Saldo esperado: 500.0 | Saldo final: " + conta.getSaldo());
        assertEquals(SALDO_INICIAL, conta.getSaldo(), 0.001);
    }

    @Test
    void testCenario10OpcaoZero() {
        System.out.println("Cenario 10 (Funcional - Valor Limite): opcao de pagamento zero");

        ContaCorrente conta = criarContaCorrente(SALDO_INICIAL);

        System.setIn(new ByteArrayInputStream("0\n".getBytes()));
        conta.comprar(200.0);

        System.out.println("Saldo esperado: 500.0 | Saldo final: " + conta.getSaldo());
        assertEquals(SALDO_INICIAL, conta.getSaldo(), 0.001);
    }

    @Test
    void testCenario11OpcaoNegativa() {
        System.out.println("Cenario 11 (Funcional - Valor Limite): opcao de pagamento negativa");

        ContaCorrente conta = criarContaCorrente(SALDO_INICIAL);

        System.setIn(new ByteArrayInputStream("-1\n".getBytes()));
        conta.comprar(200.0);

        System.out.println("Saldo esperado: 500.0 | Saldo final: " + conta.getSaldo());
        assertEquals(SALDO_INICIAL, conta.getSaldo(), 0.001);
    }
}
