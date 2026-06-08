package br.winxbank.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
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
 * Teste de sistema: Fluxo completo de PIX entre dois clientes.
 * Simula o fluxo: cadastrar cliente A -> abrir conta -> cadastrar cliente B ->
 * abrir conta -> depositar em A -> fazer pix de A para B -> verificar saldos.
 */
public class SistemaPixTest {

    private static final int NUMERO_CARTAO = 8001;
    private static final int CSV_PADRAO = 321;

    @BeforeEach
    void setUp() {
        Banco.getInstancia().receitas = 0.0;
        Banco.getInstancia().despesas = 0.0;
        RegistroDeClientes.getInstancia().limparListaDeClientes();
        Ano.getInstancia().setMesAtual("Janeiro");
    }

    private ContaCorrente criarContaCorrente(int numeroConta, double saldo) {
        Cartao cartao = new Cartao(NUMERO_CARTAO, CSV_PADRAO);
        CartaoCredito cartaoCredito = new CartaoCredito(NUMERO_CARTAO, CSV_PADRAO);
        return new ContaCorrente(numeroConta, saldo, cartao, 0, cartaoCredito);
    }

    @Test
    void testFluxoCompletoDePixEntreDoisClientes() throws InterruptedException {
        System.out.println("=== TESTE DE SISTEMA: Fluxo completo de PIX entre dois clientes ===");

        // 1. Cadastrar cliente A
        Cliente clienteA = new Cliente("Alice", "111.111.111-11");
        RegistroDeClientes.getInstancia().getClientes().add(clienteA);
        System.out.println("Cliente A cadastrado: " + clienteA.getNome() + " CPF: " + clienteA.getCpf());

        // 2. Abrir conta corrente para A com saldo de 2000
        ContaCorrente contaA = criarContaCorrente(1001, 2000.0);
        clienteA.setContas(contaA);
        Movimentacao movA = new Movimentacao(2000.0, Movimentacao.TipoDaMovimentacao.ENTRADA);
        contaA.setExtrato(movA);
        System.out.println("Conta de A aberta - numero: " + contaA.getNumeroConta() + " saldo: " + contaA.getSaldo());

        // 3. Atualizar cliente A no registro
        RegistroDeClientes.getInstancia().atualizarCliente(clienteA);

        // 4. Cadastrar cliente B
        Cliente clienteB = new Cliente("Bob", "222.222.222-22");
        RegistroDeClientes.getInstancia().getClientes().add(clienteB);
        System.out.println("Cliente B cadastrado: " + clienteB.getNome() + " CPF: " + clienteB.getCpf());

        // 5. Abrir conta corrente para B com saldo de 500
        ContaCorrente contaB = criarContaCorrente(2001, 500.0);
        clienteB.setContas(contaB);
        Movimentacao movB = new Movimentacao(500.0, Movimentacao.TipoDaMovimentacao.ENTRADA);
        contaB.setExtrato(movB);
        System.out.println("Conta de B aberta - numero: " + contaB.getNumeroConta() + " saldo: " + contaB.getSaldo());

        // 6. Atualizar cliente B no registro
        RegistroDeClientes.getInstancia().atualizarCliente(clienteB);

        // 7. Verificar que ambos estao registrados
        assertEquals(2, RegistroDeClientes.getInstancia().getClientes().size());
        assertNotNull(RegistroDeClientes.getInstancia().retornarCliente("111.111.111-11"));
        assertNotNull(RegistroDeClientes.getInstancia().retornarCliente("222.222.222-22"));

        // 8. Selecionar contas
        Conta contaSelecionadaA = clienteA.selecionarConta(1001);
        Conta contaSelecionadaB = clienteB.selecionarConta(2001);
        assertNotNull(contaSelecionadaA);
        assertNotNull(contaSelecionadaB);

        // 9. Realizar PIX: deduzir de A e creditar em B
        double valorPix = 300.0;
        double saldoInicialA = contaSelecionadaA.getSaldo();
        double saldoInicialB = contaSelecionadaB.getSaldo();

        System.out.println("\n--- Realizando PIX ---");
        System.out.println("Valor do PIX: " + valorPix);
        System.out.println("Saldo A antes: " + saldoInicialA);
        System.out.println("Saldo B antes: " + saldoInicialB);

        // Deduz da origem (simulando o que Main deveria fazer)
        contaSelecionadaA.sacar(valorPix);
        // Credita no destino via fazerPix
        contaSelecionadaA.fazerPix(contaSelecionadaB, valorPix);

        // 10. Registrar movimentacoes no extrato
        Movimentacao movSaida = new Movimentacao(valorPix, Movimentacao.TipoDaMovimentacao.SAIDA);
        Movimentacao movEntrada = new Movimentacao(valorPix, Movimentacao.TipoDaMovimentacao.ENTRADA);
        contaSelecionadaA.setExtrato(movSaida);
        contaSelecionadaB.setExtrato(movEntrada);

        RegistroDeClientes.getInstancia().atualizarCliente(clienteA);
        RegistroDeClientes.getInstancia().atualizarCliente(clienteB);

        // 11. Verificar saldos finais
        double saldoFinalA = contaSelecionadaA.getSaldo();
        double saldoFinalB = contaSelecionadaB.getSaldo();

        System.out.println("Saldo A depois: " + saldoFinalA);
        System.out.println("Saldo B depois: " + saldoFinalB);

        assertEquals(saldoInicialA - valorPix, saldoFinalA, 0.001,
                "Saldo de A deve diminuir pelo valor do PIX");
        assertEquals(saldoInicialB + valorPix, saldoFinalB, 0.001,
                "Saldo de B deve aumentar pelo valor do PIX");

        // 12. Verificar que os extratos foram atualizados
        assertTrue(contaSelecionadaA.getExtrato().size() > 1,
                "Extrato de A deve conter a movimentacao de saida");
        assertTrue(contaSelecionadaB.getExtrato().size() > 1,
                "Extrato de B deve conter a movimentacao de entrada");

        System.out.println("\n=== FLUXO DE PIX CONCLUIDO COM SUCESSO ===");
    }

