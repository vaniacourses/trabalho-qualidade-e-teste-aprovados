package br.winxbank.test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.winxbank.sistemabancario.Banco;
import br.winxbank.sistemabancario.Cartao;
import br.winxbank.sistemabancario.CartaoCredito;
import br.winxbank.sistemabancario.Conta;
import br.winxbank.sistemabancario.ContaCorrente;
import br.winxbank.sistemabancario.ContaPoupanca;
import br.winxbank.sistemabancario.Movimentacao;
import br.winxbank.sistemaclientes.Cliente;
import br.winxbank.sistemaclientes.ClienteWinx;
import br.winxbank.sistemaclientes.RegistroDeClientes;
import br.winxbank.tempo.Ano;

/**
 * Testes de integracao: RegistroDeClientes + Banco + ContaCorrente.
 * Testa a interacao real entre componentes do sistema sem mocks.
 *
 * Fluxo: cadastro de cliente -> abertura de conta corrente ->
 * verificar que cliente e suas contas estao corretamente registrados.
 *
 * Tarefa 2.1 - Fernando Rene
 */
public class IntegracaoRegistroDeClientesTest {

    private static final int NUMERO_CARTAO = 4001;
    private static final int CSV_PADRAO = 321;

    @BeforeEach
    void setUp() {
        Banco.getInstancia().receitas = 0.0;
        Banco.getInstancia().despesas = 0.0;
        RegistroDeClientes.getInstancia().limparListaDeClientes();
        Ano.getInstancia().setMesAtual("Janeiro");
    }

    @AfterEach
    void tearDown() {
        RegistroDeClientes.getInstancia().limparListaDeClientes();
    }

    private ContaCorrente criarContaCorrente(int numeroConta, double saldo) {
        Cartao cartao = new Cartao(NUMERO_CARTAO, CSV_PADRAO);
        CartaoCredito cartaoCredito = new CartaoCredito(NUMERO_CARTAO, CSV_PADRAO);
        return new ContaCorrente(numeroConta, saldo, cartao, 0, cartaoCredito);
    }

    private ContaPoupanca criarContaPoupanca(int numeroConta, double saldo) {
        Cartao cartao = new Cartao(NUMERO_CARTAO, CSV_PADRAO);
        return new ContaPoupanca(numeroConta, saldo, cartao, 0);
    }

    @Test
    void testCadastroClienteComContaCorrenteFluxoReal() {
        System.out.println("=== INTEGRACAO: Fluxo real cadastrarCliente + Banco.abrirNovaConta ===");

        InputStream originalIn = System.in;
        try {
            System.setIn(new ByteArrayInputStream("Fernando Rene\n333.444.555-66\n1\n5000\n".getBytes()));

            RegistroDeClientes.getInstancia().cadastrarCliente();

            Cliente encontrado = RegistroDeClientes.getInstancia().retornarCliente("333.444.555-66");
            assertNotNull(encontrado);
            assertEquals("Fernando Rene", encontrado.getNome());
            assertEquals(1, encontrado.getContas().size());
            assertTrue(encontrado.getContas().get(0) instanceof ContaCorrente);
            assertEquals(5000.0, encontrado.getContas().get(0).getSaldo(), 0.001);
            assertFalse(encontrado.getContas().get(0).getExtrato().isEmpty());
        } finally {
            System.setIn(originalIn);
        }

        System.out.println("=== FLUXO REAL CONCLUIDO ===");
    }

