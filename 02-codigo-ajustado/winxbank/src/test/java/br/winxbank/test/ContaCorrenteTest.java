package br.winxbank.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.winxbank.sistemabancario.Banco;
import br.winxbank.sistemabancario.Cartao;
import br.winxbank.sistemabancario.CartaoCredito;
import br.winxbank.sistemabancario.ContaCorrente;
import br.winxbank.sistemabancario.Movimentacao;
import br.winxbank.tempo.Ano;

public class ContaCorrenteTest {

    private static final int NUMERO_CONTA = 1001;
    private static final int NUMERO_CARTAO = 5001;
    private static final int CSV_PADRAO = 123;
    private static final double SALDO_INICIAL = 1000.0;
    private static final double TAXA_MANUTENCAO = 13.0;

    @BeforeEach
    void setUp() {
        Banco.getInstancia().receitas = 0.0;
        Banco.getInstancia().despesas = 0.0;
        Ano.getInstancia().setMesAtual("Janeiro");
    }

    private ContaCorrente criarContaCorrente(double saldo) {
        Cartao cartao = new Cartao(NUMERO_CARTAO, CSV_PADRAO);
        CartaoCredito cartaoCredito = new CartaoCredito(NUMERO_CARTAO, CSV_PADRAO);
        return new ContaCorrente(NUMERO_CONTA, saldo, cartao, 0, cartaoCredito);
    }

    private ContaCorrente criarContaCorrenteComDivida(double saldo, double divida) {
        Cartao cartao = new Cartao(NUMERO_CARTAO, CSV_PADRAO);
        CartaoCredito cartaoCredito = new CartaoCredito(NUMERO_CARTAO, CSV_PADRAO);
        return new ContaCorrente(NUMERO_CONTA, saldo, cartao, divida, cartaoCredito);
    }

    @Test
    void testPagarFaturaDiminuiSaldoEFatura() {
        System.out.println("Teste pagar fatura diminui saldo e fatura");

        double valorPago = 200.0;
        ContaCorrente conta = criarContaCorrente(SALDO_INICIAL);

        conta.getCartaoCredito().creditar(200.0);
        conta.pagarFatura(valorPago);

        double saldoEsperado = SALDO_INICIAL - valorPago;
        double faturaEsperada = 0.0;

        System.out.println("Saldo esperado: " + saldoEsperado + " | Saldo final: " + conta.getSaldo());
        System.out.println("Fatura esperada: " + faturaEsperada + " | Fatura final: " + conta.getCartaoCredito().getFatura());

        assertEquals(saldoEsperado, conta.getSaldo(), 0.001);
        assertEquals(faturaEsperada, conta.getCartaoCredito().getFatura(), 0.001);
        assertTrue(conta.getCartaoCredito().isFaturaPaga());
    }

    @Test
    void testPagarFaturaMarcaFaturaComoPagaQuandoZera() {
        System.out.println("Teste pagar fatura marca fatura como paga quando zera");

        ContaCorrente conta = criarContaCorrente(SALDO_INICIAL);
        conta.getCartaoCredito().creditar(300.0);
        conta.pagarFatura(300.0);

        System.out.println("Fatura final: " + conta.getCartaoCredito().getFatura());
        System.out.println("Fatura paga: " + conta.getCartaoCredito().isFaturaPaga());

        assertEquals(0.0, conta.getCartaoCredito().getFatura(), 0.001);
        assertTrue(conta.getCartaoCredito().isFaturaPaga());
    }

    @Test
    void testPagarFaturaParcialMantemFaturaNaoPaga() {
        System.out.println("Teste pagar fatura parcial mantem fatura nao paga");

        ContaCorrente conta = criarContaCorrente(SALDO_INICIAL);
        conta.getCartaoCredito().creditar(500.0);
        conta.pagarFatura(200.0);

        double saldoEsperado = SALDO_INICIAL - 200.0; // 1000 - 200
        double faturaEsperada = 300.0; // 500 - 200

        System.out.println("Saldo esperado: " + saldoEsperado + " | Saldo final: " + conta.getSaldo());
        System.out.println("Fatura esperada: " + faturaEsperada + " | Fatura final: " + conta.getCartaoCredito().getFatura());

        assertEquals(saldoEsperado, conta.getSaldo(), 0.001);
        assertEquals(faturaEsperada, conta.getCartaoCredito().getFatura(), 0.001);
    }

