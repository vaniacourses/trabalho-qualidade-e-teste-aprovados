package br.winxbank.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

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

/**
 * Testes unitarios da classe Banco.
 *
 * Estrategia (Entrega 2 - tarefa 1.2):
 *  - Os singletons RegistroDeClientes e Ano sao isolados com Mockito.mockStatic,
 *    de modo que o teste de movimentarEntreBancoConta nao depende do estado real
 *    do registro de clientes nem do calendario do sistema.
 *  - abrirNovaConta tipo 1 e tipo 2 sao testados simulando a entrada do console.
 *
 * Constantes do dominio (interface OperacoesAutomaticas):
 *  taxaManutencaoConta = 13.00 | rendimentoMensalPoupanca = 0.8 | taxaJuros = 12.75
 */
class BancoTest {

    private static final int NUMERO_CONTA = 12345;
    private static final int NUMERO_CARTAO = 1234;
    private static final int CSV_PADRAO = 999;
    private static final double SALDO_INICIAL = 500.0;
    private static final double TAXA_MANUTENCAO = 13.0;
    private static final double RENDIMENTO_MENSAL_POUPANCA = 0.8;
    private static final double TAXA_JURUS = 12.75;
    private static final double DELTA = 0.001;

    private InputStream systemInOriginal;
    private PrintStream systemOutOriginal;
    private ByteArrayOutputStream saidaConsole;

    @BeforeEach
    void setUp() {
        Banco.getInstancia().receitas = 0.0;
        Banco.getInstancia().despesas = 0.0;
        systemInOriginal = System.in;
        // Captura a saida do console para permitir verificar as mensagens ao usuario
        systemOutOriginal = System.out;
        saidaConsole = new ByteArrayOutputStream();
        System.setOut(new PrintStream(saidaConsole));
    }

    @AfterEach
    void tearDown() {
        System.setIn(systemInOriginal);
        System.setOut(systemOutOriginal);
    }

    private String saida() {
        return saidaConsole.toString();
    }

    // ---------- Helpers ----------

    private ContaCorrente criarContaCorrente(double saldo) {
        Cartao cartao = new Cartao(NUMERO_CARTAO, CSV_PADRAO);
        CartaoCredito cartaoCredito = new CartaoCredito(NUMERO_CARTAO, CSV_PADRAO);
        return new ContaCorrente(NUMERO_CONTA, saldo, cartao, 0, cartaoCredito);
    }

    private ContaPoupanca criarContaPoupanca(double saldo) {
        Cartao cartao = new Cartao(NUMERO_CARTAO, CSV_PADRAO);
        return new ContaPoupanca(NUMERO_CONTA, saldo, cartao, 0);
    }

    /** Monta uma lista de clientes (cada conta pertence a um cliente). */
    private ArrayList<Cliente> clientesCom(Conta... contas) {
        ArrayList<Cliente> clientes = new ArrayList<>();
        Cliente cliente = new Cliente("Teste", "000.000.000-00");
        for (Conta conta : contas) {
            cliente.setContas(conta);
        }
        clientes.add(cliente);
        return clientes;
    }

    // ---------- Singleton e setters ----------

    @Test
    @DisplayName("getInstancia retorna sempre a mesma instancia (singleton)")
    void testGetInstanciaRetornaMesmaInstancia() {
        assertSame(Banco.getInstancia(), Banco.getInstancia());
    }

    @Test
    @DisplayName("setReceitas soma quando o valor e positivo")
    void testSetReceitasComValorPositivo() {
        Banco.getInstancia().setReceitas(200.0);
        assertEquals(200.0, Banco.getInstancia().getReceitas(), DELTA);
    }

    @Test
    @DisplayName("setReceitas ignora valores negativos")
    void testSetReceitasNaoAlteraComValorNegativo() {
        Banco.getInstancia().setReceitas(-100.0);
        assertEquals(0.0, Banco.getInstancia().getReceitas(), DELTA);
    }

    @Test
    @DisplayName("setDespesas soma quando o valor e positivo")
    void testSetDespesasComValorPositivo() {
        Banco.getInstancia().setDespesas(150.0);
        assertEquals(150.0, Banco.getInstancia().getDespesas(), DELTA);
    }

