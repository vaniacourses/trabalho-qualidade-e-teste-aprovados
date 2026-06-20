package br.winxbank.test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import br.winxbank.sistemabancario.Banco;
import br.winxbank.sistemabancario.Cartao;
import br.winxbank.sistemabancario.CartaoCredito;
import br.winxbank.sistemabancario.ContaCorrente;
import br.winxbank.sistemabancario.ContaPoupanca;
import br.winxbank.sistemabancario.Movimentacao;
import br.winxbank.sistemaclientes.Cliente;
import br.winxbank.sistemaclientes.ClienteWinx;
import br.winxbank.sistemaclientes.RegistroDeClientes;
import br.winxbank.tempo.Ano;

/**
 * Testes unitarios para RegistroDeClientes.
 * Isola dependencias externas: Banco.getInstancia() e mockado via Mockito.mockStatic,
 * e chamadas de Scanner sao isoladas via ByteArrayInputStream em System.in.
 *
 * Tarefa 1.1 - Fernando Rene
 */
public class RegistroDeClientesTest {

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

    private ContaPoupanca criarContaPoupanca(double saldo) {
        Cartao cartao = new Cartao(NUMERO_CARTAO, CSV_PADRAO);
        return new ContaPoupanca(NUMERO_CONTA, saldo, cartao, 0);
    }

    private void simularEntrada(String input) {
        System.setIn(new ByteArrayInputStream(input.getBytes()));
    }

    // ===================== cadastrarCliente =====================

    @Test
    void testCadastrarClienteNormalComContaCorrente() {
        System.out.println("Teste cadastrar cliente normal com conta corrente (saldo < 100k)");

        ContaCorrente conta = criarContaCorrente(5000.0);

        try (MockedStatic<Banco> bancoMock = Mockito.mockStatic(Banco.class)) {
            Banco banco = mock(Banco.class);
            bancoMock.when(Banco::getInstancia).thenReturn(banco);
            when(banco.abrirNovaConta(any(Scanner.class))).thenReturn(conta);

            simularEntrada("Maria\n123.456.789-00\n");
            registro.cadastrarCliente();

            assertEquals(1, registro.getClientes().size());
            Cliente clienteCadastrado = registro.getClientes().get(0);
            assertEquals("Maria", clienteCadastrado.getNome());
            assertEquals("123.456.789-00", clienteCadastrado.getCpf());
            assertFalse(clienteCadastrado instanceof ClienteWinx);
        }
    }

    @Test
    void testCadastrarClienteWinxComSaldoAlto() {
        System.out.println("Teste cadastrar cliente com saldo >= 100k promove a ClienteWinx");

        ContaCorrente conta = criarContaCorrente(150000.0);

        try (MockedStatic<Banco> bancoMock = Mockito.mockStatic(Banco.class)) {
            Banco banco = mock(Banco.class);
            bancoMock.when(Banco::getInstancia).thenReturn(banco);
            when(banco.abrirNovaConta(any(Scanner.class))).thenReturn(conta);

            simularEntrada("Carlos\n999.888.777-66\n");
            registro.cadastrarCliente();

            assertEquals(1, registro.getClientes().size());
            Cliente clienteCadastrado = registro.getClientes().get(0);
            assertTrue(clienteCadastrado instanceof ClienteWinx);
            assertEquals("Carlos", clienteCadastrado.getNome());
            assertEquals(0, ((ClienteWinx) clienteCadastrado).getPontosDeCompra());
        }
    }

    @Test
    void testCadastrarClienteWinxComSaldoExato100k() {
        System.out.println("Teste cadastrar cliente com saldo exato de 100000 promove a ClienteWinx");

        ContaCorrente conta = criarContaCorrente(100000.0);

        try (MockedStatic<Banco> bancoMock = Mockito.mockStatic(Banco.class)) {
            Banco banco = mock(Banco.class);
            bancoMock.when(Banco::getInstancia).thenReturn(banco);
            when(banco.abrirNovaConta(any(Scanner.class))).thenReturn(conta);

            simularEntrada("Ana\n111.222.333-44\n");
            registro.cadastrarCliente();

            assertEquals(1, registro.getClientes().size());
            assertTrue(registro.getClientes().get(0) instanceof ClienteWinx);
        }
    }

