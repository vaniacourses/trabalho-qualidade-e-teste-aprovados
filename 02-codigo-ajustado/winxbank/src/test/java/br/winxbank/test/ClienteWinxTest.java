package br.winxbank.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import br.winxbank.sistemabancario.Cartao;
import br.winxbank.sistemabancario.ContaPoupanca;
import br.winxbank.sistemaclientes.ClienteWinx;

public class ClienteWinxTest {

    private static final int NUMERO_CONTA = 10001;
    private static final int NUMERO_CARTAO = 1234;
    private static final int CSV_PADRAO = 999;

    @Test
    void testConverterPontosEmSaldoAdicionaValorCorretoNaConta() {
        System.out.println("Teste converter pontos em saldo adiciona valor correto na conta");

        int pontosIniciais = 6;
        ClienteWinx cliente = new ClienteWinx("Ana", "111.111.111-11", pontosIniciais);

        Cartao cartao = new Cartao(NUMERO_CARTAO, CSV_PADRAO);
        ContaPoupanca conta = new ContaPoupanca(NUMERO_CONTA, 0.0, cartao, 0.0);

        cliente.converterPontosEmSaldo(conta);

        double saldoEsperado = 18.0; // 6 pontos × 3 = R$ 18,00

        double saldoFinal = conta.getSaldo();
        int pontosFinal = cliente.getPontosDeCompra();

        System.out.println("Pontos iniciais: " + pontosIniciais);
        System.out.println("Saldo esperado: " + saldoEsperado + " | Saldo final: " + saldoFinal);
        System.out.println("Pontos apos conversao: " + pontosFinal);

        assertEquals(saldoEsperado, saldoFinal, 0.001);
        assertEquals(0, pontosFinal);
    }

    @Test
    void testObterPontosDeCompraIncrementaPontosEmUm() {
        System.out.println("Teste obter pontos de compra incrementa pontos em um");

        int pontosIniciais = 4;
        ClienteWinx cliente = new ClienteWinx("Carlos", "222.222.222-22", pontosIniciais);

        cliente.obterPontosDeCompra();

        int pontosFinal = cliente.getPontosDeCompra();

        System.out.println("Pontos iniciais: " + pontosIniciais);
        System.out.println("Pontos esperados: " + (pontosIniciais + 1) + " | Pontos finais: " + pontosFinal);

        assertEquals(pontosIniciais + 1, pontosFinal);
    }
}
