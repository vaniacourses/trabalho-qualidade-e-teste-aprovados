package br.winxbank.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import br.winxbank.sistemabancario.Banco;
import br.winxbank.sistemabancario.Cartao;
import br.winxbank.sistemabancario.CartaoCredito;
import br.winxbank.sistemabancario.Conta;
import br.winxbank.sistemabancario.ContaCorrente;
import br.winxbank.sistemabancario.ContaPoupanca;
import br.winxbank.sistemaclientes.Cliente;
import br.winxbank.sistemaclientes.RegistroDeClientes;

/**
 * Teste funcional (caixa-preta) de Banco.movimentarEntreBancoConta.
 *
 * Tecnica: Particao de Equivalencia + Analise do Valor Limite.
 *
 * O metodo nao recebe parametros diretos: sua "entrada" e a composicao da lista
 * de clientes/contas do registro (isolada via mockStatic) e os saldos/faturas.
 *
 * PARTICAO DE EQUIVALENCIA (composicao da carteira de contas):
 *   CE1 - lista vazia ............... nenhuma movimentacao
 *   CE2 - somente conta corrente .... desconta taxa (receita do banco)
 *   CE3 - somente conta poupanca .... acrescenta rendimento (despesa do banco)
 *   CE4 - carteira mista ............ receita (corrente) e despesa (poupanca)
 *
 * ANALISE DO VALOR LIMITE:
 *   VL1 - saldo = 0 (limite inferior do saldo)
 *   VL2 - fatura = 0 (limite que NAO cobra juros) x fatura > 0 (cobra)
 */
class BancoFuncionalTest {

    private static final int NUMERO_CONTA = 5000;
    private static final int NUMERO_CARTAO = 4321;
    private static final int CSV = 321;
    private static final double TAXA_MANUTENCAO = 13.0;
    private static final double RENDIMENTO_MENSAL_POUPANCA = 0.8;
    private static final double DELTA = 0.001;

    @BeforeEach
    void setUp() {
        Banco.getInstancia().receitas = 0.0;
        Banco.getInstancia().despesas = 0.0;
    }

    private ContaCorrente contaCorrente(double saldo) {
        return new ContaCorrente(NUMERO_CONTA, saldo, new Cartao(NUMERO_CARTAO, CSV), 0,
                new CartaoCredito(NUMERO_CARTAO, CSV));
    }

    private ContaPoupanca contaPoupanca(double saldo) {
        return new ContaPoupanca(NUMERO_CONTA, saldo, new Cartao(NUMERO_CARTAO, CSV), 0);
    }

    private ArrayList<Cliente> carteira(Conta... contas) {
        ArrayList<Cliente> clientes = new ArrayList<>();
        Cliente cliente = new Cliente("Func", "111.111.111-11");
        for (Conta c : contas) {
            cliente.setContas(c);
        }
        clientes.add(cliente);
        return clientes;
    }

    private void movimentarCom(ArrayList<Cliente> clientes) {
        RegistroDeClientes registroMock = mock(RegistroDeClientes.class);
        when(registroMock.getClientes()).thenReturn(clientes);
        try (MockedStatic<RegistroDeClientes> registro = mockStatic(RegistroDeClientes.class)) {
            registro.when(RegistroDeClientes::getInstancia).thenReturn(registroMock);
            Banco.getInstancia().movimentarEntreBancoConta();
        }
    }

    @Test
    @DisplayName("CE1 - lista vazia: nenhuma movimentacao")
    void ce1ListaVazia() {
        movimentarCom(new ArrayList<>());

        assertEquals(0.0, Banco.getInstancia().getReceitas(), DELTA);
        assertEquals(0.0, Banco.getInstancia().getDespesas(), DELTA);
    }

    @Test
    @DisplayName("CE2 - so conta corrente: gera receita = taxa de manutencao")
    void ce2SoContaCorrente() {
        movimentarCom(carteira(contaCorrente(500.0)));

        assertEquals(TAXA_MANUTENCAO, Banco.getInstancia().getReceitas(), DELTA);
        assertEquals(0.0, Banco.getInstancia().getDespesas(), DELTA);
    }

    @Test
    @DisplayName("CE3 - so conta poupanca: gera despesa = rendimento")
    void ce3SoContaPoupanca() {
        ContaPoupanca poupanca = contaPoupanca(500.0);
        movimentarCom(carteira(poupanca));

        assertEquals(0.0, Banco.getInstancia().getReceitas(), DELTA);
        assertEquals(500.0 / RENDIMENTO_MENSAL_POUPANCA, Banco.getInstancia().getDespesas(), DELTA);
    }

    @Test
    @DisplayName("CE4 - carteira mista: receita (corrente) e despesa (poupanca)")
    void ce4CarteiraMista() {
        movimentarCom(carteira(contaCorrente(500.0), contaPoupanca(500.0)));

        assertEquals(TAXA_MANUTENCAO, Banco.getInstancia().getReceitas(), DELTA);
        assertEquals(500.0 / RENDIMENTO_MENSAL_POUPANCA, Banco.getInstancia().getDespesas(), DELTA);
    }

    @Test
    @DisplayName("VL1 - poupanca com saldo = 0: rendimento e zero")
    void vl1PoupancaSaldoZero() {
        ContaPoupanca poupanca = contaPoupanca(0.0);
        movimentarCom(carteira(poupanca));

        assertEquals(0.0, poupanca.getSaldo(), DELTA);
        assertEquals(0.0, Banco.getInstancia().getDespesas(), DELTA);
    }

    @Test
    @DisplayName("VL1 - corrente com saldo = 0: fica negativa no valor da taxa")
    void vl1CorrenteSaldoZero() {
        ContaCorrente corrente = contaCorrente(0.0);
        movimentarCom(carteira(corrente));

        assertEquals(-TAXA_MANUTENCAO, corrente.getSaldo(), DELTA);
        assertEquals(TAXA_MANUTENCAO, Banco.getInstancia().getReceitas(), DELTA);
    }

    @Test
    @DisplayName("VL2 - fatura = 0: nao cobra juros (receita = apenas a taxa)")
    void vl2FaturaZero() {
        ContaCorrente corrente = contaCorrente(500.0); // fatura default = 0
        movimentarCom(carteira(corrente));

        assertEquals(TAXA_MANUTENCAO, Banco.getInstancia().getReceitas(), DELTA);
    }
}