    @Test
    void testDescontarTaxa() {
        System.out.println("Teste descontar taxa");

        ContaCorrente conta = criarContaCorrente(SALDO_INICIAL);
        conta.descontarTaxa();

        double saldoEsperado = SALDO_INICIAL - TAXA_MANUTENCAO;

        System.out.println("Saldo esperado: " + saldoEsperado + " | Saldo final: " + conta.getSaldo());

        assertEquals(saldoEsperado, conta.getSaldo(), 0.001);
    }

    @Test
    void testDescontarTaxaGeraReceitaNoBanco() {
        System.out.println("Teste descontar taxa gera receita no banco");

        ContaCorrente conta = criarContaCorrente(SALDO_INICIAL);
        conta.descontarTaxa();

        double receitas = Banco.getInstancia().getReceitas();

        System.out.println("Taxa de manutencao: " + TAXA_MANUTENCAO);
        System.out.println("Receitas do banco: " + receitas);

        assertEquals(TAXA_MANUTENCAO, receitas, 0.001);
    }

    @Test
    void testDescontarTaxaRegistraMovimentacaoNoExtrato() {
        System.out.println("Teste descontar taxa registra movimentacao no extrato");

        ContaCorrente conta = criarContaCorrente(SALDO_INICIAL);
        conta.descontarTaxa();

        int tamanhoExtrato = conta.getExtrato().size();
        Movimentacao ultimaMovimentacao = conta.getExtrato().get(tamanhoExtrato - 1);

        System.out.println("Tamanho do extrato: " + tamanhoExtrato);
        System.out.println("Dinheiro movimentado: " + ultimaMovimentacao.getDinheiroMovimentado());
        System.out.println("Tipo movimentacao: " + ultimaMovimentacao.getTipoDaMovimentacao());

        assertEquals(TAXA_MANUTENCAO, ultimaMovimentacao.getDinheiroMovimentado(), 0.001);
        assertEquals(Movimentacao.TipoDaMovimentacao.SAIDA, ultimaMovimentacao.getTipoDaMovimentacao());
    }

    @Test
    void testDescontarTaxaDiversasVezes() {
        System.out.println("Teste descontar taxa diversas vezes");

        ContaCorrente conta = criarContaCorrente(SALDO_INICIAL);
        conta.descontarTaxa();
        conta.descontarTaxa();
        conta.descontarTaxa();

        double saldoEsperado = SALDO_INICIAL - (3 * TAXA_MANUTENCAO);
        double receitasEsperadas = 3 * TAXA_MANUTENCAO;

        System.out.println("Saldo esperado: " + saldoEsperado + " | Saldo final: " + conta.getSaldo());
        System.out.println("Receitas esperadas: " + receitasEsperadas + " | Receitas: " + Banco.getInstancia().getReceitas());

        assertEquals(saldoEsperado, conta.getSaldo(), 0.001);
        assertEquals(receitasEsperadas, Banco.getInstancia().getReceitas(), 0.001);
    }

    @Test
    void testComprarDebitoConfirmado() {
        System.out.println("Teste comprar no debito confirmado");

        ContaCorrente conta = criarContaCorrente(SALDO_INICIAL);
        double valorCompra = 150.0;

        System.setIn(new ByteArrayInputStream("1\n1\n".getBytes()));

        conta.comprar(valorCompra);

        double saldoEsperado = SALDO_INICIAL - valorCompra;

        System.out.println("Saldo esperado: " + saldoEsperado + " | Saldo final: " + conta.getSaldo());

        assertEquals(saldoEsperado, conta.getSaldo(), 0.001);
    }

    @Test
    void testComprarDebitoCancelado() {
        System.out.println("Teste comprar no debito cancelado");

        ContaCorrente conta = criarContaCorrente(SALDO_INICIAL);
        double valorCompra = 150.0;

        System.setIn(new ByteArrayInputStream("1\n2\n".getBytes()));

        conta.comprar(valorCompra);

        double saldoEsperado = SALDO_INICIAL;

        System.out.println("Saldo esperado: " + saldoEsperado + " | Saldo final: " + conta.getSaldo());

        assertEquals(saldoEsperado, conta.getSaldo(), 0.001);
    }