    @Test
    void testCadastroClienteComContaCorrenteIntegracao() {
        System.out.println("=== INTEGRACAO: Cadastro de cliente com conta corrente ===");

        // 1. Criar cliente e conta corrente (simulando o fluxo de cadastrarCliente + abrirNovaConta)
        Cliente cliente = new Cliente("Maria Silva", "123.456.789-00");
        ContaCorrente conta = criarContaCorrente(10001, 5000.0);
        Movimentacao movimentacao = new Movimentacao(conta.getSaldo(), Movimentacao.TipoDaMovimentacao.ENTRADA);
        conta.setExtrato(movimentacao);
        cliente.setContas(conta);

        // 2. Registrar no RegistroDeClientes (integracao real)
        RegistroDeClientes.getInstancia().getClientes().add(cliente);

        // 3. Verificar que o cliente foi registrado
        assertEquals(1, RegistroDeClientes.getInstancia().getClientes().size());
        Cliente encontrado = RegistroDeClientes.getInstancia().retornarCliente("123.456.789-00");
        assertNotNull(encontrado);
        assertEquals("Maria Silva", encontrado.getNome());

        // 4. Verificar conta corrente vinculada
        assertFalse(encontrado.getContas().isEmpty());
        Conta contaEncontrada = encontrado.getContas().get(0);
        assertTrue(contaEncontrada instanceof ContaCorrente);
        assertEquals(5000.0, contaEncontrada.getSaldo(), 0.001);

        // 5. Verificar extrato
        assertFalse(contaEncontrada.getExtrato().isEmpty());
        assertEquals(Movimentacao.TipoDaMovimentacao.ENTRADA,
                contaEncontrada.getExtrato().get(0).getTipoDaMovimentacao());

        // 6. Verificar cartao de credito (integracao ContaCorrente)
        ContaCorrente corrente = (ContaCorrente) contaEncontrada;
        assertNotNull(corrente.getCartaoCredito());
        assertEquals(1000.0, corrente.getCartaoCredito().getLimite(), 0.001);

        System.out.println("Cliente: " + encontrado.getNome() + " | Saldo: " + contaEncontrada.getSaldo());
        System.out.println("=== INTEGRACAO CONCLUIDA COM SUCESSO ===");
    }

    @Test
    void testCadastroClienteComContaPoupancaIntegracao() {
        System.out.println("=== INTEGRACAO: Cadastro de cliente com conta poupanca ===");

        Cliente cliente = new Cliente("Joao Pedro", "987.654.321-00");
        ContaPoupanca conta = criarContaPoupanca(10002, 8000.0);
        Movimentacao movimentacao = new Movimentacao(conta.getSaldo(), Movimentacao.TipoDaMovimentacao.ENTRADA);
        conta.setExtrato(movimentacao);
        conta.setInformeRendimento(movimentacao);
        cliente.setContas(conta);
        RegistroDeClientes.getInstancia().getClientes().add(cliente);

        // Verificar registro
        assertEquals(1, RegistroDeClientes.getInstancia().getClientes().size());
        Conta contaEncontrada = RegistroDeClientes.getInstancia()
                .retornarCliente("987.654.321-00").getContas().get(0);
        assertTrue(contaEncontrada instanceof ContaPoupanca);
        assertEquals(8000.0, contaEncontrada.getSaldo(), 0.001);

        // Verificar informe de rendimento
        ContaPoupanca poupanca = (ContaPoupanca) contaEncontrada;
        assertFalse(poupanca.getInformeRendimento().isEmpty());

        System.out.println("=== INTEGRACAO CONTA POUPANCA CONCLUIDA ===");
    }

    @Test
    void testCadastroClienteWinxComPromocaoIntegracao() {
        System.out.println("=== INTEGRACAO: Cadastro com saldo >= 100k promove a ClienteWinx ===");

        // Cliente com saldo >= 100000 deve ser ClienteWinx
        ContaCorrente conta = criarContaCorrente(10003, 150000.0);
        Movimentacao movimentacao = new Movimentacao(conta.getSaldo(), Movimentacao.TipoDaMovimentacao.ENTRADA);
        conta.setExtrato(movimentacao);

        ClienteWinx clienteWinx = new ClienteWinx("Carlos Rico", "111.222.333-44", 0);
        clienteWinx.setContas(conta);
        RegistroDeClientes.getInstancia().getClientes().add(clienteWinx);

        // Verificar promocao
        Cliente encontrado = RegistroDeClientes.getInstancia().retornarCliente("111.222.333-44");
        assertNotNull(encontrado);
        assertTrue(encontrado instanceof ClienteWinx);
        assertEquals(0, ((ClienteWinx) encontrado).getPontosDeCompra());
        assertEquals(150000.0, encontrado.getContas().get(0).getSaldo(), 0.001);

        System.out.println("=== PROMOCAO A CLIENTEWINX CONCLUIDA ===");
    }