    @Test
    void testCadastrarClienteComContaPoupanca() {
        System.out.println("Teste cadastrar cliente com conta poupanca registra informe de rendimento");

        ContaPoupanca conta = criarContaPoupanca(3000.0);

        try (MockedStatic<Banco> bancoMock = Mockito.mockStatic(Banco.class)) {
            Banco banco = mock(Banco.class);
            bancoMock.when(Banco::getInstancia).thenReturn(banco);
            when(banco.abrirNovaConta(any(Scanner.class))).thenReturn(conta);

            simularEntrada("Pedro\n555.666.777-88\n");
            registro.cadastrarCliente();

            assertEquals(1, registro.getClientes().size());
            assertFalse(conta.getInformeRendimento().isEmpty(),
                    "Conta poupanca deve ter informe de rendimento registrado");
        }
    }

    @Test
    void testCadastrarClienteWinxComContaPoupanca() {
        System.out.println("Teste cadastrar ClienteWinx com conta poupanca");

        ContaPoupanca conta = criarContaPoupanca(200000.0);

        try (MockedStatic<Banco> bancoMock = Mockito.mockStatic(Banco.class)) {
            Banco banco = mock(Banco.class);
            bancoMock.when(Banco::getInstancia).thenReturn(banco);
            when(banco.abrirNovaConta(any(Scanner.class))).thenReturn(conta);

            simularEntrada("Rica\n888.999.000-11\n");
            registro.cadastrarCliente();

            assertEquals(1, registro.getClientes().size());
            assertTrue(registro.getClientes().get(0) instanceof ClienteWinx);
            assertFalse(conta.getInformeRendimento().isEmpty());
        }
    }

    @Test
    void testCadastrarClienteCpfDuplicadoNaoAdiciona() {
        System.out.println("Teste cadastrar cliente com CPF duplicado nao adiciona ao registro");

        // Adicionar cliente existente
        Cliente existente = new Cliente("Existente", "123.456.789-00");
        registro.getClientes().add(existente);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        try (MockedStatic<Banco> bancoMock = Mockito.mockStatic(Banco.class)) {
            Banco banco = mock(Banco.class);
            bancoMock.when(Banco::getInstancia).thenReturn(banco);

            simularEntrada("Duplicado\n123.456.789-00\n");
            registro.cadastrarCliente();

            assertEquals(1, registro.getClientes().size(),
                    "Nao deve adicionar cliente com CPF duplicado");
            verify(banco, never()).abrirNovaConta(any(java.util.Scanner.class));
        }

        System.setOut(originalOut);
        String saida = outContent.toString();
        assertTrue(saida.contains("CPF ja existente"),
                "Deve exibir mensagem de CPF existente");
    }

    @Test
    void testCadastrarPrimeiroClienteListaVazia() {
        System.out.println("Teste cadastrar primeiro cliente com lista vazia");

        assertTrue(registro.getClientes().isEmpty());

        ContaCorrente conta = criarContaCorrente(1000.0);

        try (MockedStatic<Banco> bancoMock = Mockito.mockStatic(Banco.class)) {
            Banco banco = mock(Banco.class);
            bancoMock.when(Banco::getInstancia).thenReturn(banco);
            when(banco.abrirNovaConta(any(Scanner.class))).thenReturn(conta);

            simularEntrada("Primeiro\n000.000.000-00\n");
            registro.cadastrarCliente();

            assertEquals(1, registro.getClientes().size());
            assertEquals("Primeiro", registro.getClientes().get(0).getNome());
        }
    }

    @Test
    void testCadastrarClienteRegistraMovimentacaoNoExtrato() {
        System.out.println("Teste cadastrar cliente registra movimentacao ENTRADA no extrato");

        double saldoInicial = 7500.0;
        ContaCorrente conta = criarContaCorrente(saldoInicial);

        try (MockedStatic<Banco> bancoMock = Mockito.mockStatic(Banco.class)) {
            Banco banco = mock(Banco.class);
            bancoMock.when(Banco::getInstancia).thenReturn(banco);
            when(banco.abrirNovaConta(any(Scanner.class))).thenReturn(conta);

            simularEntrada("Joao\n111.111.111-11\n");
            registro.cadastrarCliente();

            assertFalse(conta.getExtrato().isEmpty(), "Extrato deve conter movimentacao");
            Movimentacao mov = conta.getExtrato().get(0);
            assertEquals(saldoInicial, mov.getDinheiroMovimentado(), 0.001);
            assertEquals(Movimentacao.TipoDaMovimentacao.ENTRADA, mov.getTipoDaMovimentacao());
        }
    }