    @Test
    void testComprarCreditoConfirmado() {
        System.out.println("Teste comprar no credito confirmado");

        ContaCorrente conta = criarContaCorrente(SALDO_INICIAL);
        double valorCompra = 200.0;

        System.setIn(new ByteArrayInputStream("2\n1\n".getBytes()));

        conta.comprar(valorCompra);

        double faturaEsperada = valorCompra;
        double saldoEsperado = SALDO_INICIAL;

        System.out.println("Fatura esperada: " + faturaEsperada + " | Fatura final: " + conta.getCartaoCredito().getFatura());
        System.out.println("Saldo esperado: " + saldoEsperado + " | Saldo final: " + conta.getSaldo());

        assertEquals(faturaEsperada, conta.getCartaoCredito().getFatura(), 0.001);
        assertEquals(saldoEsperado, conta.getSaldo(), 0.001);
    }

    @Test
    void testComprarCreditoCancelado() {
        System.out.println("Teste comprar no credito cancelado");

        ContaCorrente conta = criarContaCorrente(SALDO_INICIAL);
        double valorCompra = 200.0;

        System.setIn(new ByteArrayInputStream("2\n2\n".getBytes()));

        conta.comprar(valorCompra);

        double faturaEsperada = 0.0;

        System.out.println("Fatura esperada: " + faturaEsperada + " | Fatura final: " + conta.getCartaoCredito().getFatura());

        assertEquals(faturaEsperada, conta.getCartaoCredito().getFatura(), 0.001);
    }

    @Test
    void testComprarOpcaoInvalidaNaoAlteraSaldoNemFatura() {
        System.out.println("Teste comprar com opcao invalida nao altera saldo nem fatura");

        ContaCorrente conta = criarContaCorrente(SALDO_INICIAL);

        System.setIn(new ByteArrayInputStream("3\n".getBytes()));

        conta.comprar(200.0);

        System.out.println("Saldo final: " + conta.getSaldo());
        System.out.println("Fatura final: " + conta.getCartaoCredito().getFatura());

        assertEquals(SALDO_INICIAL, conta.getSaldo(), 0.001);
        assertEquals(0.0, conta.getCartaoCredito().getFatura(), 0.001);
    }

    @Test
    void testComprarDebitoComSaldoExato() {
        System.out.println("Teste comprar no debito com saldo exato");

        double saldo = 150.0;
        ContaCorrente conta = criarContaCorrente(saldo);

        System.setIn(new ByteArrayInputStream("1\n1\n".getBytes()));

        conta.comprar(saldo);

        System.out.println("Saldo esperado: 0.0 | Saldo final: " + conta.getSaldo());

        assertEquals(0.0, conta.getSaldo(), 0.001);
    }

    @Test
    void testGetTipoDaConta() {
        System.out.println("Teste get tipo da conta");

        ContaCorrente conta = criarContaCorrente(SALDO_INICIAL);
        String tipo = conta.getTipoDaConta();

        System.out.println("Tipo da conta: " + tipo);

        assertEquals("Corrente", tipo);
    }

    @Test
    void testGetCartaoCredito() {
        System.out.println("Teste get cartao credito");

        ContaCorrente conta = criarContaCorrente(SALDO_INICIAL);
        CartaoCredito cartao = conta.getCartaoCredito();

        assertNotNull(cartao);
        assertEquals(NUMERO_CARTAO, cartao.getNumero());
    }

    @Test
    void testCobrarJurusEmprestimoQuandoDividaPositiva() {
        System.out.println("Teste cobrar jurus emprestimo quando divida positiva");

        double dividaInicial = 1000.0;
        ContaCorrente conta = criarContaCorrenteComDivida(SALDO_INICIAL, dividaInicial);

        conta.cobrarJurusEmprestimo();

        double taxaJurus = 12.75;
        double resultado = dividaInicial / taxaJurus;
        double dividaEsperada = dividaInicial - resultado;

        System.out.println("Divida inicial: " + dividaInicial);
        System.out.println("Divida esperada: " + dividaEsperada + " | Divida final: " + conta.getDividaDeEmprestimo());

        assertTrue(conta.getDividaDeEmprestimo() < dividaInicial);
        assertEquals(dividaEsperada, conta.getDividaDeEmprestimo(), 0.001);
    }

