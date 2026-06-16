package br.winxbank.test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.winxbank.gui.ActionResult;
import br.winxbank.gui.WinxBankController;
import br.winxbank.sistemabancario.Banco;
import br.winxbank.sistemabancario.Cartao;
import br.winxbank.sistemabancario.CartaoCredito;
import br.winxbank.sistemabancario.Conta;
import br.winxbank.sistemabancario.ContaCorrente;
import br.winxbank.sistemabancario.Movimentacao;
import br.winxbank.sistemaclientes.Cliente;
import br.winxbank.sistemaclientes.ClienteWinx;
import br.winxbank.sistemaclientes.RegistroDeClientes;
import br.winxbank.tempo.Ano;

/**
 * Teste E2E (End-to-End): Cadastro completo de usuario com conta corrente via interface.
 * Utiliza o WinxBankController como ponto de entrada da interface grafica (Swing),
 * simulando o fluxo completo que um usuario faria pela GUI.
 *
 * O cadastro e feito via setup direto (pois cadastrarCliente usa multiplos Scanners
 * internamente), e todas as operacoes subsequentes sao feitas via WinxBankController.
 *
 * Tarefa 4.2.1 - Fernando Rene
 */
public class E2ECadastroContaCorrenteTest {

    private static final int NUMERO_CARTAO = 6001;
    private static final int CSV_PADRAO = 789;

    private WinxBankController controller;

    @BeforeEach
    void setUp() {
        Banco.getInstancia().receitas = 0.0;
        Banco.getInstancia().despesas = 0.0;
        RegistroDeClientes.getInstancia().limparListaDeClientes();
        Ano.getInstancia().setMesAtual("Janeiro");
        controller = new WinxBankController();
    }

    @AfterEach
    void tearDown() {
        RegistroDeClientes.getInstancia().limparListaDeClientes();
    }

    private void cadastrarClienteNoRegistro(String nome, String cpf, double saldo) {
        ContaCorrente conta = new ContaCorrente(
                10001, saldo,
                new Cartao(NUMERO_CARTAO, CSV_PADRAO), 0,
                new CartaoCredito(NUMERO_CARTAO, CSV_PADRAO));
        Movimentacao mov = new Movimentacao(saldo, Movimentacao.TipoDaMovimentacao.ENTRADA);
        conta.setExtrato(mov);

        if (saldo >= 100000) {
            ClienteWinx winx = new ClienteWinx(nome, cpf, 0);
            winx.setContas(conta);
            RegistroDeClientes.getInstancia().getClientes().add(winx);
        } else {
            Cliente cliente = new Cliente(nome, cpf);
            cliente.setContas(conta);
            RegistroDeClientes.getInstancia().getClientes().add(cliente);
        }
    }

    @Test
    void testE2ECadastroCompletoViaController() {
        System.out.println("=== E2E: Cadastro completo de usuario com conta corrente via interface ===");

        // 1. CADASTRAR CLIENTE (setup do sistema)
        System.out.println("\n--- Passo 1: Cadastrar cliente ---");
        cadastrarClienteNoRegistro("Fernando Rene", "111.222.333-44", 5000.0);
        assertEquals(1, controller.getClientes().size());

        // 2. LOGIN via controller (simula formulario da GUI)
        System.out.println("\n--- Passo 2: Login via interface ---");
        ActionResult resultLogin = controller.login("111.222.333-44");
        assertTrue(resultLogin.isOk(), "Login deve ser bem-sucedido");
        assertTrue(resultLogin.isSessionChanged(), "Sessao deve ser atualizada");
        assertNotNull(resultLogin.getUpdatedCliente());
        System.out.println("Login: " + resultLogin.getMessage());

        // 3. VERIFICAR DADOS do cliente logado
        System.out.println("\n--- Passo 3: Verificar dados ---");
        Cliente clienteLogado = resultLogin.getUpdatedCliente();
        assertEquals("Fernando Rene", clienteLogado.getNome());
        assertEquals("111.222.333-44", clienteLogado.getCpf());
        assertFalse(clienteLogado.getContas().isEmpty());

        Conta conta = clienteLogado.getContas().get(0);
        assertTrue(conta instanceof ContaCorrente);
        assertEquals(5000.0, conta.getSaldo(), 0.001);

        ContaCorrente corrente = (ContaCorrente) conta;
        assertNotNull(corrente.getCartaoCredito());
        System.out.println("Conta: " + conta.getNumeroConta() + " | Saldo: " + conta.getSaldo());

        // 4. DEPOSITAR via controller
        System.out.println("\n--- Passo 4: Depositar via interface ---");
        ActionResult resultDeposito = controller.depositar(
                clienteLogado, conta.getNumeroConta(), 3000.0);
        assertTrue(resultDeposito.isOk());
        assertEquals(8000.0, conta.getSaldo(), 0.001);
        System.out.println("Deposito: " + resultDeposito.getMessage());

        // 5. SACAR via controller
        System.out.println("\n--- Passo 5: Sacar via interface ---");
        ActionResult resultSaque = controller.sacar(
                clienteLogado, conta.getNumeroConta(), 1000.0);
        assertTrue(resultSaque.isOk());
        assertEquals(7000.0, conta.getSaldo(), 0.001);
        System.out.println("Saque: " + resultSaque.getMessage());

        // 6. APAGAR USUARIO via controller
        System.out.println("\n--- Passo 6: Apagar usuario via interface ---");
        ActionResult resultApagar = controller.apagarUsuario(clienteLogado);
        assertTrue(resultApagar.isOk());
        assertTrue(controller.getClientes().isEmpty());
        System.out.println("Apagar: " + resultApagar.getMessage());

        System.out.println("\n=== E2E CADASTRO COMPLETO VIA INTERFACE CONCLUIDO COM SUCESSO ===");
    }