    @Test
    void testCadastrarMultiplosClientes() {
        System.out.println("Teste cadastrar multiplos clientes com CPFs diferentes");

        ContaCorrente conta1 = criarContaCorrente(1000.0);
        ContaCorrente conta2 = criarContaCorrente(2000.0);

        try (MockedStatic<Banco> bancoMock = Mockito.mockStatic(Banco.class)) {
            Banco banco = mock(Banco.class);
            bancoMock.when(Banco::getInstancia).thenReturn(banco);
            when(banco.abrirNovaConta(any(Scanner.class))).thenReturn(conta1, conta2);

            simularEntrada("Alice\n111.111.111-11\n");
            registro.cadastrarCliente();

            simularEntrada("Bob\n222.222.222-22\n");
            registro.cadastrarCliente();

            assertEquals(2, registro.getClientes().size());
        }
    }

    // ===================== checarCpf =====================

    @Test
    void testChecarCpfDisponivel() {
        System.out.println("Teste checarCpf com CPF disponivel retorna true");

        registro.getClientes().add(new Cliente("Existente", "111.111.111-11"));

        boolean resultado = registro.checarCpf("222.222.222-22");

        assertTrue(resultado, "CPF nao existente deve retornar true (disponivel)");
    }

    @Test
    void testChecarCpfDuplicado() {
        System.out.println("Teste checarCpf com CPF duplicado retorna false");

        registro.getClientes().add(new Cliente("Existente", "111.111.111-11"));

        boolean resultado = registro.checarCpf("111.111.111-11");

        assertFalse(resultado, "CPF existente deve retornar false (indisponivel)");
    }

    @Test
    void testChecarCpfListaVazia() {
        System.out.println("Teste checarCpf com lista vazia retorna true");

        assertTrue(registro.getClientes().isEmpty());

        boolean resultado = registro.checarCpf("qualquer-cpf");

        assertTrue(resultado, "Lista vazia deve retornar true (CPF disponivel)");
    }

    @Test
    void testChecarCpfMultiplosClientes() {
        System.out.println("Teste checarCpf com multiplos clientes");

        registro.getClientes().add(new Cliente("A", "111.111.111-11"));
        registro.getClientes().add(new Cliente("B", "222.222.222-22"));
        registro.getClientes().add(new Cliente("C", "333.333.333-33"));

        assertTrue(registro.checarCpf("444.444.444-44"));
        assertFalse(registro.checarCpf("222.222.222-22"));
    }

    // ===================== retornarCliente =====================

    @Test
    void testRetornarClienteComCpfExistente() {
        System.out.println("Teste retornarCliente com CPF existente");

        Cliente esperado = new Cliente("Maria", "123.456.789-00");
        registro.getClientes().add(esperado);

        Cliente resultado = registro.retornarCliente("123.456.789-00");

        assertNotNull(resultado);
        assertEquals("Maria", resultado.getNome());
        assertEquals("123.456.789-00", resultado.getCpf());
    }

    @Test
    void testRetornarClienteComCpfInexistente() {
        System.out.println("Teste retornarCliente com CPF inexistente retorna null");

        registro.getClientes().add(new Cliente("Maria", "123.456.789-00"));

        Cliente resultado = registro.retornarCliente("999.999.999-99");

        assertNull(resultado, "CPF inexistente deve retornar null");
    }

    @Test
    void testRetornarClienteComListaVazia() {
        System.out.println("Teste retornarCliente com lista vazia retorna null");

        Cliente resultado = registro.retornarCliente("qualquer-cpf");

        assertNull(resultado, "Lista vazia deve retornar null");
    }

