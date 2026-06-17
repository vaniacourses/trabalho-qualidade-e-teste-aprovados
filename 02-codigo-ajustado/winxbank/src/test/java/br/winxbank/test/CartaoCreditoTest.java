package br.winxbank.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import br.winxbank.sistemabancario.Banco;
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

    @BeforeEach
    void setUp() {
        Ano.getInstancia().setMesAtual(MES_INICIAL);
        Banco.getInstancia().setReceitas(0.0);
        Banco.getInstancia().setDespesas(0.0);
    }

    @Test
    void testCreditarValorDentroDoLimite() throws Exception {

        System.out.println("Teste creditar valor dentro do limite");

        double valorCompra = 500.0;

        CartaoCredito cartao = criarCartao(0.0, 0, true, LIMITE_PADRAO);

        cartao.creditar(valorCompra, 0, MES_INICIAL);

        double fatura = cartao.getFatura();

        System.out.println("Valor da compra: " + valorCompra);
        System.out.println("Fatura: " + fatura);

        assertEquals(500.0, fatura, 0.001);
        assertEquals(0, cartao.getIndexMesDaFatura());
        assertEquals(MES_INICIAL, cartao.getMesDaFatura());
    }

    @Test
    void testNaoDeveCreditarValorAcimaDoLimite() throws Exception {

        System.out.println("Teste nao deve creditar valor acima do limite");

        double valorCompra = 1200.0;

        CartaoCredito cartao = criarCartao(0.0, 0, true, LIMITE_PADRAO);

        cartao.creditar(valorCompra, 0, MES_INICIAL);

        double fatura = cartao.getFatura();

        System.out.println("Valor da compra: " + valorCompra);
        System.out.println("Fatura: " + fatura);

        assertEquals(0.0, fatura, 0.001);
    }

    @Test
    void testNaoDeveCobrarJurosQuandoFaturaPaga() throws Exception {

        System.out.println("Teste nao deve cobrar juros quando fatura esta paga");

        double faturaInicial = 500.0;

        CartaoCredito cartao = criarCartao(faturaInicial, 0, true, LIMITE_PADRAO);

        cartao.cobrarJuros(1);

        double faturaFinal = cartao.getFatura();

        System.out.println("Fatura inicial: " + faturaInicial);
        System.out.println("Fatura final: " + faturaFinal);

        assertEquals(faturaInicial, faturaFinal, 0.001);
    }

    @Test
    void testNaoDeveCobrarJurosQuandoMesAtualIgualMesDaFatura() throws Exception {

        System.out.println("Teste nao deve cobrar juros quando mes atual e igual ao mes da fatura");

        double faturaInicial = 500.0;

        CartaoCredito cartao = spy(criarCartao(faturaInicial, 0, false, LIMITE_PADRAO));

        doNothing().when(cartao).movimentacaoBancaria(anyDouble());

        cartao.cobrarJuros(0);

        double faturaFinal = cartao.getFatura();

        System.out.println("Fatura inicial: " + faturaInicial);
        System.out.println("Fatura final: " + faturaFinal);
        System.out.println("Movimentacao bancaria nao deve ser chamada");

        assertEquals(faturaInicial, faturaFinal, 0.001);

        verify(cartao, times(0)).movimentacaoBancaria(anyDouble());
    }

    @Test
    void testCreditarValorExatamenteNoLimite() throws Exception {

        System.out.println("Teste creditar valor exatamente no limite");

        double valorCompra = 1000.0;

        CartaoCredito cartao = criarCartao(0.0, 0, true, LIMITE_PADRAO);

        cartao.creditar(valorCompra, 0, MES_INICIAL);

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

        CartaoCredito cartao = criarCartao(0.0, 0, true, LIMITE_PADRAO);

        cartao.creditar(primeiraCompra, 0, MES_INICIAL);
        cartao.creditar(segundaCompra, 0, MES_INICIAL);

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

        CartaoCredito cartao = spy(criarCartao(faturaInicial, 0, false, LIMITE_PADRAO));

        doNothing().when(cartao).movimentacaoBancaria(anyDouble());

        cartao.cobrarJuros(1);

        ArgumentCaptor<Double> captor = ArgumentCaptor.forClass(Double.class);

        verify(cartao, times(1)).movimentacaoBancaria(captor.capture());

        double valorMovimentado = captor.getValue();

        System.out.println("Fatura inicial: " + faturaInicial);
        System.out.println("Valor movimentado: " + valorMovimentado);

        assertTrue(valorMovimentado > 0);
    }

    @Test
    void testNaoDeveAjustarLimiteComValorNegativo() throws Exception {

        System.out.println("Teste nao deve ajustar limite com valor negativo");

        CartaoCredito cartao = criarCartao(0.0, 0, true, LIMITE_PADRAO);

        assertThrows(IllegalArgumentException.class, () -> {
            cartao.ajustarLimite(-500.0);
        });

        double limiteAtual = cartao.getLimite();

        System.out.println("Limite esperado: " + LIMITE_PADRAO);
        System.out.println("Limite atual: " + limiteAtual);

        assertEquals(LIMITE_PADRAO, limiteAtual, 0.001);
    }

    @Test
    void testDeveAjustarLimiteComValorPositivo() throws Exception {

        System.out.println("Teste deve ajustar limite com valor positivo");

        CartaoCredito cartao = criarCartao(0.0, 0, true, LIMITE_PADRAO);

        cartao.ajustarLimite(2000.0);

        double limiteAtual = cartao.getLimite();

        System.out.println("Limite esperado: 2000.0");
        System.out.println("Limite atual: " + limiteAtual);

        assertEquals(2000.0, limiteAtual, 0.001);
    }

    @Test
    void testSetFaturaComValorQueZeraFaturaMarcaComoPaga() throws Exception {

        System.out.println("Teste setFatura com valor que zera fatura marca como paga");

        CartaoCredito cartao = criarCartao(500.0, 0, false, LIMITE_PADRAO);

        cartao.setFatura(-500.0);

        System.out.println("Fatura final: " + cartao.getFatura());
        System.out.println("Fatura paga: " + cartao.isFaturaPaga());

        assertEquals(0.0, cartao.getFatura(), 0.001);
        assertTrue(cartao.isFaturaPaga());
    }
}