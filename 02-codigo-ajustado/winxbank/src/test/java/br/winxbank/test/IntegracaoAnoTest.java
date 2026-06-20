package br.winxbank.test;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.winxbank.sistemabancario.Banco;
import br.winxbank.sistemabancario.Cartao;
import br.winxbank.sistemabancario.CartaoCredito;
import br.winxbank.sistemabancario.ContaCorrente;
import br.winxbank.sistemaclientes.Cliente;
import br.winxbank.sistemaclientes.RegistroDeClientes;
import br.winxbank.tempo.Ano;

public class IntegracaoAnoTest {

    @BeforeEach
    void setUp() throws Exception {

        Ano.getInstancia().setMesAtual("Janeiro");

        RegistroDeClientes.getInstancia().limparListaDeClientes();

        Banco.getInstancia().setReceitas(0);
        Banco.getInstancia().setDespesas(0);

        Field field = Ano.class.getDeclaredField("count");
        field.setAccessible(true);
        field.setLong(Ano.getInstancia(), 0L);
    }

    @Test
    void testMudancaDeMesDisparaMovimentacaoBancaria() {

        Cliente cliente = new Cliente("Joao", "123.456.789-00");

        Cartao cartao = new Cartao(1111, 123);
        CartaoCredito cc = new CartaoCredito(1111, 123);

        ContaCorrente conta =
                new ContaCorrente(1, 1000.0, cartao, 0, cc);

        cliente.setContas(conta);

        RegistroDeClientes.getInstancia()
                .getClientes()
                .add(cliente);

        double saldoInicial = conta.getSaldo();

        int mesInicial =
                Ano.getInstancia().getIndexMesAtual();

        while (Ano.getInstancia().getIndexMesAtual()
                == mesInicial) {

            Ano.getInstancia().fazerMesPassar();
        }

        assertEquals(
                (mesInicial + 1) % 12,
                Ano.getInstancia().getIndexMesAtual()
        );

        assertTrue(conta.getSaldo() < saldoInicial);

        assertTrue(
                Banco.getInstancia().getReceitas() > 0
        );
    }

    @Test
    void testNaoDeveMovimentarBancoAntesDeCincoInteracoes()
            throws Exception {

        Cliente cliente = new Cliente("Joao", "123.456.789-00");

        Cartao cartao = new Cartao(1111, 123);
        CartaoCredito cc = new CartaoCredito(1111, 123);

        ContaCorrente conta =
                new ContaCorrente(1, 1000.0, cartao, 0, cc);

        cliente.setContas(conta);

        RegistroDeClientes.getInstancia()
                .getClientes()
                .add(cliente);

        Field field = Ano.class.getDeclaredField("count");
        field.setAccessible(true);

        field.setLong(Ano.getInstancia(), 1L);

        double saldoInicial = conta.getSaldo();

        double receitaInicial =
                Banco.getInstancia().getReceitas();

        int mesInicial =
                Ano.getInstancia().getIndexMesAtual();

        Ano.getInstancia().fazerMesPassar();

        assertEquals(
                mesInicial,
                Ano.getInstancia().getIndexMesAtual()
        );

        assertEquals(
                saldoInicial,
                conta.getSaldo(),
                0.001
        );

        assertEquals(
                receitaInicial,
                Banco.getInstancia().getReceitas(),
                0.001
        );
    }

    @Test
    void testWrapAroundMantemMovimentacaoBancaria() {

        Ano.getInstancia().setMesAtual("Dezembro");

        Cliente cliente = new Cliente("Joao", "123.456.789-00");

        Cartao cartao = new Cartao(1111, 123);
        CartaoCredito cc = new CartaoCredito(1111, 123);

        ContaCorrente conta =
                new ContaCorrente(1, 1000.0, cartao, 0, cc);

        cliente.setContas(conta);

        RegistroDeClientes.getInstancia()
                .getClientes()
                .add(cliente);

        double saldoInicial = conta.getSaldo();

        while (Ano.getInstancia().getIndexMesAtual() == 11) {

            Ano.getInstancia().fazerMesPassar();
        }

        assertEquals(
                0,
                Ano.getInstancia().getIndexMesAtual()
        );

        assertEquals(
                "Janeiro",
                Ano.getInstancia().getMesAtual()
        );

        assertTrue(
                conta.getSaldo() < saldoInicial
        );

        assertTrue(
                Banco.getInstancia().getReceitas() > 0
        );
    }
}