    @Test
    void testRetornarClienteWinx() {
        System.out.println("Teste retornarCliente retorna ClienteWinx corretamente");

        ClienteWinx winx = new ClienteWinx("Winx", "999.888.777-66", 10);
        registro.getClientes().add(winx);

        Cliente resultado = registro.retornarCliente("999.888.777-66");

        assertNotNull(resultado);
        assertTrue(resultado instanceof ClienteWinx);
        assertEquals(10, ((ClienteWinx) resultado).getPontosDeCompra());
    }

    // ===================== atualizarCliente =====================

    @Test
    void testAtualizarClienteExistente() throws InterruptedException {
        System.out.println("Teste atualizarCliente atualiza cliente existente");

        Cliente original = new Cliente("Original", "123.456.789-00");
        registro.getClientes().add(original);

        Cliente atualizado = new Cliente("Atualizado", "123.456.789-00");
        ContaCorrente conta = criarContaCorrente(5000.0);
        atualizado.setContas(conta);

        registro.atualizarCliente(atualizado);

        Cliente encontrado = registro.retornarCliente("123.456.789-00");
        assertNotNull(encontrado);
        assertEquals("Atualizado", encontrado.getNome());
        assertFalse(encontrado.getContas().isEmpty(),
                "Cliente atualizado deve ter contas");
    }

    @Test
    void testAtualizarClienteNaoExistente() throws InterruptedException {
        System.out.println("Teste atualizarCliente com cliente nao existente nao altera lista");

        registro.getClientes().add(new Cliente("Existente", "111.111.111-11"));

        Cliente naoExistente = new Cliente("Fantasma", "999.999.999-99");
        registro.atualizarCliente(naoExistente);

        assertEquals(1, registro.getClientes().size());
        assertEquals("Existente", registro.getClientes().get(0).getNome());
    }

    // ===================== removerCliente =====================

    @Test
    void testRemoverClienteExistente() {
        System.out.println("Teste removerCliente remove cliente existente");

        Cliente cliente = new Cliente("ARemover", "123.456.789-00");
        registro.getClientes().add(cliente);

        assertEquals(1, registro.getClientes().size());

        registro.removerCliente(cliente);

        assertEquals(0, registro.getClientes().size());
    }

    @Test
    void testRemoverClienteNaoExistente() {
        System.out.println("Teste removerCliente com cliente nao existente nao altera lista");

        registro.getClientes().add(new Cliente("Permanece", "111.111.111-11"));

        Cliente naoExistente = new Cliente("Fantasma", "999.999.999-99");
        registro.removerCliente(naoExistente);

        assertEquals(1, registro.getClientes().size());
        assertEquals("Permanece", registro.getClientes().get(0).getNome());
    }

    @Test
    void testRemoverUnicoCliente() {
        System.out.println("Teste remover unico cliente da lista");

        Cliente unico = new Cliente("Unico", "000.000.000-00");
        registro.getClientes().add(unico);

        registro.removerCliente(unico);

        assertTrue(registro.getClientes().isEmpty());
    }

    // ===================== Singleton e utilitarios =====================

    @Test
    void testGetInstanciaRetornaMesmaInstancia() {
        System.out.println("Teste getInstancia retorna mesma instancia (Singleton)");

        RegistroDeClientes instancia1 = RegistroDeClientes.getInstancia();
        RegistroDeClientes instancia2 = RegistroDeClientes.getInstancia();

        assertSame(instancia1, instancia2);
    }

    @Test
    void testLimparListaDeClientes() {
        System.out.println("Teste limparListaDeClientes esvazia a lista");

        registro.getClientes().add(new Cliente("A", "111"));
        registro.getClientes().add(new Cliente("B", "222"));
        assertEquals(2, registro.getClientes().size());

        registro.limparListaDeClientes();

        assertTrue(registro.getClientes().isEmpty());
    }

    @Test
    void testSetClientesAdicionaColecao() {
        System.out.println("Teste setClientes adiciona colecao inteira");

        java.util.ArrayList<Cliente> novosClientes = new java.util.ArrayList<>();
        novosClientes.add(new Cliente("A", "111"));
        novosClientes.add(new Cliente("B", "222"));

        registro.setClientes(novosClientes);

        assertEquals(2, registro.getClientes().size());
    }

    // ===================== visualizarDetalhesDoCliente =====================

