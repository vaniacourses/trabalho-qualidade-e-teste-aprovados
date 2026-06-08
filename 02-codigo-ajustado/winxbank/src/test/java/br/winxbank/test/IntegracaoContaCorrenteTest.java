package br.winxbank.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.winxbank.sistemabancario.Banco;
import br.winxbank.sistemabancario.Cartao;
import br.winxbank.sistemabancario.CartaoCredito;
import br.winxbank.sistemabancario.ContaCorrente;
import br.winxbank.sistemabancario.ContaPoupanca;
import br.winxbank.sistemabancario.Movimentacao;
import br.winxbank.sistemaclientes.Cliente;
import br.winxbank.sistemaclientes.RegistroDeClientes;
import br.winxbank.tempo.Ano;

/**
 * Testes de integracao para ContaCorrente:
 * - pagarFatura + CartaoCredito
 * - movimentarEntreBancoConta + descontarTaxa + cobrarJurus
 */
public class IntegracaoContaCorrenteTest {

    private static final int NUMERO_CONTA = 3001;
    private static final int NUMERO_CONTA_POUPANCA = 3002;
    private static final int NUMERO_CARTAO = 7001;
    private static final int CSV_PADRAO = 789;
    private static final double SALDO_INICIAL = 1000.0;
    private static final double TAXA_MANUTENCAO = 13.0;
    private static final double RENDIMENTO_MENSAL_POUPANCA = 0.8;

    @BeforeEach
    void setUp() {
        Banco.getInstancia().receitas = 0.0;
        Banco.getInstancia().despesas = 0.0;
        RegistroDeClientes.getInstancia().limparListaDeClientes();
        Ano.getInstancia().setMesAtual("Janeiro");
    }

    private ContaCorrente criarContaCorrente(double saldo) {
        Cartao cartao = new Cartao(NUMERO_CARTAO, CSV_PADRAO);
        CartaoCredito cartaoCredito = new CartaoCredito(NUMERO_CARTAO, CSV_PADRAO);
        return new ContaCorrente(NUMERO_CONTA, saldo, cartao, 0, cartaoCredito);
    }

    private ContaPoupanca criarContaPoupanca(double saldo) {
        Cartao cartao = new Cartao(NUMERO_CARTAO, CSV_PADRAO);
        return new ContaPoupanca(NUMERO_CONTA_POUPANCA, saldo, cartao, 0);
    }

    @Test
    void testPagarFaturaIntegracaoSaldoEFatura() {
        System.out.println("Teste de integracao: pagarFatura diminui saldo da conta e fatura do cartao");

        ContaCorrente conta = criarContaCorrente(SALDO_INICIAL);
        CartaoCredito cartaoCredito = conta.getCartaoCredito();

        cartaoCredito.creditar(400.0, 0, "Janeiro");
        assertEquals(400.0, cartaoCredito.getFatura(), 0.001);
        assertFalse(cartaoCredito.isFaturaPaga());

        conta.pagarFatura(400.0);

        double saldoEsperado = SALDO_INICIAL - 400.0;

        System.out.println("Saldo esperado: " + saldoEsperado + " | Saldo final: " + conta.getSaldo());
        System.out.println("Fatura esperada: 0.0 | Fatura final: " + cartaoCredito.getFatura());
        System.out.println("Fatura paga esperada: true | Fatura paga: " + cartaoCredito.isFaturaPaga());

        assertEquals(saldoEsperado, conta.getSaldo(), 0.001);
        assertEquals(0.0, cartaoCredito.getFatura(), 0.001);
        assertTrue(cartaoCredito.isFaturaPaga());
    }

    @Test
    void testPagarFaturaParcialIntegracao() {
        System.out.println("Teste de integracao: pagarFatura parcial mantem fatura nao paga");

        ContaCorrente conta = criarContaCorrente(SALDO_INICIAL);
        CartaoCredito cartaoCredito = conta.getCartaoCredito();

        cartaoCredito.creditar(500.0, 0, "Janeiro");
        conta.pagarFatura(200.0);

        System.out.println("Saldo esperado: 800.0 | Saldo final: " + conta.getSaldo());
        System.out.println("Fatura esperada: 300.0 | Fatura final: " + cartaoCredito.getFatura());
        System.out.println("Fatura paga esperada: false | Fatura paga: " + cartaoCredito.isFaturaPaga());

        assertEquals(800.0, conta.getSaldo(), 0.001);
        assertEquals(300.0, cartaoCredito.getFatura(), 0.001);
        assertFalse(cartaoCredito.isFaturaPaga());
    }