    @Test
    void testE2ECadastroClienteWinxViaController() {
        System.out.println("=== E2E: Cadastro de ClienteWinx via interface ===");

        // Cadastrar com saldo >= 100k
        cadastrarClienteNoRegistro("Milionario", "999.888.777-66", 150000.0);

        // Login via controller
        ActionResult resultLogin = controller.login("999.888.777-66");
        assertTrue(resultLogin.isOk());

        Cliente logado = resultLogin.getUpdatedCliente();
        assertNotNull(logado);
        assertTrue(logado instanceof ClienteWinx,
                "Cliente com saldo >= 100k deve ser ClienteWinx");
        assertFalse(logado.getContas().isEmpty());
        assertTrue(logado.getContas().get(0).getSaldo() >= 100000.0,
                "ClienteWinx deve manter saldo acima do limite de promocao");

        System.out.println("=== E2E CLIENTEWINX CONCLUIDO ===");
    }

    @Test
    void testE2ELoginComCpfInexistenteViaController() {
        System.out.println("=== E2E: Login com CPF inexistente via interface ===");

        ActionResult resultado = controller.login("000.000.000-00");

        assertFalse(resultado.isOk(), "Login com CPF inexistente deve falhar");
        assertTrue(resultado.getMessage().contains("inexistente"));

        System.out.println("Resultado: " + resultado.getMessage());
        System.out.println("=== E2E LOGIN INEXISTENTE CONCLUIDO ===");
    }

    @Test
    void testE2EFluxoCompletoComMultiplasOperacoes() {
        System.out.println("=== E2E: Fluxo completo com multiplas operacoes via interface ===");

        // 1. Cadastrar
        cadastrarClienteNoRegistro("Joao", "123.123.123-12", 10000.0);
        assertEquals(1, controller.getClientes().size());

        // 2. Login
        ActionResult r2 = controller.login("123.123.123-12");
        assertTrue(r2.isOk());
        Cliente cliente = r2.getUpdatedCliente();
        int numeroConta = cliente.getContas().get(0).getNumeroConta();

        // 3. Depositar
        ActionResult r3 = controller.depositar(cliente, numeroConta, 5000.0);
        assertTrue(r3.isOk());

        // 4. Sacar
        ActionResult r4 = controller.sacar(cliente, numeroConta, 2000.0);
        assertTrue(r4.isOk());

        // 5. Verificar saldo final
        Conta conta = cliente.selecionarConta(numeroConta);
        assertNotNull(conta);
        assertEquals(13000.0, conta.getSaldo(), 0.001);

        // 6. Apagar
        ActionResult r5 = controller.apagarUsuario(cliente);
        assertTrue(r5.isOk());
        assertTrue(controller.getClientes().isEmpty());

        System.out.println("=== E2E FLUXO COMPLETO CONCLUIDO ===");
    }
}