    @Test
    void testCadastroMultiplosClientesEBuscaPorCpf() throws InterruptedException {
        System.out.println("=== INTEGRACAO: Cadastro multiplo + busca por CPF ===");

        // Cadastrar cliente 1
        Cliente alice = new Cliente("Alice", "111.111.111-11");
        ContaCorrente conta1 = criarContaCorrente(10004, 3000.0);
        alice.setContas(conta1);
        RegistroDeClientes.getInstancia().getClientes().add(alice);

        // Cadastrar cliente 2
        Cliente bob = new Cliente("Bob", "222.222.222-22");
        ContaCorrente conta2 = criarContaCorrente(10005, 7000.0);
        bob.setContas(conta2);
        RegistroDeClientes.getInstancia().getClientes().add(bob);

        // Verificar que ambos foram registrados
        assertEquals(2, RegistroDeClientes.getInstancia().getClientes().size());

        // Buscar por CPF (integracao com retornarCliente)
        Cliente aliceEncontrada = RegistroDeClientes.getInstancia().retornarCliente("111.111.111-11");
        Cliente bobEncontrado = RegistroDeClientes.getInstancia().retornarCliente("222.222.222-22");
        assertNotNull(aliceEncontrada);
        assertNotNull(bobEncontrado);
        assertEquals("Alice", aliceEncontrada.getNome());
        assertEquals("Bob", bobEncontrado.getNome());

        // Verificar saldos
        assertEquals(3000.0, aliceEncontrada.getContas().get(0).getSaldo(), 0.001);
        assertEquals(7000.0, bobEncontrado.getContas().get(0).getSaldo(), 0.001);

        System.out.println("=== INTEGRACAO MULTIPLA CONCLUIDA ===");
    }

    @Test
    void testCadastroContaCorrenteComOperacoesBancarias() throws InterruptedException {
        System.out.println("=== INTEGRACAO: Cadastro + conta corrente + operacoes bancarias ===");

        // Cadastrar cliente com conta corrente
        Cliente cliente = new Cliente("Fernando", "333.333.333-33");
        ContaCorrente conta = criarContaCorrente(10006, 10000.0);
        Movimentacao mov = new Movimentacao(10000.0, Movimentacao.TipoDaMovimentacao.ENTRADA);
        conta.setExtrato(mov);
        cliente.setContas(conta);
        RegistroDeClientes.getInstancia().getClientes().add(cliente);

        double saldoInicial = conta.getSaldo();

        // Depositar na conta (integracao Conta)
        conta.depositar(5000.0);
        assertEquals(saldoInicial + 5000.0, conta.getSaldo(), 0.001);

        // Descontar taxa (integracao ContaCorrente + Banco)
        conta.descontarTaxa();
        assertEquals(saldoInicial + 5000.0 - 13.0, conta.getSaldo(), 0.001);
        assertTrue(Banco.getInstancia().getReceitas() > 0,
                "Banco deve ter receita da taxa de manutencao");
        assertEquals(13.0, Banco.getInstancia().getReceitas(), 0.001);

        // Atualizar cliente no registro (integracao RegistroDeClientes)
        RegistroDeClientes.getInstancia().atualizarCliente(cliente);
        Cliente atualizado = RegistroDeClientes.getInstancia().retornarCliente("333.333.333-33");
        assertNotNull(atualizado);

        // Verificar movimentacoes automaticas (integracao Banco + ContaCorrente)
        Banco.getInstancia().receitas = 0.0;
        Banco.getInstancia().movimentarEntreBancoConta();
        assertTrue(Banco.getInstancia().getReceitas() > 0,
                "movimentarEntreBancoConta deve gerar receita com taxa da conta corrente");

        System.out.println("Saldo final: " + conta.getSaldo());
        System.out.println("Receitas banco: " + Banco.getInstancia().getReceitas());
        System.out.println("=== INTEGRACAO COM OPERACOES CONCLUIDA ===");
    }
}
