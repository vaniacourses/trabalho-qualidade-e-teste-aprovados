package br.winxbank.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import br.winxbank.sistemabancario.CartaoCredito;

public class CartaoCreditoFuncionalTest {

    private static final double LIMITE_PADRAO = 1000.0;
    private static final int NUMERO_PADRAO = 1234;
    private static final int CSV_PADRAO = 999;
    private static final String MES_INICIAL = "Janeiro";
    private static final int INDEX_MES_INICIAL = 0;

    private CartaoCredito criarCartaoComFaturaZerada() {
        return new CartaoCredito(0.0, INDEX_MES_INICIAL, true, LIMITE_PADRAO, NUMERO_PADRAO, CSV_PADRAO);
    }

    @Test
    void cenario1CompraDentroDoLimiteDeveAtualizarFatura() {

        System.out.println("Cenario 1 - Compra dentro do limite");

        CartaoCredito cartao = criarCartaoComFaturaZerada();

        assertEquals(0.0, cartao.getFatura(), 0.001);

        cartao.creditar(500.0, INDEX_MES_INICIAL, MES_INICIAL);

        double faturaAtual = cartao.getFatura();

        System.out.println("Valor da compra: 500.0");
        System.out.println("Fatura esperada: 500.0");
        System.out.println("Fatura atual: " + faturaAtual);

        assertEquals(500.0, faturaAtual, 0.001);
    }

    @Test
    void cenario2CompraExatamenteNoLimiteDeveAtualizarFatura() {

        System.out.println("Cenario 2 - Compra exatamente no limite");

        CartaoCredito cartao = criarCartaoComFaturaZerada();

        assertEquals(0.0, cartao.getFatura(), 0.001);

        cartao.creditar(1000.0, INDEX_MES_INICIAL, MES_INICIAL);

        double faturaAtual = cartao.getFatura();

        System.out.println("Valor da compra: 1000.0");
        System.out.println("Fatura esperada: 1000.0");
        System.out.println("Fatura atual: " + faturaAtual);

        assertEquals(1000.0, faturaAtual, 0.001);
    }

    @Test
    void cenario3CompraAcimaDoLimiteDeveSerRejeitada() {

        System.out.println("Cenario 3 - Compra acima do limite");

        CartaoCredito cartao = criarCartaoComFaturaZerada();

        assertEquals(0.0, cartao.getFatura(), 0.001);

        cartao.creditar(1000.01, INDEX_MES_INICIAL, MES_INICIAL);

        double faturaAtual = cartao.getFatura();

        System.out.println("Valor da compra: 1000.01");
        System.out.println("Fatura esperada: 0.0");
        System.out.println("Fatura atual: " + faturaAtual);

        assertEquals(0.0, faturaAtual, 0.001);
    }

    @Test
    void cenario4ComprasAcumuladasAtingindoLimiteDevemSerAceitas() {

        System.out.println("Cenario 4 - Compras acumuladas atingindo o limite");

        CartaoCredito cartao = criarCartaoComFaturaZerada();

        assertEquals(0.0, cartao.getFatura(), 0.001);

        cartao.creditar(800.0, INDEX_MES_INICIAL, MES_INICIAL);

        assertEquals(800.0, cartao.getFatura(), 0.001);

        cartao.creditar(200.0, INDEX_MES_INICIAL, MES_INICIAL);

        double faturaAtual = cartao.getFatura();

        System.out.println("Primeira compra: 800.0");
        System.out.println("Segunda compra: 200.0");
        System.out.println("Fatura esperada: 1000.0");
        System.out.println("Fatura atual: " + faturaAtual);

        assertEquals(1000.0, faturaAtual, 0.001);
    }

    @Test
    void cenario5ComprasAcumuladasUltrapassandoLimiteDevemRejeitarSegundaCompra() {

        System.out.println("Cenario 5 - Compras acumuladas ultrapassando o limite");

        CartaoCredito cartao = criarCartaoComFaturaZerada();

        assertEquals(0.0, cartao.getFatura(), 0.001);

        cartao.creditar(800.0, INDEX_MES_INICIAL, MES_INICIAL);

        assertEquals(800.0, cartao.getFatura(), 0.001);

        cartao.creditar(200.01, INDEX_MES_INICIAL, MES_INICIAL);

        double faturaAtual = cartao.getFatura();

        System.out.println("Primeira compra: 800.0");
        System.out.println("Segunda compra: 200.01");
        System.out.println("Fatura esperada: 800.0");
        System.out.println("Fatura atual: " + faturaAtual);

        assertEquals(800.0, faturaAtual, 0.001);
    }
}