    @Test
    @DisplayName("setDespesas ignora valores negativos")
    void testSetDespesasNaoAlteraComValorNegativo() {
        Banco.getInstancia().setDespesas(-50.0);
        assertEquals(0.0, Banco.getInstancia().getDespesas(), DELTA);
    }

    @Test
    @DisplayName("setBanco copia receitas e despesas de outro banco")
    void testSetBancoCopiaDadosDeOutroBanco() {
        Banco bancoCopia = new Banco();
        bancoCopia.receitas = 1000.0;
        bancoCopia.despesas = 300.0;

        Banco.getInstancia().setBanco(bancoCopia);

        assertEquals(1000.0, Banco.getInstancia().getReceitas(), DELTA);
        assertEquals(300.0, Banco.getInstancia().getDespesas(), DELTA);
    }

    // ---------- fecharConta ----------

    @Test
    @DisplayName("fecharConta lanca excecao quando a conta nao e encontrada")
    void testFecharContaLancaExcecaoQuandoContaNaoEncontrada() {
        Cliente cliente = new Cliente("Teste", "000.000.000-00");
        System.setIn(new ByteArrayInputStream("99999\n".getBytes()));

        assertThrows(BankAccountNotFoundException.class,
                () -> Banco.getInstancia().fecharConta(cliente));
    }

    @Test
    @DisplayName("fecharConta remove a conta do cliente quando encontrada")
    void testFecharContaRemoveContaDoCliente() {
        Cliente cliente = new Cliente("Teste", "000.000.000-00");
        cliente.setContas(criarContaCorrente(SALDO_INICIAL));
        System.setIn(new ByteArrayInputStream((NUMERO_CONTA + "\n").getBytes()));

        Banco.getInstancia().fecharConta(cliente);

        assertEquals(0, cliente.getContas().size());
        assertTrue(saida().contains("Digite o numero da conta"), "deve pedir o numero da conta");
        assertTrue(saida().contains("apagada com sucesso"), "deve confirmar a remocao");
    }

    // ---------- abrirNovaConta (input simulado) ----------

    @Test
    @DisplayName("abrirNovaConta tipo 1 cria uma ContaCorrente com o saldo informado")
    void testAbrirNovaContaTipo1CriaContaCorrente() {
        System.setIn(new ByteArrayInputStream("1\n500\n".getBytes()));

        Conta conta = Banco.getInstancia().abrirNovaConta();

        assertInstanceOf(ContaCorrente.class, conta);
        assertEquals(500.0, conta.getSaldo(), DELTA);
        assertTrue(saida().contains("Qual tipo de conta"), "deve perguntar o tipo de conta");
        assertTrue(saida().contains("criando uma conta corrente"), "deve indicar criacao de conta corrente");
        assertTrue(saida().contains("Digite o saldo"), "deve solicitar o saldo");
        assertTrue(saida().contains("conta corrente foi criada com sucesso"), "deve confirmar a criacao");
    }

    @Test
    @DisplayName("abrirNovaConta tipo 2 cria uma ContaPoupanca com o saldo informado")
    void testAbrirNovaContaTipo2CriaContaPoupanca() {
        System.setIn(new ByteArrayInputStream("2\n300\n".getBytes()));

        Conta conta = Banco.getInstancia().abrirNovaConta();

        assertInstanceOf(ContaPoupanca.class, conta);
        assertEquals(300.0, conta.getSaldo(), DELTA);
        assertTrue(saida().contains("criando uma conta poupanca"), "deve indicar criacao de conta poupanca");
        assertTrue(saida().contains("Digite o saldo"), "deve solicitar o saldo");
        assertTrue(saida().contains("conta poupanca foi criada com sucesso"), "deve confirmar a criacao");
    }

    @Test
    @DisplayName("abrirNovaConta com opcao invalida retorna null")
    void testAbrirNovaContaComOpcaoInvalidaRetornaNull() {
        System.setIn(new ByteArrayInputStream("3\n".getBytes()));

        Conta conta = Banco.getInstancia().abrirNovaConta();

        assertNull(conta, "opcao diferente de 1 ou 2 deve retornar null");
    }

    // ---------- movimentarEntreBancoConta (singletons mockados) ----------

