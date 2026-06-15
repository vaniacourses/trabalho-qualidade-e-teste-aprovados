package br.winxbank.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.winxbank.sistemabancario.Banco;
import br.winxbank.sistemabancario.Cartao;
import br.winxbank.sistemabancario.CartaoCredito;
import br.winxbank.sistemabancario.Conta;
import br.winxbank.sistemabancario.ContaCorrente;
import br.winxbank.sistemabancario.Movimentacao;
import br.winxbank.sistemaclientes.Cliente;
import br.winxbank.sistemaclientes.RegistroDeClientes;
import br.winxbank.tempo.Ano;

/**
 * Teste de SISTEMA (Entrega 2 - tarefa 3.2).
 * Fluxo do ponto de vista do usuario: cadastrar cliente -> abrir conta corrente ->
 * depositar -> sacar -> verificar saldo e extrato.
 *
 * Usa os componentes reais (sem mocks), refletindo o que o Main faz: as operacoes
 * de deposito/saque registram movimentacoes no extrato.
 */
class SistemaContaCorrenteTest {

    private static final int NUMERO_CONTA = 6001;
    private static final int NUMERO_CARTAO = 7777;
    private static final int CSV = 111;
    private static final double DELTA = 0.001;

    @BeforeEach
    void setUp() {
        Banco.getInstancia().receitas = 0.0;
        Banco.getInstancia().despesas = 0.0;
        RegistroDeClientes.getInstancia().limparListaDeClientes();
        Ano.getInstancia().setMesAtual("Janeiro");
    }

    private ContaCorrente abrirContaCorrente(Cliente cliente, double saldoInicial) {
        ContaCorrente conta = new ContaCorrente(NUMERO_CONTA, saldoInicial,
                new Cartao(NUMERO_CARTAO, CSV), 0, new CartaoCredito(NUMERO_CARTAO, CSV));
        cliente.setContas(conta);
        // saldo inicial registrado como entrada (como o Main faz ao abrir a conta)
        conta.setExtrato(new Movimentacao(saldoInicial, Movimentacao.TipoDaMovimentacao.ENTRADA));
        return conta;
    }

    /** Deposito do fluxo de sistema: credita e registra no extrato. */
    private void depositar(Conta conta, double valor) {
        conta.depositar(valor);
        conta.setExtrato(new Movimentacao(valor, Movimentacao.TipoDaMovimentacao.ENTRADA));
    }

    /** Saque do fluxo de sistema: debita e registra no extrato. */
    private void sacar(Conta conta, double valor) {
        conta.sacar(valor);
        conta.setExtrato(new Movimentacao(valor, Movimentacao.TipoDaMovimentacao.SAIDA));
    }

    @Test
    @DisplayName("Fluxo depositar e sacar atualiza saldo e extrato")
    void testFluxoDepositarSacar() {
        // 1. Cadastro e abertura de conta
        Cliente cliente = new Cliente("Caio", "123.456.789-00");
        RegistroDeClientes.getInstancia().getClientes().add(cliente);
        ContaCorrente conta = abrirContaCorrente(cliente, 1000.0);

        // 2. "Login": recupera o cliente e sua conta pelo registro
        Cliente logado = RegistroDeClientes.getInstancia().retornarCliente("123.456.789-00");
        assertNotNull(logado, "cliente deve estar registrado para login");
        Conta contaLogada = logado.selecionarConta(NUMERO_CONTA);
        assertNotNull(contaLogada, "conta deve ser selecionavel apos login");

        // 3. Deposito e saque
        depositar(contaLogada, 500.0);
        sacar(contaLogada, 200.0);

        // 4. Verificacao de saldo: 1000 + 500 - 200 = 1300
        assertEquals(1300.0, contaLogada.getSaldo(), DELTA);

        // 5. Verificacao do extrato: abertura (entrada) + deposito (entrada) + saque (saida)
        assertEquals(3, contaLogada.getExtrato().size(), "extrato deve ter 3 movimentacoes");
        assertEquals(Movimentacao.TipoDaMovimentacao.ENTRADA,
                contaLogada.getExtrato().get(1).getTipoDaMovimentacao());
        assertEquals(Movimentacao.TipoDaMovimentacao.SAIDA,
                contaLogada.getExtrato().get(2).getTipoDaMovimentacao());
    }

    @Test
    @DisplayName("Multiplos depositos e saques acumulam no saldo e no extrato")
    void testMultiplasOperacoes() {
        Cliente cliente = new Cliente("Caio", "123.456.789-00");
        RegistroDeClientes.getInstancia().getClientes().add(cliente);
        ContaCorrente conta = abrirContaCorrente(cliente, 0.0);

        depositar(conta, 100.0);
        depositar(conta, 250.0);
        sacar(conta, 50.0);
        sacar(conta, 100.0);

        // 0 + 100 + 250 - 50 - 100 = 200
        assertEquals(200.0, conta.getSaldo(), DELTA);
        // abertura + 4 operacoes
        assertEquals(5, conta.getExtrato().size());
    }

    @Test
    @DisplayName("Deposito de valor zero nao altera o saldo")
    void testDepositoValorZero() {
        Cliente cliente = new Cliente("Caio", "123.456.789-00");
        ContaCorrente conta = abrirContaCorrente(cliente, 800.0);

        depositar(conta, 0.0);

        assertEquals(800.0, conta.getSaldo(), DELTA);
    }

    @Test
    @DisplayName("Saque acima do saldo deixa o saldo negativo (comportamento atual do sistema)")
    void testSaqueAcimaDoSaldo() {
        Cliente cliente = new Cliente("Caio", "123.456.789-00");
        ContaCorrente conta = abrirContaCorrente(cliente, 100.0);

        sacar(conta, 150.0);

        assertEquals(-50.0, conta.getSaldo(), DELTA);
        assertTrue(conta.getSaldo() < 0, "o sistema atual permite saldo negativo no saque");
    }
}
