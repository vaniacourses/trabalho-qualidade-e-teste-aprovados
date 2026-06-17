package br.winxbank.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.winxbank.sistemabancario.Banco;
import br.winxbank.sistemabancario.CartaoCredito;
import br.winxbank.tempo.Ano;

public class IntegracaoCartaoCreditoTest {

    private static final double LIMITE_PADRAO = 1000.0;
    private static final int NUMERO_PADRAO = 1234;
    private static final int CSV_PADRAO = 999;

    @BeforeEach
    void setUp() {
        Banco.getInstancia().setReceitas(0.0);
        Banco.getInstancia().setDespesas(0.0);
        Ano.getInstancia().setMesAtual("Janeiro");
    }

    @Test
    void testCobrarJurosDeveGerarReceitaNoBanco() throws Exception {

        System.out.println("Teste de integração: CartaoCredito deve gerar receita no Banco ao cobrar juros");

        double faturaInicial = 500.0;

        CartaoCredito cartao = new CartaoCredito(
                faturaInicial,
                0,
                false,
                LIMITE_PADRAO,
                NUMERO_PADRAO,
                CSV_PADRAO
        );

        double receitaInicialBanco = Banco.getInstancia().getReceitas();

        Ano.getInstancia().setMesAtual("Fevereiro");

        cartao.cobrarJuros();

        double faturaFinal = cartao.getFatura();
        double receitaFinalBanco = Banco.getInstancia().getReceitas();

        System.out.println("Fatura inicial: " + faturaInicial);
        System.out.println("Fatura final: " + faturaFinal);
        System.out.println("Receita inicial do banco: " + receitaInicialBanco);
        System.out.println("Receita final do banco: " + receitaFinalBanco);

        assertTrue(faturaFinal > faturaInicial);
        assertTrue(receitaFinalBanco > receitaInicialBanco);
    }
    
    @Test
    void testCompraNoCreditoECobrancaDeJurosDeveGerarReceitaNoBanco() throws Exception {

        System.out.println("Teste de integração: compra no crédito e cobrança de juros devem gerar receita no Banco");

        double valorCompra = 500.0;

        Ano.getInstancia().setMesAtual("Janeiro");

        CartaoCredito cartao = new CartaoCredito(
                0.0,
                0,
                true,
                LIMITE_PADRAO,
                NUMERO_PADRAO,
                CSV_PADRAO
        );

        cartao.creditar(valorCompra);

        double faturaAposCompra = cartao.getFatura();

        System.out.println("Valor da compra: " + valorCompra);
        System.out.println("Fatura após compra: " + faturaAposCompra);

        assertEquals(valorCompra, faturaAposCompra, 0.001);

        double receitaInicialBanco = Banco.getInstancia().getReceitas();

        Ano.getInstancia().setMesAtual("Fevereiro");

        cartao.cobrarJuros();

        double faturaFinal = cartao.getFatura();
        double receitaFinalBanco = Banco.getInstancia().getReceitas();
        double jurosGerados = faturaFinal - faturaAposCompra;
        double receitaGeradaNoBanco = receitaFinalBanco - receitaInicialBanco;

        System.out.println("Fatura após juros: " + faturaFinal);
        System.out.println("Juros gerados: " + jurosGerados);
        System.out.println("Receita inicial do banco: " + receitaInicialBanco);
        System.out.println("Receita final do banco: " + receitaFinalBanco);
        System.out.println("Receita gerada no banco: " + receitaGeradaNoBanco);

        assertTrue(faturaFinal > faturaAposCompra);
        assertTrue(receitaFinalBanco > receitaInicialBanco);
        assertEquals(jurosGerados, receitaGeradaNoBanco, 0.001);
    }
    
}