    @Test
    @DisplayName("movimentar com lista de clientes vazia nao altera receitas/despesas")
    void testMovimentarComListaVaziaNaoAltera() {
        RegistroDeClientes registroMock = mock(RegistroDeClientes.class);
        when(registroMock.getClientes()).thenReturn(new ArrayList<>());

        try (MockedStatic<RegistroDeClientes> registro = mockStatic(RegistroDeClientes.class)) {
            registro.when(RegistroDeClientes::getInstancia).thenReturn(registroMock);

            Banco.getInstancia().movimentarEntreBancoConta();
        }

        assertEquals(0.0, Banco.getInstancia().getReceitas(), DELTA);
        assertEquals(0.0, Banco.getInstancia().getDespesas(), DELTA);
    }

    @Test
    @DisplayName("movimentar desconta a taxa de manutencao da conta corrente e gera receita")
    void testMovimentarDescontaTaxaContaCorrente() {
        ContaCorrente conta = criarContaCorrente(SALDO_INICIAL);
        RegistroDeClientes registroMock = mock(RegistroDeClientes.class);
        when(registroMock.getClientes()).thenReturn(clientesCom(conta));

        try (MockedStatic<RegistroDeClientes> registro = mockStatic(RegistroDeClientes.class)) {
            registro.when(RegistroDeClientes::getInstancia).thenReturn(registroMock);

            Banco.getInstancia().movimentarEntreBancoConta();
        }

        assertEquals(SALDO_INICIAL - TAXA_MANUTENCAO, conta.getSaldo(), DELTA);
        assertEquals(TAXA_MANUTENCAO, Banco.getInstancia().getReceitas(), DELTA);
    }

    @Test
    @DisplayName("movimentar acrescenta rendimento na poupanca e gera despesa")
    void testMovimentarAcrescentaRendimentoPoupanca() {
        ContaPoupanca conta = criarContaPoupanca(SALDO_INICIAL);
        RegistroDeClientes registroMock = mock(RegistroDeClientes.class);
        when(registroMock.getClientes()).thenReturn(clientesCom(conta));

        try (MockedStatic<RegistroDeClientes> registro = mockStatic(RegistroDeClientes.class)) {
            registro.when(RegistroDeClientes::getInstancia).thenReturn(registroMock);

            Banco.getInstancia().movimentarEntreBancoConta();
        }

        double rendimento = SALDO_INICIAL / RENDIMENTO_MENSAL_POUPANCA;
        assertEquals(SALDO_INICIAL + rendimento, conta.getSaldo(), DELTA);
        assertEquals(rendimento, Banco.getInstancia().getDespesas(), DELTA);
    }

    @Test
    @DisplayName("movimentar com conta mista atualiza receita (corrente) e despesa (poupanca)")
    void testMovimentarContaMista() {
        ContaCorrente corrente = criarContaCorrente(SALDO_INICIAL);
        ContaPoupanca poupanca = criarContaPoupanca(SALDO_INICIAL);
        RegistroDeClientes registroMock = mock(RegistroDeClientes.class);
        when(registroMock.getClientes()).thenReturn(clientesCom(corrente, poupanca));

        try (MockedStatic<RegistroDeClientes> registro = mockStatic(RegistroDeClientes.class)) {
            registro.when(RegistroDeClientes::getInstancia).thenReturn(registroMock);

            Banco.getInstancia().movimentarEntreBancoConta();
        }

        assertEquals(TAXA_MANUTENCAO, Banco.getInstancia().getReceitas(), DELTA);
        assertEquals(SALDO_INICIAL / RENDIMENTO_MENSAL_POUPANCA, Banco.getInstancia().getDespesas(), DELTA);
    }