    @Test
    void testVisualizarDetalhesClienteNormal() {
        System.out.println("Teste visualizarDetalhesDoCliente com cliente normal");

        Cliente cliente = new Cliente("Maria", "123.456.789-00");
        ContaCorrente conta = criarContaCorrente(5000.0);
        cliente.setContas(conta);
        registro.getClientes().add(cliente);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        registro.visualizarDetalhesDoCliente("123.456.789-00");

        System.setOut(originalOut);
        String saida = outContent.toString();

        assertTrue(saida.contains("Maria"), "Deve exibir o nome do cliente");
        assertTrue(saida.contains("123.456.789-00"), "Deve exibir o CPF");
    }

    @Test
    void testVisualizarDetalhesClienteWinx() {
        System.out.println("Teste visualizarDetalhesDoCliente com ClienteWinx");

        ClienteWinx winx = new ClienteWinx("Carlos", "999.888.777-66", 15);
        ContaCorrente conta = criarContaCorrente(200000.0);
        winx.setContas(conta);
        registro.getClientes().add(winx);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        registro.visualizarDetalhesDoCliente("999.888.777-66");

        System.setOut(originalOut);
        String saida = outContent.toString();

        assertTrue(saida.contains("Carlos"), "Deve exibir o nome");
        assertTrue(saida.contains("Pontos"), "Deve exibir pontos de compra");
    }

    @Test
    void testVisualizarDetalhesCpfInexistente() {
        System.out.println("Teste visualizarDetalhesDoCliente com CPF inexistente nao imprime nada");

        registro.getClientes().add(new Cliente("Alguem", "111.111.111-11"));

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        registro.visualizarDetalhesDoCliente("999.999.999-99");

        System.setOut(originalOut);
        String saida = outContent.toString();

        assertTrue(saida.isEmpty(), "Nao deve imprimir nada para CPF inexistente");
    }

    // ===================== visualizarContas =====================

    @Test
    void testVisualizarContasContaCorrente() {
        System.out.println("Teste visualizarContas com conta corrente");

        Cliente cliente = new Cliente("Teste", "000.000.000-00");
        ContaCorrente conta = criarContaCorrente(1000.0);
        cliente.setContas(conta);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        registro.visualizarContas(cliente);

        System.setOut(originalOut);
        String saida = outContent.toString();

        assertTrue(saida.contains("Corrente"), "Deve exibir tipo Corrente");
        assertTrue(saida.contains("Cartao Credito"), "Deve exibir cartao de credito");
    }

    @Test
    void testVisualizarContasContaPoupanca() {
        System.out.println("Teste visualizarContas com conta poupanca");

        Cliente cliente = new Cliente("Teste", "000.000.000-00");
        ContaPoupanca conta = criarContaPoupanca(2000.0);
        cliente.setContas(conta);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        registro.visualizarContas(cliente);

        System.setOut(originalOut);
        String saida = outContent.toString();

        assertTrue(saida.contains("Poupanca"), "Deve exibir tipo Poupanca");
    }

    // ===================== printarListaDeClientes =====================

    @Test
    void testPrintarListaDeClientes() {
        System.out.println("Teste printarListaDeClientes exibe todos os clientes");

        Cliente c1 = new Cliente("Alice", "111.111.111-11");
        c1.setContas(criarContaCorrente(1000.0));
        ClienteWinx c2 = new ClienteWinx("Bob", "222.222.222-22", 5);
        c2.setContas(criarContaPoupanca(200000.0));

        registro.getClientes().add(c1);
        registro.getClientes().add(c2);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        registro.printarListaDeClientes();

        System.setOut(originalOut);
        String saida = outContent.toString();

        assertTrue(saida.contains("Alice"), "Deve listar Alice");
        assertTrue(saida.contains("Bob"), "Deve listar Bob");
        assertTrue(saida.contains("Clientes"), "Deve conter cabecalho");
    }

    @Test
    void testPrintarListaVazia() {
        System.out.println("Teste printarListaDeClientes com lista vazia");

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        registro.printarListaDeClientes();

        System.setOut(originalOut);
        String saida = outContent.toString();

        assertTrue(saida.contains("Clientes"), "Deve imprimir cabecalho mesmo com lista vazia");
    }
}
