package br.winxbank.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;

import br.winxbank.sistemabancario.CartaoCredito;
import br.winxbank.tempo.Ano;

public class CartaoCreditoTest {

    private static final double LIMITE_PADRAO = 1000.0;
    private static final int NUMERO_PADRAO = 1234;
    private static final int CSV_PADRAO = 999;
    private static final String MES_INICIAL = "Janeiro";
    private static final String MES_POSTERIOR = "Fevereiro";

    private CartaoCredito criarCartao(double fatura, int indexMesDaFatura, boolean faturaPaga, double limite) {
        return new CartaoCredito(fatura, indexMesDaFatura, faturaPaga, limite, NUMERO_PADRAO, CSV_PADRAO);
    }

    @Test
    void testCreditarValorDentroDoLimite() throws Exception {

        System.out.println("Teste creditar valor dentro do limite");

        double valorCompra = 500.0;

        Ano.getInstancia().setMesAtual(MES_INICIAL);

        CartaoCredito cartao = criarCartao(0.0, 0, true, LIMITE_PADRAO);

        cartao.creditar(valorCompra);

        double fatura = cartao.getFatura();

        System.out.println("Valor da compra: " + valorCompra);
        System.out.println("Fatura: " + fatura);

        assertEquals(500.0, fatura, 0.001);
    }

    @Test
    void testNaoDeveCreditarValorAcimaDoLimite() throws Exception {

        System.out.println("Teste nao deve creditar valor acima do limite");

        double valorCompra = 1200.0;

        Ano.getInstancia().setMesAtual(MES_INICIAL);

        CartaoCredito cartao = criarCartao(0.0, 0, true, LIMITE_PADRAO);

        cartao.creditar(valorCompra);

        double fatura = cartao.getFatura();

        System.out.println("Valor da compra: " + valorCompra);
        System.out.println("Fatura: " + fatura);

        assertEquals(0.0, fatura, 0.001);
    }

    @Test
    void testNaoDeveCobrarJurosQuandoFaturaPaga() throws Exception {

        System.out.println("Teste nao deve cobrar juros quando fatura esta paga");

        double faturaInicial = 500.0;

        Ano.getInstancia().setMesAtual(MES_INICIAL);

        CartaoCredito cartao = criarCartao(faturaInicial, 0, true, LIMITE_PADRAO);

        Ano.getInstancia().setMesAtual(MES_POSTERIOR);

        cartao.cobrarJurus();

        double faturaFinal = cartao.getFatura();

        System.out.println("Fatura inicial: " + faturaInicial);
        System.out.println("Fatura final: " + faturaFinal);

        assertEquals(500.0, faturaFinal, 0.001);
    }

    @Test
    void testCreditarValorExatamenteNoLimite() throws Exception {

        System.out.println("Teste creditar valor exatamente no limite");

        double valorCompra = 1000.0;

        Ano.getInstancia().setMesAtual(MES_INICIAL);

        CartaoCredito cartao = criarCartao(0.0, 0, true, LIMITE_PADRAO);

        cartao.creditar(valorCompra);

        double fatura = cartao.getFatura();

        System.out.println("Valor da compra: " + valorCompra);
        System.out.println("Fatura: " + fatura);

        assertEquals(1000.0, fatura, 0.001);
    }

    @Test
    void testNaoDeveUltrapassarLimiteComCreditosAcumulados() throws Exception {

        System.out.println("Teste nao deve ultrapassar limite com creditos acumulados");

        double primeiraCompra = 800.0;
        double segundaCompra = 300.0;

        Ano.getInstancia().setMesAtual(MES_INICIAL);

        CartaoCredito cartao = criarCartao(0.0, 0, true, LIMITE_PADRAO);

        cartao.creditar(primeiraCompra);
        cartao.creditar(segundaCompra);

        double fatura = cartao.getFatura();

        System.out.println("Primeira compra: " + primeiraCompra);
        System.out.println("Segunda compra: " + segundaCompra);
        System.out.println("Fatura: " + fatura);

        assertEquals(800.0, fatura, 0.001);
    }

    @Test
    void testDeveChamarMovimentacaoBancariaAoCobrarJuros() throws Exception {

        System.out.println("Teste deve chamar movimentacao bancaria ao cobrar juros");

        double faturaInicial = 500.0;

        Ano.getInstancia().setMesAtual(MES_INICIAL);

        CartaoCredito cartao = spy(criarCartao(faturaInicial, 0, false, LIMITE_PADRAO));

        Ano.getInstancia().setMesAtual(MES_POSTERIOR);

        cartao.cobrarJurus();

        System.out.println("Fatura inicial: " + faturaInicial);
        System.out.println("Movimentacao bancaria deve ser chamada uma vez");

        verify(cartao, times(1)).movimentacaoBancaria(anyDouble());
    }
    
    @Test
    void testNaoDeveAjustarLimiteComValorNegativo() throws Exception {

        System.out.println("Teste nao deve ajustar limite com valor negativo");

        CartaoCredito cartao = criarCartao(0.0, 0, true, LIMITE_PADRAO);

        String entradaUsuario = "-500\n";
        System.setIn(new java.io.ByteArrayInputStream(entradaUsuario.getBytes()));

        cartao.ajustarLimite();

        java.lang.reflect.Field campoLimite = CartaoCredito.class.getDeclaredField("limite");
        campoLimite.setAccessible(true);

        double limiteAtual = campoLimite.getDouble(cartao);

        System.out.println("Limite esperado: " + LIMITE_PADRAO);
        System.out.println("Limite atual: " + limiteAtual);

        assertEquals(LIMITE_PADRAO, limiteAtual, 0.001);
    }
}