    @Test
    @DisplayName("movimentar com fatura > 0 cobra juros do cartao (ramo verdadeiro)")
    void testMovimentarComFaturaPositivaCobraJuros() {
        ContaCorrente conta = criarContaCorrente(SALDO_INICIAL);
        // fatura = 100 (dentro do limite de 1000); mes da fatura = 0 (Janeiro)
        conta.getCartaoCredito().setFatura(100.0);

        RegistroDeClientes registroMock = mock(RegistroDeClientes.class);
        when(registroMock.getClientes()).thenReturn(clientesCom(conta));

        Ano anoMock = mock(Ano.class);
        when(anoMock.getIndexMesAtual()).thenReturn(2); // mes atual > mes da fatura -> cobra juros

        try (MockedStatic<RegistroDeClientes> registro = mockStatic(RegistroDeClientes.class);
             MockedStatic<Ano> ano = mockStatic(Ano.class)) {
            registro.when(RegistroDeClientes::getInstancia).thenReturn(registroMock);
            ano.when(Ano::getInstancia).thenReturn(anoMock);

            Banco.getInstancia().movimentarEntreBancoConta();
        }

        // fatura passa de 100 para 100 * 12.75 = 1275
        double faturaEsperada = 100.0 * TAXA_JURUS;
        assertEquals(faturaEsperada, conta.getCartaoCredito().getFatura(), DELTA);

        // receita = taxa de manutencao (13) + juros cobrados (1275 - 100 = 1175)
        double receitaEsperada = TAXA_MANUTENCAO + (faturaEsperada - 100.0);
        assertEquals(receitaEsperada, Banco.getInstancia().getReceitas(), DELTA);
    }

    @Test
    @DisplayName("movimentar com fatura = 0 nao cobra juros do cartao (ramo falso)")
    void testMovimentarComFaturaZeroNaoCobraJuros() {
        ContaCorrente conta = criarContaCorrente(SALDO_INICIAL); // fatura default = 0
        RegistroDeClientes registroMock = mock(RegistroDeClientes.class);
        when(registroMock.getClientes()).thenReturn(clientesCom(conta));

        try (MockedStatic<RegistroDeClientes> registro = mockStatic(RegistroDeClientes.class)) {
            registro.when(RegistroDeClientes::getInstancia).thenReturn(registroMock);

            Banco.getInstancia().movimentarEntreBancoConta();
        }

        assertEquals(0.0, conta.getCartaoCredito().getFatura(), DELTA);
        assertEquals(TAXA_MANUTENCAO, Banco.getInstancia().getReceitas(), DELTA);
    }

    @Test
    @DisplayName("movimentar cobra juros do emprestimo quando ha divida (saldo devedor diminui)")
    void testMovimentarCobraJurosEmprestimo() {
        Cartao cartao = new Cartao(NUMERO_CARTAO, CSV_PADRAO);
        CartaoCredito cartaoCredito = new CartaoCredito(NUMERO_CARTAO, CSV_PADRAO);
        ContaCorrente conta = new ContaCorrente(NUMERO_CONTA, SALDO_INICIAL, cartao, 1000.0, cartaoCredito);

        RegistroDeClientes registroMock = mock(RegistroDeClientes.class);
        when(registroMock.getClientes()).thenReturn(clientesCom(conta));

        try (MockedStatic<RegistroDeClientes> registro = mockStatic(RegistroDeClientes.class)) {
            registro.when(RegistroDeClientes::getInstancia).thenReturn(registroMock);

            Banco.getInstancia().movimentarEntreBancoConta();
        }

        double dividaEsperada = 1000.0 - (1000.0 / TAXA_JURUS);
        assertEquals(dividaEsperada, conta.getDividaDeEmprestimo(), DELTA);
    }

    // ---------- printarBanco ----------

    @Test
    @DisplayName("printarBanco reduz despesas quando despesas > receitas")
    void testPrintarBancoDespesasMaiorQueReceitasReduzDespesas() {
        Banco.getInstancia().setDespesas(1000.0); // despesas (1000) > receitas (0)

        Banco.getInstancia().printarBanco();

        double despesasApos = Banco.getInstancia().getDespesas();
        assertTrue(despesasApos < 1000.0,
                "quando despesas > receitas, printarBanco divide as despesas por um numero grande (deve diminuir)");
    }

    @Test
    @DisplayName("printarBanco nao altera despesas quando receitas >= despesas")
    void testPrintarBancoReceitasMaiorOuIgualNaoAltera() {
        Banco.getInstancia().setDespesas(300.0);
        Banco.getInstancia().setReceitas(300.0); // receitas == despesas -> ramo falso

        Banco.getInstancia().printarBanco();

        assertEquals(300.0, Banco.getInstancia().getDespesas(), DELTA);
        assertTrue(saida().contains("Despesas do banco"), "deve imprimir as despesas");
        assertTrue(saida().contains("Receitas do banco"), "deve imprimir as receitas");
    }
}
