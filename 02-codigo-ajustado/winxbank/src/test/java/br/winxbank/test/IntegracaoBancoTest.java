package br.winxbank.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.winxbank.sistemabancario.Banco;
import br.winxbank.sistemabancario.Cartao;
import br.winxbank.sistemabancario.CartaoCredito;
import br.winxbank.sistemabancario.ContaCorrente;
import br.winxbank.sistemabancario.ContaPoupanca;
import br.winxbank.sistemaclientes.Cliente;
import br.winxbank.sistemaclientes.RegistroDeClientes;
import br.winxbank.tempo.Ano;

/**
 * Testes de INTEGRACAO de Banco (Entrega 2 - tarefa 2.2).
 *
 * Diferente do BancoTest (unitario, com mockStatic), aqui usamos os componentes
 * REAIS interagindo: Banco + RegistroDeClientes + Cliente + ContaCorrente +
 * ContaPoupanca + CartaoCredito + Ano. Nada e mockado.
 *
 * Verifica que movimentarEntreBancoConta:
 *  - desconta a taxa de manutencao da conta corrente e gera receita no banco;
 *  - acrescenta rendimento na poupanca e gera despesa no banco;
 *  - cobra juros do cartao quando ha fatura em aberto e o mes avancou (integra com Ano).
 */
class IntegracaoBancoTest {

    private static final int NUM_CC = 9001;
    private static final int NUM_CP = 9002;
    private static final int NUM_CARTAO = 8001;
    private static final int CSV = 456;
    private static final double SALDO_INICIAL = 1000.0;
    private static final double TAXA_MANUTENCAO = 13.0;
    private static final double RENDIMENTO_MENSAL_POUPANCA = 0.8;
    private static final double TAXA_JURUS = 12.75;
    private static final double DELTA = 0.001;

    @BeforeEach
    void setUp() {
        Banco.getInstancia().receitas = 0.0;
        Banco.getInstancia().despesas = 0.0;
        RegistroDeClientes.getInstancia().limparListaDeClientes();
        Ano.getInstancia().setMesAtual("Janeiro");
    }

    private Cliente clienteComContaCorrente(double saldo, CartaoCredito cartaoCredito) {
        Cliente cliente = new Cliente("Integra", "222.222.222-22");
        cliente.setContas(new ContaCorrente(NUM_CC, saldo, new Cartao(NUM_CARTAO, CSV), 0, cartaoCredito));
        return cliente;
    }

    private Cliente clienteComContaPoupanca(double saldo) {
        Cliente cliente = new Cliente("IntegraP", "333.333.333-33");
        cliente.setContas(new ContaPoupanca(NUM_CP, saldo, new Cartao(NUM_CARTAO, CSV), 0));
        return cliente;
    }

    @Test
    @DisplayName("Integracao: taxa da conta corrente vira receita do banco")
    void integracaoContaCorrenteGeraReceita() {
        Cliente cliente = clienteComContaCorrente(SALDO_INICIAL, new CartaoCredito(NUM_CARTAO, CSV));
        RegistroDeClientes.getInstancia().getClientes().add(cliente);

        Banco.getInstancia().movimentarEntreBancoConta();

        ContaCorrente conta = (ContaCorrente) cliente.getContas().get(0);
        assertEquals(SALDO_INICIAL - TAXA_MANUTENCAO, conta.getSaldo(), DELTA);
        assertEquals(TAXA_MANUTENCAO, Banco.getInstancia().getReceitas(), DELTA);
        assertEquals(1, conta.getExtrato().size(), "deve registrar uma movimentacao de SAIDA no extrato");
    }

    @Test
    @DisplayName("Integracao: rendimento da poupanca vira despesa do banco")
    void integracaoContaPoupancaGeraDespesa() {
        Cliente cliente = clienteComContaPoupanca(SALDO_INICIAL);
        RegistroDeClientes.getInstancia().getClientes().add(cliente);

        Banco.getInstancia().movimentarEntreBancoConta();

        ContaPoupanca conta = (ContaPoupanca) cliente.getContas().get(0);
        double rendimento = SALDO_INICIAL / RENDIMENTO_MENSAL_POUPANCA;
        assertEquals(SALDO_INICIAL + rendimento, conta.getSaldo(), DELTA);
        assertEquals(rendimento, Banco.getInstancia().getDespesas(), DELTA);
    }

    @Test
    @DisplayName("Integracao: carteira com corrente e poupanca atualiza receita e despesa")
    void integracaoCarteiraMista() {
        RegistroDeClientes.getInstancia().getClientes().add(
                clienteComContaCorrente(SALDO_INICIAL, new CartaoCredito(NUM_CARTAO, CSV)));
        RegistroDeClientes.getInstancia().getClientes().add(
                clienteComContaPoupanca(SALDO_INICIAL));

        Banco.getInstancia().movimentarEntreBancoConta();

        assertEquals(TAXA_MANUTENCAO, Banco.getInstancia().getReceitas(), DELTA);
        assertEquals(SALDO_INICIAL / RENDIMENTO_MENSAL_POUPANCA, Banco.getInstancia().getDespesas(), DELTA);
    }

    @Test
    @DisplayName("Integracao com Ano: fatura em aberto + mes avancado faz o cartao cobrar juros")
    void integracaoCobrancaDeJurosComAnoAvancado() {
        CartaoCredito cartaoCredito = new CartaoCredito(NUM_CARTAO, CSV);
        cartaoCredito.setFatura(200.0); // fatura em aberto (mes da fatura = Janeiro/0)
        Cliente cliente = clienteComContaCorrente(SALDO_INICIAL, cartaoCredito);
        RegistroDeClientes.getInstancia().getClientes().add(cliente);

        Ano.getInstancia().setMesAtual("Marco"); // mes atual (2) > mes da fatura (0)

        Banco.getInstancia().movimentarEntreBancoConta();

        double faturaEsperada = 200.0 * TAXA_JURUS;
        assertEquals(faturaEsperada, cartaoCredito.getFatura(), DELTA);

        // receita = taxa de manutencao + juros cobrados (fatura nova - fatura antiga)
        double receitaEsperada = TAXA_MANUTENCAO + (faturaEsperada - 200.0);
        assertEquals(receitaEsperada, Banco.getInstancia().getReceitas(), DELTA);
        assertTrue(Banco.getInstancia().getReceitas() > TAXA_MANUTENCAO,
                "com juros a receita deve superar a taxa de manutencao");
    }
}