    @Test
    void testFluxoPixComSelecaoDeContaInvalida() {
        System.out.println("=== TESTE DE SISTEMA: PIX com conta inexistente deve retornar null ===");

        Cliente clienteA = new Cliente("Alice", "333.333.333-33");
        ContaCorrente contaA = criarContaCorrente(1001, 1000.0);
        clienteA.setContas(contaA);

        Cliente clienteB = new Cliente("Bob", "444.444.444-44");
        ContaCorrente contaB = criarContaCorrente(2001, 500.0);
        clienteB.setContas(contaB);

        Conta contaInvalida = clienteB.selecionarConta(9999);

        System.out.println("Conta selecionada com numero inexistente: " + contaInvalida);

        assertEquals(null, contaInvalida,
                "selecionarConta com numero inexistente deve retornar null");
    }

    @Test
    void testFluxoPixMultiploComMesmoCliente() {
        System.out.println("=== TESTE DE SISTEMA: PIX multiplo com mesmo cliente ===");

        Cliente cliente = new Cliente("Carlos", "555.555.555-55");
        RegistroDeClientes.getInstancia().getClientes().add(cliente);

        ContaCorrente conta1 = criarContaCorrente(3001, 1000.0);
        ContaCorrente conta2 = criarContaCorrente(3002, 500.0);
        cliente.setContas(conta1);
        cliente.setContas(conta2);

        double valorPix = 200.0;
        double saldoInicialConta1 = conta1.getSaldo();
        double saldoInicialConta2 = conta2.getSaldo();

        conta1.sacar(valorPix);
        conta1.fazerPix(conta2, valorPix);

        System.out.println("Conta 1 - saldo antes: " + saldoInicialConta1 + " | depois: " + conta1.getSaldo());
        System.out.println("Conta 2 - saldo antes: " + saldoInicialConta2 + " | depois: " + conta2.getSaldo());

        assertEquals(saldoInicialConta1 - valorPix, conta1.getSaldo(), 0.001);
        assertEquals(saldoInicialConta2 + valorPix, conta2.getSaldo(), 0.001);
    }

    @Test
    void testFluxoPixValorZero() {
        System.out.println("=== TESTE DE SISTEMA: PIX com valor zero ===");

        Cliente clienteA = new Cliente("Alice", "666.666.666-66");
        Cliente clienteB = new Cliente("Bob", "777.777.777-77");
        RegistroDeClientes.getInstancia().getClientes().add(clienteA);
        RegistroDeClientes.getInstancia().getClientes().add(clienteB);

        ContaCorrente contaA = criarContaCorrente(4001, 1000.0);
        ContaCorrente contaB = criarContaCorrente(4002, 500.0);
        clienteA.setContas(contaA);
        clienteB.setContas(contaB);

        double saldoAntesA = contaA.getSaldo();
        double saldoAntesB = contaB.getSaldo();

        contaA.fazerPix(contaB, 0.0);

        System.out.println("Saldo A antes: " + saldoAntesA + " | depois: " + contaA.getSaldo());
        System.out.println("Saldo B antes: " + saldoAntesB + " | depois: " + contaB.getSaldo());

        assertEquals(saldoAntesA, contaA.getSaldo(), 0.001,
                "Saldo de A nao deve mudar com PIX de valor zero");
        assertEquals(saldoAntesB, contaB.getSaldo(), 0.001,
                "Saldo de B nao deve mudar com PIX de valor zero");
    }
}
