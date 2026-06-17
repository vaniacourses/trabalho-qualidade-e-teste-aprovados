package br.winxbank.test;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import br.winxbank.sistemabancario.Banco;
import br.winxbank.sistemabancario.Cartao;
import br.winxbank.sistemabancario.CartaoCredito;
import br.winxbank.sistemabancario.ContaCorrente;
import br.winxbank.sistemaclientes.Cliente;
import br.winxbank.sistemaclientes.ClienteWinx;
import br.winxbank.sistemaclientes.RegistroDeClientes;
import br.winxbank.tempo.Ano;
import java.util.Scanner;

/**
 * Teste funcional (caixa-preta) para RegistroDeClientes.cadastrarCliente.
 *
 * Tecnicas aplicadas:
 * - Particao de equivalencia: CPF valido/duplicado/vazio
 * - Analise de valor limite: saldo = 99999.99, 100000.00, 100000.01
 *
 * Tarefa 5.1.1 - Fernando Rene
 */
public class RegistroDeClientesFuncionalTest {

    private static final int NUMERO_CONTA = 10001;
    private static final int NUMERO_CARTAO = 1234;
    private static final int CSV_PADRAO = 999;

    private RegistroDeClientes registro;
    private InputStream originalIn;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        registro = RegistroDeClientes.getInstancia();
        registro.limparListaDeClientes();
        Ano.getInstancia().setMesAtual("Janeiro");
        originalIn = System.in;
        originalOut = System.out;
    }

    @AfterEach
    void tearDown() {
        System.setIn(originalIn);
        System.setOut(originalOut);
        registro.limparListaDeClientes();
    }

    private ContaCorrente criarContaCorrente(double saldo) {
        Cartao cartao = new Cartao(NUMERO_CARTAO, CSV_PADRAO);
        CartaoCredito cartaoCredito = new CartaoCredito(NUMERO_CARTAO, CSV_PADRAO);
        return new ContaCorrente(NUMERO_CONTA, saldo, cartao, 0, cartaoCredito);
    }

    // ============= PARTICAO DE EQUIVALENCIA: CPF =============

    @Test
    void testCadastrarCliente_CpfValido_CadastraComSucesso() {
        System.out.println("Funcional: CPF valido e unico - deve cadastrar com sucesso");

        ContaCorrente conta = criarContaCorrente(5000.0);

        try (MockedStatic<Banco> bancoMock = Mockito.mockStatic(Banco.class)) {
            Banco banco = mock(Banco.class);
            bancoMock.when(Banco::getInstancia).thenReturn(banco);
            when(banco.abrirNovaConta(any(Scanner.class))).thenReturn(conta);

            System.setIn(new ByteArrayInputStream("Maria\n123.456.789-00\n".getBytes()));
            registro.cadastrarCliente();

            assertEquals(1, registro.getClientes().size(), "CPF valido deve permitir cadastro");
            assertEquals("Maria", registro.getClientes().get(0).getNome());
        }
    }

    @Test
    void testCadastrarCliente_CpfDuplicado_RejeitaCadastro() {
        System.out.println("Funcional: CPF duplicado - deve rejeitar cadastro");

        registro.getClientes().add(new Cliente("Existente", "123.456.789-00"));

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        try (MockedStatic<Banco> bancoMock = Mockito.mockStatic(Banco.class)) {
            Banco banco = mock(Banco.class);
            bancoMock.when(Banco::getInstancia).thenReturn(banco);

            System.setIn(new ByteArrayInputStream("Duplicado\n123.456.789-00\n".getBytes()));
            registro.cadastrarCliente();

            assertEquals(1, registro.getClientes().size(),
                    "CPF duplicado nao deve adicionar novo cliente");
            assertEquals("Existente", registro.getClientes().get(0).getNome(),
                    "Cliente original deve permanecer");
        }

        System.setOut(originalOut);
        assertTrue(outContent.toString().contains("CPF ja existente"),
                "Deve exibir mensagem de rejeicao");
    }

    @Test
    void testCadastrarCliente_CpfVazio_CadastraComSucesso() {
        System.out.println("Funcional: CPF vazio (string vazia) - verifica comportamento");

        ContaCorrente conta = criarContaCorrente(1000.0);

        try (MockedStatic<Banco> bancoMock = Mockito.mockStatic(Banco.class)) {
            Banco banco = mock(Banco.class);
            bancoMock.when(Banco::getInstancia).thenReturn(banco);
            when(banco.abrirNovaConta(any(Scanner.class))).thenReturn(conta);

            System.setIn(new ByteArrayInputStream("Sem CPF\n\n".getBytes()));
            registro.cadastrarCliente();

            // CPF vazio e tratado como valido (checarCpf retorna true pois nenhum
            // cliente existente tem CPF vazio)
            assertEquals(1, registro.getClientes().size(),
                    "CPF vazio e aceito pelo sistema (nao ha validacao de formato)");
        }
    }

    @Test
    void testCadastrarCliente_DoisClientesCpfsDiferentes_AmbosCadastrados() {
        System.out.println("Funcional: Dois clientes com CPFs diferentes - ambos cadastrados");

        ContaCorrente conta1 = criarContaCorrente(3000.0);
        ContaCorrente conta2 = criarContaCorrente(4000.0);

        try (MockedStatic<Banco> bancoMock = Mockito.mockStatic(Banco.class)) {
            Banco banco = mock(Banco.class);
            bancoMock.when(Banco::getInstancia).thenReturn(banco);
            when(banco.abrirNovaConta(any(Scanner.class))).thenReturn(conta1,conta2);

            System.setIn(new ByteArrayInputStream("Alice\n111.111.111-11\n".getBytes()));
            registro.cadastrarCliente();

            System.setIn(new ByteArrayInputStream("Bob\n222.222.222-22\n".getBytes()));
            registro.cadastrarCliente();

            assertEquals(2, registro.getClientes().size(),
                    "CPFs diferentes devem permitir cadastro de ambos");
        }
    }

    // ============= ANALISE DE VALOR LIMITE: SALDO =============

    @Test
    void testCadastrarCliente_SaldoAbaixoDoLimite_99999_99_ClienteNormal() {
        System.out.println("Valor limite: saldo = 99999.99 (abaixo de 100k) -> Cliente normal");

        ContaCorrente conta = criarContaCorrente(99999.99);

        try (MockedStatic<Banco> bancoMock = Mockito.mockStatic(Banco.class)) {
            Banco banco = mock(Banco.class);
            bancoMock.when(Banco::getInstancia).thenReturn(banco);
            when(banco.abrirNovaConta(any(Scanner.class))).thenReturn(conta);

            System.setIn(new ByteArrayInputStream("Quase Winx\n111.111.111-11\n".getBytes()));
            registro.cadastrarCliente();

            assertEquals(1, registro.getClientes().size());
            Cliente cliente = registro.getClientes().get(0);
            assertFalse(cliente instanceof ClienteWinx,
                    "Saldo = 99999.99 nao deve promover a ClienteWinx");
            assertEquals(Cliente.class, cliente.getClass());
        }
    }

    @Test
    void testCadastrarCliente_SaldoExatoNoLimite_100000_ClienteWinx() {
        System.out.println("Valor limite: saldo = 100000.00 (exatamente 100k) -> ClienteWinx");

        ContaCorrente conta = criarContaCorrente(100000.0);

        try (MockedStatic<Banco> bancoMock = Mockito.mockStatic(Banco.class)) {
            Banco banco = mock(Banco.class);
            bancoMock.when(Banco::getInstancia).thenReturn(banco);
            when(banco.abrirNovaConta(any(Scanner.class))).thenReturn(conta);

            System.setIn(new ByteArrayInputStream("Exato Winx\n222.222.222-22\n".getBytes()));
            registro.cadastrarCliente();

            assertEquals(1, registro.getClientes().size());
            Cliente cliente = registro.getClientes().get(0);
            assertTrue(cliente instanceof ClienteWinx,
                    "Saldo = 100000.00 deve promover a ClienteWinx");
        }
    }

    @Test
    void testCadastrarCliente_SaldoAcimaDoLimite_100000_01_ClienteWinx() {
        System.out.println("Valor limite: saldo = 100000.01 (acima de 100k) -> ClienteWinx");

        ContaCorrente conta = criarContaCorrente(100000.01);

        try (MockedStatic<Banco> bancoMock = Mockito.mockStatic(Banco.class)) {
            Banco banco = mock(Banco.class);
            bancoMock.when(Banco::getInstancia).thenReturn(banco);
            when(banco.abrirNovaConta(any(Scanner.class))).thenReturn(conta);

            System.setIn(new ByteArrayInputStream("Super Winx\n333.333.333-33\n".getBytes()));
            registro.cadastrarCliente();

            assertEquals(1, registro.getClientes().size());
            Cliente cliente = registro.getClientes().get(0);
            assertTrue(cliente instanceof ClienteWinx,
                    "Saldo = 100000.01 deve promover a ClienteWinx");
        }
    }

    @Test
    void testCadastrarCliente_SaldoZero_ClienteNormal() {
        System.out.println("Valor limite: saldo = 0.0 -> Cliente normal");

        ContaCorrente conta = criarContaCorrente(0.0);

        try (MockedStatic<Banco> bancoMock = Mockito.mockStatic(Banco.class)) {
            Banco banco = mock(Banco.class);
            bancoMock.when(Banco::getInstancia).thenReturn(banco);
            when(banco.abrirNovaConta(any(Scanner.class))).thenReturn(conta);

            System.setIn(new ByteArrayInputStream("Zero\n444.444.444-44\n".getBytes()));
            registro.cadastrarCliente();

            assertEquals(1, registro.getClientes().size());
            assertFalse(registro.getClientes().get(0) instanceof ClienteWinx);
        }
    }

    @Test
    void testCadastrarCliente_SaldoMuitoAlto_ClienteWinx() {
        System.out.println("Valor limite: saldo = 1000000.0 (muito acima de 100k) -> ClienteWinx");

        ContaCorrente conta = criarContaCorrente(1000000.0);

        try (MockedStatic<Banco> bancoMock = Mockito.mockStatic(Banco.class)) {
            Banco banco = mock(Banco.class);
            bancoMock.when(Banco::getInstancia).thenReturn(banco);
            when(banco.abrirNovaConta(any(Scanner.class))).thenReturn(conta);

            System.setIn(new ByteArrayInputStream("Milionario\n555.555.555-55\n".getBytes()));
            registro.cadastrarCliente();

            assertTrue(registro.getClientes().get(0) instanceof ClienteWinx);
        }
    }

    // ============= COMBINACAO: CPF + SALDO =============

    @Test
    void testCadastrarCliente_CpfDuplicadoComSaldoAlto_RejeitaCadastro() {
        System.out.println("Funcional: CPF duplicado com saldo alto - deve rejeitar mesmo com saldo >= 100k");

        registro.getClientes().add(new Cliente("Original", "111.111.111-11"));

        try (MockedStatic<Banco> bancoMock = Mockito.mockStatic(Banco.class)) {
            Banco banco = mock(Banco.class);
            bancoMock.when(Banco::getInstancia).thenReturn(banco);

            System.setIn(new ByteArrayInputStream("Duplicado Rico\n111.111.111-11\n".getBytes()));
            registro.cadastrarCliente();

            assertEquals(1, registro.getClientes().size(),
                    "CPF duplicado deve ser rejeitado independente do saldo");
            assertEquals("Original", registro.getClientes().get(0).getNome());
        }
    }
}
