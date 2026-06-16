package br.winxbank.test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
 * Teste de sistema: Fluxo completo de cadastro e login.
 * Valida o fluxo do ponto de vista do usuario:
 *   criar usuario -> logar -> verificar dados do cliente logado ->
 *   apagar usuario -> verificar remocao.
 *
 * Tarefa 4.1.1 - Fernando Rene
 */
public class SistemaCadastroLoginTest {

    private static final int NUMERO_CARTAO = 5001;
    private static final int CSV_PADRAO = 456;

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

    private void cadastrarCliente(String nome, String cpf, double saldo) {
        Cliente cliente = new Cliente(nome, cpf);
        ContaCorrente conta = criarContaCorrente(10001, saldo);
        Movimentacao mov = new Movimentacao(saldo, Movimentacao.TipoDaMovimentacao.ENTRADA);
        conta.setExtrato(mov);
        cliente.setContas(conta);
        if (saldo >= 100000) {
            ClienteWinx winx = new ClienteWinx(nome, cpf, 0);
            winx.setContas(conta);
            RegistroDeClientes.getInstancia().getClientes().add(winx);
        } else {
            RegistroDeClientes.getInstancia().getClientes().add(cliente);
        }
    }

    @Test
    void testFluxoCompletoCadastroLoginVerificacaoRemocao() {
        System.out.println("=== TESTE DE SISTEMA: Fluxo completo cadastro -> login -> verificar -> apagar ===");

        // 1. CADASTRAR USUARIO
        System.out.println("\n--- Etapa 1: Cadastrar usuario ---");
        cadastrarCliente("Fernando Rene", "111.222.333-44", 5000.0);

        assertEquals(1, RegistroDeClientes.getInstancia().getClientes().size(),
                "Deve ter 1 cliente registrado apos cadastro");
        System.out.println("Usuario cadastrado com sucesso");

        // 2. LOGAR (buscar por CPF)
        System.out.println("\n--- Etapa 2: Login ---");
        Cliente clienteLogado = RegistroDeClientes.getInstancia().retornarCliente("111.222.333-44");

        assertNotNull(clienteLogado, "Login deve encontrar o cliente");
        System.out.println("Login efetuado: " + clienteLogado.getNome());

        // 3. VERIFICAR DADOS DO CLIENTE LOGADO
        System.out.println("\n--- Etapa 3: Verificar dados ---");
        assertEquals("Fernando Rene", clienteLogado.getNome());
        assertEquals("111.222.333-44", clienteLogado.getCpf());
        assertFalse(clienteLogado.getContas().isEmpty(), "Cliente deve ter contas");

        Conta conta = clienteLogado.getContas().get(0);
        assertTrue(conta instanceof ContaCorrente, "Deve ter conta corrente");
        assertEquals(5000.0, conta.getSaldo(), 0.001);
        System.out.println("Dados verificados - Nome: " + clienteLogado.getNome()
                + " | Saldo: " + conta.getSaldo());

        // 4. APAGAR USUARIO
        System.out.println("\n--- Etapa 4: Apagar usuario ---");
        RegistroDeClientes.getInstancia().removerCliente(clienteLogado);

        // 5. VERIFICAR REMOCAO
        System.out.println("\n--- Etapa 5: Verificar remocao ---");
        assertEquals(0, RegistroDeClientes.getInstancia().getClientes().size(),
                "Lista deve estar vazia apos remocao");
        assertNull(RegistroDeClientes.getInstancia().retornarCliente("111.222.333-44"),
                "Busca por CPF removido deve retornar null");

        System.out.println("=== FLUXO COMPLETO DE CADASTRO/LOGIN CONCLUIDO COM SUCESSO ===");
    }

