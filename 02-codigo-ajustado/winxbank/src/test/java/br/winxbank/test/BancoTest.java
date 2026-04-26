package br.winxbank.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.winxbank.exception.BankAccountNotFoundException;
import br.winxbank.sistemabancario.Banco;
import br.winxbank.sistemabancario.Cartao;
import br.winxbank.sistemabancario.CartaoCredito;
import br.winxbank.sistemabancario.Conta;
import br.winxbank.sistemabancario.ContaCorrente;
import br.winxbank.sistemabancario.ContaPoupanca;
import br.winxbank.sistemaclientes.Cliente;
import br.winxbank.sistemaclientes.RegistroDeClientes;
import br.winxbank.tempo.Ano;

public class BancoTest {

    private static final int NUMERO_CONTA = 12345;
    private static final int NUMERO_CARTAO = 1234;
    private static final int CSV_PADRAO = 999;
    private static final double SALDO_INICIAL = 500.0;
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
        return new ContaPoupanca(NUMERO_CONTA, saldo, cartao, 0);
    }

    @Test
    void testGetInstanciaRetornaMesmaInstancia() {
        System.out.println("Teste get instancia retorna mesma instancia");

        Banco instancia1 = Banco.getInstancia();
        Banco instancia2 = Banco.getInstancia();

        assertSame(instancia1, instancia2);
    }

    @Test
    void testSetReceitasComValorPositivo() {
        System.out.println("Teste set receitas com valor positivo");

        double valorReceita = 200.0;

        Banco.getInstancia().setReceitas(valorReceita);

        double receitas = Banco.getInstancia().getReceitas();

        System.out.println("Valor adicionado: " + valorReceita);
        System.out.println("Receitas: " + receitas);

        assertEquals(200.0, receitas, 0.001);
    }

    @Test
    void testSetReceitasNaoAlteraComValorNegativo() {
        System.out.println("Teste set receitas nao altera com valor negativo");

        double valorNegativo = -100.0;

        Banco.getInstancia().setReceitas(valorNegativo);

        double receitas = Banco.getInstancia().getReceitas();

        System.out.println("Valor negativo: " + valorNegativo);
        System.out.println("Receitas: " + receitas);

        assertEquals(0.0, receitas, 0.001);
    }

    @Test
    void testSetDespesasComValorPositivo() {
        System.out.println("Teste set despesas com valor positivo");

        double valorDespesa = 150.0;

        Banco.getInstancia().setDespesas(valorDespesa);

        double despesas = Banco.getInstancia().getDespesas();

        System.out.println("Valor adicionado: " + valorDespesa);
        System.out.println("Despesas: " + despesas);

        assertEquals(150.0, despesas, 0.001);
    }

    @Test
    void testSetDespesasNaoAlteraComValorNegativo() {
        System.out.println("Teste set despesas nao altera com valor negativo");

        double valorNegativo = -50.0;

        Banco.getInstancia().setDespesas(valorNegativo);

        double despesas = Banco.getInstancia().getDespesas();

        System.out.println("Valor negativo: " + valorNegativo);
        System.out.println("Despesas: " + despesas);

        assertEquals(0.0, despesas, 0.001);
    }

    @Test
    void testSetBancoCopiaDadosDeOutroBanco() {
        System.out.println("Teste set banco copia dados de outro banco");

        Banco bancoCopia = new Banco();
        bancoCopia.receitas = 1000.0;
        bancoCopia.despesas = 300.0;

        Banco.getInstancia().setBanco(bancoCopia);

        double receitas = Banco.getInstancia().getReceitas();
        double despesas = Banco.getInstancia().getDespesas();

        System.out.println("Receitas esperadas: 1000.0 | Receitas: " + receitas);
        System.out.println("Despesas esperadas: 300.0 | Despesas: " + despesas);

        assertEquals(1000.0, receitas, 0.001);
        assertEquals(300.0, despesas, 0.001);
    }

    @Test
    void testFecharContaLancaExcecaoQuandoContaNaoEncontrada() {
        System.out.println("Teste fechar conta lanca excecao quando conta nao encontrada");

        Cliente cliente = new Cliente("Teste", "000.000.000-00");

        System.setIn(new ByteArrayInputStream("99999\n".getBytes()));

        System.out.println("Cliente sem contas registradas, numero buscado: 99999");

        assertThrows(BankAccountNotFoundException.class, () -> {
            Banco.getInstancia().fecharConta(cliente);
        });
    }

    @Test
    void testFecharContaRemoveContaDoCliente() {
        System.out.println("Teste fechar conta remove conta do cliente");

        Cliente cliente = new Cliente("Teste", "000.000.000-00");
        ContaCorrente conta = criarContaCorrente(SALDO_INICIAL);
        cliente.setContas(conta);

        System.setIn(new ByteArrayInputStream((NUMERO_CONTA + "\n").getBytes()));

        Banco.getInstancia().fecharConta(cliente);

        int quantidadeContas = cliente.getContas().size();

        System.out.println("Quantidade de contas apos fechar: " + quantidadeContas);

        assertEquals(0, quantidadeContas);
    }

    @Test
    void testMovimentarEntreBancoContaComListaVazia() {
        System.out.println("Teste movimentar entre banco conta com lista de clientes vazia");

        Banco.getInstancia().movimentarEntreBancoConta();

        double receitas = Banco.getInstancia().getReceitas();
        double despesas = Banco.getInstancia().getDespesas();

        System.out.println("Receitas: " + receitas);
        System.out.println("Despesas: " + despesas);

        assertEquals(0.0, receitas, 0.001);
        assertEquals(0.0, despesas, 0.001);
    }

    @Test
    void testMovimentarEntreBancoContaDescontaTaxaDaContaCorrente() {
        System.out.println("Teste movimentar entre banco conta desconta taxa da conta corrente");

        Cliente cliente = new Cliente("Teste", "000.000.000-00");
        ContaCorrente conta = criarContaCorrente(SALDO_INICIAL);
        cliente.setContas(conta);
        RegistroDeClientes.getInstancia().getClientes().add(cliente);

        Banco.getInstancia().movimentarEntreBancoConta();

        double saldoEsperado = SALDO_INICIAL - TAXA_MANUTENCAO;
        double saldoFinal = conta.getSaldo();

        System.out.println("Saldo inicial: " + SALDO_INICIAL);
        System.out.println("Taxa de manutencao: " + TAXA_MANUTENCAO);
        System.out.println("Saldo esperado: " + saldoEsperado + " | Saldo final: " + saldoFinal);

        assertEquals(saldoEsperado, saldoFinal, 0.001);
    }

    @Test
    void testMovimentarEntreBancoContaGeraReceitaComTaxaDaContaCorrente() {
        System.out.println("Teste movimentar entre banco conta gera receita com taxa da conta corrente");

        Cliente cliente = new Cliente("Teste", "000.000.000-00");
        ContaCorrente conta = criarContaCorrente(SALDO_INICIAL);
        cliente.setContas(conta);
        RegistroDeClientes.getInstancia().getClientes().add(cliente);

        Banco.getInstancia().movimentarEntreBancoConta();

        double receitas = Banco.getInstancia().getReceitas();

        System.out.println("Taxa de manutencao cobrada: " + TAXA_MANUTENCAO);
        System.out.println("Receitas do banco: " + receitas);

        assertEquals(TAXA_MANUTENCAO, receitas, 0.001);
    }

    @Test
    void testMovimentarEntreBancoContaAcrescentaRendimentoNaPoupanca() {
        System.out.println("Teste movimentar entre banco conta acrescenta rendimento na poupanca");

        Cliente cliente = new Cliente("Teste", "000.000.000-00");
        ContaPoupanca conta = criarContaPoupanca(SALDO_INICIAL);
        cliente.setContas(conta);
        RegistroDeClientes.getInstancia().getClientes().add(cliente);

        Banco.getInstancia().movimentarEntreBancoConta();

        double rendimento = SALDO_INICIAL / RENDIMENTO_MENSAL_POUPANCA;
        double saldoEsperado = SALDO_INICIAL + rendimento;
        double saldoFinal = conta.getSaldo();

        System.out.println("Saldo inicial: " + SALDO_INICIAL);
        System.out.println("Rendimento acrescentado: " + rendimento);
        System.out.println("Saldo esperado: " + saldoEsperado + " | Saldo final: " + saldoFinal);

        assertEquals(saldoEsperado, saldoFinal, 0.001);
    }

    @Test
    void testPrintarBancoNaoDeveModificarDespesasQuandoDespesasMaiorQueReceitas() {
        System.out.println("Teste printarBanco nao deve modificar despesas quando despesas > receitas");

        double despesasIniciais = 1000.0;

        Banco.getInstancia().setDespesas(despesasIniciais);

        Banco.getInstancia().printarBanco();

        double despesasApos = Banco.getInstancia().getDespesas();

        System.out.println("Despesas antes: " + despesasIniciais);
        System.out.println("Despesas apos printarBanco: " + despesasApos);

        assertEquals(despesasIniciais, despesasApos, 0.001,
                "printarBanco nao deve alterar o valor de despesas");
    }

    @Test
    void testPrintarBancoNaoDeveModificarDespesasQuandoReceitasMaiorOuIgual() {
        System.out.println("Teste printarBanco nao deve modificar despesas quando receitas >= despesas");

        Banco.getInstancia().setDespesas(300.0);
        Banco.getInstancia().setReceitas(300.0);
        // receitas == despesas -> condicao despesas > receitas e falsa, nao entra no if

        Banco.getInstancia().printarBanco();

        double despesasApos = Banco.getInstancia().getDespesas();

        System.out.println("Despesas apos printarBanco: " + despesasApos);

        assertEquals(300.0, despesasApos, 0.001,
                "printarBanco nao deve alterar despesas quando receitas >= despesas");
    }

    @Test
    void testAbrirNovaContaComOpcaoInvalidaNaoDeveRetornarNull() {
        System.out.println("Teste abrirNovaConta com opcao invalida nao deve retornar null");

        // Opcao 3 e invalida (diferente de 1 e 2)
        System.setIn(new ByteArrayInputStream("3\n".getBytes()));

        Conta resultado = Banco.getInstancia().abrirNovaConta();

        System.out.println("Resultado retornado: " + resultado);

        assertTrue(resultado != null,
                "abrirNovaConta nao deve retornar null para opcao invalida, deve lancar excecao");
    }

    @Test
    void testAbrirNovaContaComOpcaoZeroNaoDeveRetornarNull() {
        System.out.println("Teste abrirNovaConta com opcao zero nao deve retornar null");

        System.setIn(new ByteArrayInputStream("0\n".getBytes()));

        Conta resultado = Banco.getInstancia().abrirNovaConta();

        System.out.println("Resultado retornado: " + resultado);

        assertTrue(resultado != null,
                "abrirNovaConta nao deve retornar null para opcao invalida, deve lancar excecao");
    }
}