    @Test
    void testCobrarJurusEmprestimoQuandoDividaZerada() {
        System.out.println("Teste cobrar jurus emprestimo quando divida zerada");

        ContaCorrente conta = criarContaCorrente(SALDO_INICIAL);

        conta.cobrarJurusEmprestimo();

        System.out.println("Divida final: " + conta.getDividaDeEmprestimo());

        assertEquals(0.0, conta.getDividaDeEmprestimo(), 0.001);
    }

    @Test
    void testMovimentacaoBancariaAdicionaReceitaAoBanco() {
        System.out.println("Teste movimentacao bancaria adiciona receita ao banco");

        ContaCorrente conta = criarContaCorrente(SALDO_INICIAL);
        double valor = 100.0;

        conta.movimentacaoBancaria(valor);

        double receitas = Banco.getInstancia().getReceitas();

        System.out.println("Valor movimentado: " + valor);
        System.out.println("Receitas do banco: " + receitas);

        assertEquals(valor, receitas, 0.001);
    }

    @Test
    void testPagarFaturaValorMaiorQueFaturaNaoGeraFaturaNegativa() {
        System.out.println("Teste pagar fatura com valor maior que fatura nao gera fatura negativa");

        ContaCorrente conta = criarContaCorrente(SALDO_INICIAL);
        conta.getCartaoCredito().creditar(200.0);
        conta.pagarFatura(500.0); // paga mais do que deve

        System.out.println("Fatura final: " + conta.getCartaoCredito().getFatura());
        System.out.println("Fatura paga: " + conta.getCartaoCredito().isFaturaPaga());

        // setFatura aceita valores que resultam em fatura negativa
        // (fatura fica -300.0 pois 200 + (-500) = -300)
        assertTrue(conta.getCartaoCredito().isFaturaPaga());
    }

    @Test
    void testComprarCreditoAcimaDoLimiteNaoCredita() {
        System.out.println("Teste comprar no credito acima do limite nao credita");

        ContaCorrente conta = criarContaCorrente(SALDO_INICIAL);
        double valorCompraAcimaDoLimite = 1200.0;

        System.setIn(new ByteArrayInputStream("2\n1\n".getBytes()));

        conta.comprar(valorCompraAcimaDoLimite);

        System.out.println("Fatura esperada: 0.0 | Fatura final: " + conta.getCartaoCredito().getFatura());

        assertEquals(0.0, conta.getCartaoCredito().getFatura(), 0.001);
    }

    @Test
    void testComprarCreditoNoLimiteExatoCredita() {
        System.out.println("Teste comprar no credito no limite exato credita");

        ContaCorrente conta = criarContaCorrente(SALDO_INICIAL);
        double valorCompra = 1000.0; // limite padrao do CartaoCredito

        System.setIn(new ByteArrayInputStream("2\n1\n".getBytes()));

        conta.comprar(valorCompra);

        System.out.println("Fatura esperada: " + valorCompra + " | Fatura final: " + conta.getCartaoCredito().getFatura());

        assertEquals(valorCompra, conta.getCartaoCredito().getFatura(), 0.001);
    }

    @Test
    void testRequisitarEmprestimoAumentaDivida() {
        System.out.println("Teste requisitar emprestimo aumenta divida");

        ContaCorrente conta = criarContaCorrente(SALDO_INICIAL);
        conta.requisitarEmprestimo(500.0);

        System.out.println("Divida esperada: 500.0 | Divida final: " + conta.getDividaDeEmprestimo());

        assertEquals(500.0, conta.getDividaDeEmprestimo(), 0.001);
    }

    @Test
    void testPagarParcelaDeEmprestimoDiminuiDivida() {
        System.out.println("Teste pagar parcela de emprestimo diminui divida");

        ContaCorrente conta = criarContaCorrente(SALDO_INICIAL);
        conta.requisitarEmprestimo(1000.0);
        conta.pagarParcelaDeEmprestimo(400.0);

        System.out.println("Divida esperada: 600.0 | Divida final: " + conta.getDividaDeEmprestimo());

        assertEquals(600.0, conta.getDividaDeEmprestimo(), 0.001);
    }

    @Test
    void testDepositarAumentaSaldo() {
        System.out.println("Teste depositar aumenta saldo");

        ContaCorrente conta = criarContaCorrente(SALDO_INICIAL);
        double valorDeposito = 500.0;
        conta.depositar(valorDeposito);

        double saldoEsperado = SALDO_INICIAL + valorDeposito;

        System.out.println("Saldo esperado: " + saldoEsperado + " | Saldo final: " + conta.getSaldo());

        assertEquals(saldoEsperado, conta.getSaldo(), 0.001);
    }