    @Test
    void testFluxoCadastroLoginComClienteWinx() {
        System.out.println("=== TESTE DE SISTEMA: Fluxo cadastro/login com ClienteWinx ===");

        // 1. CADASTRAR com saldo >= 100k
        cadastrarCliente("Carlos Winx", "555.666.777-88", 200000.0);

        // 2. LOGAR
        Cliente clienteLogado = RegistroDeClientes.getInstancia().retornarCliente("555.666.777-88");
        assertNotNull(clienteLogado);
        assertTrue(clienteLogado instanceof ClienteWinx,
                "Cliente com saldo >= 100k deve ser ClienteWinx");
        assertEquals(0, ((ClienteWinx) clienteLogado).getPontosDeCompra());

        // 3. VERIFICAR DADOS
        assertEquals("Carlos Winx", clienteLogado.getNome());
        assertEquals(200000.0, clienteLogado.getContas().get(0).getSaldo(), 0.001);

        // 4. APAGAR
        RegistroDeClientes.getInstancia().removerCliente(clienteLogado);

        // 5. VERIFICAR REMOCAO
        assertTrue(RegistroDeClientes.getInstancia().getClientes().isEmpty());
        assertNull(RegistroDeClientes.getInstancia().retornarCliente("555.666.777-88"));

        System.out.println("=== FLUXO CLIENTEWINX CONCLUIDO ===");
    }

    @Test
    void testFluxoLoginComCpfInexistente() {
        System.out.println("=== TESTE DE SISTEMA: Login com CPF inexistente ===");

        cadastrarCliente("Alice", "111.111.111-11", 1000.0);

        // Tentar logar com CPF inexistente
        Cliente resultado = RegistroDeClientes.getInstancia().retornarCliente("999.999.999-99");
        assertNull(resultado, "Login com CPF inexistente deve retornar null");

        // Verificar que o cliente original permanece
        assertEquals(1, RegistroDeClientes.getInstancia().getClientes().size());
        assertNotNull(RegistroDeClientes.getInstancia().retornarCliente("111.111.111-11"));

        System.out.println("=== LOGIN INEXISTENTE TRATADO CORRETAMENTE ===");
    }

    @Test
    void testFluxoCadastroMultiploComDuplicacaoDeCpf() {
        System.out.println("=== TESTE DE SISTEMA: Cadastro duplo com mesmo CPF ===");

        // Cadastrar primeiro cliente
        cadastrarCliente("Primeiro", "111.111.111-11", 3000.0);
        assertEquals(1, RegistroDeClientes.getInstancia().getClientes().size());

        // Verificar que checarCpf bloqueia duplicata
        boolean cpfDisponivel = RegistroDeClientes.getInstancia().checarCpf("111.111.111-11");
        assertFalse(cpfDisponivel, "CPF ja cadastrado nao deve estar disponivel");

        // Verificar que o original permanece
        assertEquals("Primeiro", RegistroDeClientes.getInstancia().getClientes().get(0).getNome());

        System.out.println("=== DUPLICACAO DE CPF BLOQUEADA CORRETAMENTE ===");
    }

    @Test
    void testFluxoCadastroLoginAtualizacaoERemocao() throws InterruptedException {
        System.out.println("=== TESTE DE SISTEMA: Cadastro -> Login -> Atualizar -> Remover ===");

        // 1. CADASTRAR
        cadastrarCliente("Maria Original", "222.333.444-55", 8000.0);

        // 2. LOGAR
        Cliente clienteLogado = RegistroDeClientes.getInstancia().retornarCliente("222.333.444-55");
        assertNotNull(clienteLogado);

        // 3. DEPOSITAR (operacao bancaria)
        Conta conta = clienteLogado.getContas().get(0);
        double saldoAntes = conta.getSaldo();
        conta.depositar(2000.0);
        assertEquals(saldoAntes + 2000.0, conta.getSaldo(), 0.001);

        // 4. ATUALIZAR no registro
        RegistroDeClientes.getInstancia().atualizarCliente(clienteLogado);

        // 5. VERIFICAR atualizacao
        Cliente verificado = RegistroDeClientes.getInstancia().retornarCliente("222.333.444-55");
        assertNotNull(verificado);

        // 6. REMOVER
        RegistroDeClientes.getInstancia().removerCliente(verificado);
        assertTrue(RegistroDeClientes.getInstancia().getClientes().isEmpty());

        System.out.println("=== FLUXO COMPLETO COM ATUALIZACAO CONCLUIDO ===");
    }
}