    @Test
    void testMovimentarEntreBancoContaDescontaTaxaECobraJuros() {
        System.out.println("Teste de integracao: movimentarEntreBancoConta desconta taxa e cobra juros do cartao");

        ContaCorrente conta = criarContaCorrente(SALDO_INICIAL);
        CartaoCredito cartaoCredito = conta.getCartaoCredito();

        cartaoCredito.creditar(200.0, 0, "Janeiro");

        Cliente cliente = new Cliente("Teste", "111.111.111-11");
        cliente.setContas(conta);
        RegistroDeClientes.getInstancia().getClientes().add(cliente);

        Ano.getInstancia().setMesAtual("Fevereiro");

        Banco.getInstancia().movimentarEntreBancoConta();

        double saldoEsperado = SALDO_INICIAL - TAXA_MANUTENCAO;
        double taxaJurus = 12.75;
        double faturaEsperada = 200.0 * taxaJurus;
        double receitaEsperada = TAXA_MANUTENCAO + (faturaEsperada - 200.0);

        System.out.println("Saldo esperado: " + saldoEsperado + " | Saldo final: " + conta.getSaldo());
        System.out.println("Fatura esperada: " + faturaEsperada + " | Fatura final: " + cartaoCredito.getFatura());
        System.out.println("Receitas esperadas: " + receitaEsperada + " | Receitas: " + Banco.getInstancia().getReceitas());

        assertEquals(saldoEsperado, conta.getSaldo(), 0.001);
        assertEquals(faturaEsperada, cartaoCredito.getFatura(), 0.001);
        assertEquals(receitaEsperada, Banco.getInstancia().getReceitas(), 0.001);
    }

    @Test
    void testMovimentarEntreBancoContaComFaturaPagaNaoCobraJuros() {
        System.out.println("Teste de integracao: movimentarEntreBancoConta com fatura paga nao cobra juros");

        ContaCorrente conta = criarContaCorrente(SALDO_INICIAL);

        Cliente cliente = new Cliente("Teste", "222.222.222-22");
        cliente.setContas(conta);
        RegistroDeClientes.getInstancia().getClientes().add(cliente);

        Ano.getInstancia().setMesAtual("Fevereiro");

        Banco.getInstancia().movimentarEntreBancoConta();

        double saldoEsperado = SALDO_INICIAL - TAXA_MANUTENCAO;

        System.out.println("Saldo esperado: " + saldoEsperado + " | Saldo final: " + conta.getSaldo());
        System.out.println("Fatura esperada: 0.0 | Fatura final: " + conta.getCartaoCredito().getFatura());
        System.out.println("Receitas esperadas: " + TAXA_MANUTENCAO + " | Receitas: " + Banco.getInstancia().getReceitas());

        assertEquals(saldoEsperado, conta.getSaldo(), 0.001);
        assertEquals(0.0, conta.getCartaoCredito().getFatura(), 0.001);
        assertEquals(TAXA_MANUTENCAO, Banco.getInstancia().getReceitas(), 0.001);
    }

    @Test
    void testMovimentarEntreBancoContaComContasMistas() {
        System.out.println("Teste de integracao: movimentarEntreBancoConta com contas corrente e poupanca");

        ContaCorrente contaCorrente = criarContaCorrente(SALDO_INICIAL);
        ContaPoupanca contaPoupanca = criarContaPoupanca(SALDO_INICIAL);

        Cliente cliente = new Cliente("Teste", "333.333.333-33");
        cliente.setContas(contaCorrente);
        cliente.setContas(contaPoupanca);
        RegistroDeClientes.getInstancia().getClientes().add(cliente);

        Banco.getInstancia().movimentarEntreBancoConta();

        double rendimento = SALDO_INICIAL / RENDIMENTO_MENSAL_POUPANCA;
        double saldoCorrenteEsperado = SALDO_INICIAL - TAXA_MANUTENCAO;
        double saldoPoupancaEsperado = SALDO_INICIAL + rendimento;

        double receitas = Banco.getInstancia().getReceitas();
        double despesas = Banco.getInstancia().getDespesas();

        System.out.println("Conta corrente - saldo: " + contaCorrente.getSaldo());
        System.out.println("Conta poupanca - saldo: " + contaPoupanca.getSaldo());
        System.out.println("Receitas do banco: " + receitas);
        System.out.println("Despesas do banco: " + despesas);

        assertEquals(saldoCorrenteEsperado, contaCorrente.getSaldo(), 0.001);
        assertEquals(saldoPoupancaEsperado, contaPoupanca.getSaldo(), 0.001);
        assertEquals(TAXA_MANUTENCAO, receitas, 0.001);
        assertEquals(rendimento, despesas, 0.001);
    }

    @Test
    void testExtratoRegistraMovimentacoesAposMovimentarEntreBancoConta() {
        System.out.println("Teste de integracao: extrato registra movimentacoes apos movimentarEntreBancoConta");

        ContaCorrente conta = criarContaCorrente(SALDO_INICIAL);
        int extratoAntes = conta.getExtrato().size();

        Cliente cliente = new Cliente("Teste", "444.444.444-44");
        cliente.setContas(conta);
        RegistroDeClientes.getInstancia().getClientes().add(cliente);

        Banco.getInstancia().movimentarEntreBancoConta();

        int extratoDepois = conta.getExtrato().size();
        Movimentacao ultima = conta.getExtrato().get(extratoDepois - 1);

        System.out.println("Extrato antes: " + extratoAntes + " | depois: " + extratoDepois);
        System.out.println("Ultima movimentacao - valor: " + ultima.getDinheiroMovimentado() + " tipo: " + ultima.getTipoDaMovimentacao());

        assertEquals(extratoAntes + 1, extratoDepois);
        assertEquals(TAXA_MANUTENCAO, ultima.getDinheiroMovimentado(), 0.001);
        assertEquals(Movimentacao.TipoDaMovimentacao.SAIDA, ultima.getTipoDaMovimentacao());
    }
}