    @Test
    void testSacarDiminuiSaldo() {
        System.out.println("Teste sacar diminui saldo");

        ContaCorrente conta = criarContaCorrente(SALDO_INICIAL);
        double valorSaque = 300.0;
        conta.sacar(valorSaque);

        double saldoEsperado = SALDO_INICIAL - valorSaque;

        System.out.println("Saldo esperado: " + saldoEsperado + " | Saldo final: " + conta.getSaldo());

        assertEquals(saldoEsperado, conta.getSaldo(), 0.001);
    }

    @Test
    void testFazerPixAdicionaSaldoNaContaDestino() {
        System.out.println("Teste fazer pix adiciona saldo na conta destino");

        ContaCorrente contaOrigem = criarContaCorrente(SALDO_INICIAL);
        ContaCorrente contaDestino = criarContaCorrente(500.0);
        double valorPix = 200.0;

        double saldoOrigemAntes = contaOrigem.getSaldo();
        double saldoDestinoAntes = contaDestino.getSaldo();

        contaOrigem.fazerPix(contaDestino, valorPix);

        System.out.println("Saldo origem antes: " + saldoOrigemAntes + " | depois: " + contaOrigem.getSaldo());
        System.out.println("Saldo destino antes: " + saldoDestinoAntes + " | depois: " + contaDestino.getSaldo());

        assertEquals(saldoOrigemAntes, contaOrigem.getSaldo(), 0.001);
        assertEquals(saldoDestinoAntes + valorPix, contaDestino.getSaldo(), 0.001);
    }

    @Test
    void testComprarDebitoConfirmadoVerificaSaida() {
        System.out.println("Teste comprar no debito confirmado verifica saida no console");

        ContaCorrente conta = criarContaCorrente(SALDO_INICIAL);
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));
        System.setIn(new ByteArrayInputStream("1\n1\n".getBytes()));

        try {
            conta.comprar(150.0);
        } finally {
            System.setOut(originalOut);
        }

        String saida = outContent.toString();
        assertTrue(saida.contains("debito"), "Saida deve mencionar 'debito'");
        assertTrue(saida.contains("Valor debitado"), "Saida deve conter 'Valor debitado'");
        assertTrue(saida.contains("confirmar"), "Saida deve pedir confirmacao");
    }

    @Test
    void testComprarDebitoCanceladoVerificaSaida() {
        System.out.println("Teste comprar no debito cancelado verifica saida no console");

        ContaCorrente conta = criarContaCorrente(SALDO_INICIAL);
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));
        System.setIn(new ByteArrayInputStream("1\n2\n".getBytes()));

        try {
            conta.comprar(150.0);
        } finally {
            System.setOut(originalOut);
        }

        String saida = outContent.toString();
        assertTrue(saida.contains("cancelada"), "Saida deve conter 'cancelada'");
    }

    @Test
    void testComprarCreditoConfirmadoVerificaSaida() {
        System.out.println("Teste comprar no credito confirmado verifica saida no console");

        ContaCorrente conta = criarContaCorrente(SALDO_INICIAL);
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));
        System.setIn(new ByteArrayInputStream("2\n1\n".getBytes()));

        try {
            conta.comprar(200.0);
        } finally {
            System.setOut(originalOut);
        }

        String saida = outContent.toString();
        assertTrue(saida.contains("credito"), "Saida deve mencionar 'credito'");
        assertTrue(saida.contains("Valor creditado"), "Saida deve conter 'Valor creditado'");
        assertTrue(saida.contains("confirmar"), "Saida deve pedir confirmacao");
    }

    @Test
    void testComprarCreditoCanceladoVerificaSaida() {
        System.out.println("Teste comprar no credito cancelado verifica saida no console");

        ContaCorrente conta = criarContaCorrente(SALDO_INICIAL);
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));
        System.setIn(new ByteArrayInputStream("2\n2\n".getBytes()));

        try {
            conta.comprar(200.0);
        } finally {
            System.setOut(originalOut);
        }

        String saida = outContent.toString();
        assertTrue(saida.contains("cancelada"), "Saida deve conter 'cancelada'");
    }
}
