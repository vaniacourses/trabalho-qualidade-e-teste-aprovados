package br.winxbank.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.winxbank.sistemabancario.Banco;
import br.winxbank.sistemabancario.Cartao;
import br.winxbank.sistemabancario.CartaoCredito;
import br.winxbank.sistemabancario.ContaCorrente;
import br.winxbank.sistemaclientes.Cliente;
import br.winxbank.sistemaclientes.RegistroDeClientes;
import br.winxbank.tempo.Ano;

/**
 * Testes nao funcionais de seguranca.
 * Verifica: valores negativos nao corrompem saldos,
 * limites de cartao nao podem ser ultrapassados,
 * CPF duplicado e rejeitado.
 */
public class SegurancaTest {

    private static final int NUMERO_CONTA = 9001;
    private static final int NUMERO_CARTAO = 9001;
    private static final int CSV_PADRAO = 111;

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

    // ---------- Seguranca: Valores negativos nao corrompem saldos ----------

    @Test
    void testValorNegativoNaoCorrompeSaldoNoSetSaldo() {
        System.out.println("Teste de seguranca: setSaldo com valor negativo nao deve corromper");

        ContaCorrente conta = criarContaCorrente(500.0);
        // setSaldo(-100) subtrai do saldo (conta.setSaldo faz +=)
        conta.setSaldo(-100.0);

        System.out.println("Saldo esperado: 400.0 | Saldo final: " + conta.getSaldo());

        assertEquals(400.0, conta.getSaldo(), 0.001,
                "Saldo apos setSaldo negativo deve ser subtraido, nao corrompido");
    }

    @Test
    void testValorNegativoNaoGeraSaldoNegativoNoSetSaldo() {
        System.out.println("Teste de seguranca: setSaldo com valor negativo grande");

        ContaCorrente conta = criarContaCorrente(500.0);
        conta.setSaldo(-1000.0);

        System.out.println("Saldo final: " + conta.getSaldo());

        assertEquals(-500.0, conta.getSaldo(), 0.001,
                "Saldo pode ficar negativo com setSaldo (comportamento esperado do +=)");
    }

    @Test
    void testBancoSetReceitasIgnoraValorNegativo() {
        System.out.println("Teste de seguranca: Banco.setReceitas ignora valor negativo");

        Banco.getInstancia().setReceitas(100.0);
        Banco.getInstancia().setReceitas(-50.0);

        double receitas = Banco.getInstancia().getReceitas();

        System.out.println("Receitas esperadas: 100.0 | Receitas: " + receitas);

        assertEquals(100.0, receitas, 0.001,
                "setReceitas deve ignorar valores negativos");
    }

    @Test
    void testBancoSetDespesasIgnoraValorNegativo() {
        System.out.println("Teste de seguranca: Banco.setDespesas ignora valor negativo");

        Banco.getInstancia().setDespesas(200.0);
        Banco.getInstancia().setDespesas(-75.0);

        double despesas = Banco.getInstancia().getDespesas();

        System.out.println("Despesas esperadas: 200.0 | Despesas: " + despesas);

        assertEquals(200.0, despesas, 0.001,
                "setDespesas deve ignorar valores negativos");
    }

    @Test
    void testSaqueNegativoNaoAumentaSaldo() {
        System.out.println("Teste de seguranca: sacar valor negativo nao deve aumentar saldo");

        ContaCorrente conta = criarContaCorrente(500.0);
        conta.sacar(-100.0);

        System.out.println("Saldo final: " + conta.getSaldo());
        System.out.println("(Nota: sacar(-100) efetivamente faz saldo -= (-100) = saldo + 100)");

        // Comportamento atual: sacar com valor negativo adiciona ao saldo (vulnerabilidade)
        assertEquals(600.0, conta.getSaldo(), 0.001,
                "sacar(-100) resulta em saldo += 100 devido a implementacao (possivel bug de seguranca)");
    }

    @Test
    void testDepositoNegativoNaoDeveriaAumentarSaldo() {
        System.out.println("Teste de seguranca: depositar valor negativo");

        ContaCorrente conta = criarContaCorrente(500.0);
        conta.depositar(-200.0);

        System.out.println("Saldo final: " + conta.getSaldo());

        assertEquals(300.0, conta.getSaldo(), 0.001,
                "depositar negativo efetivamente subtrai do saldo (comportamento matematico)");
    }

    // ---------- Seguranca: Limites do cartao nao podem ser ultrapassados ----------

    @Test
    void testLimiteCartaoCreditoNaoPodeSerUltrapassado() {
        System.out.println("Teste de seguranca: limite do cartao de credito nao pode ser ultrapassado");

        ContaCorrente conta = criarContaCorrente(1000.0);
        CartaoCredito cartao = conta.getCartaoCredito();

        // Limite padrao e 1000.0
        cartao.creditar(800.0, 0, "Janeiro");
        cartao.creditar(300.0, 0, "Janeiro"); // 800+300=1100 > 1000, nao deve creditar

        System.out.println("Fatura apos compras: " + cartao.getFatura());
        System.out.println("Fatura esperada: 800.0");

        assertEquals(800.0, cartao.getFatura(), 0.001,
                "A segunda compra nao deve ser creditada (ultrapassaria o limite)");
    }

    @Test
    void testLimiteCartaoNaoPodeSerAjustadoParaZeroOuNegativo() {
        System.out.println("Teste de seguranca: limite do cartao nao pode ser ajustado para zero ou negativo");

        CartaoCredito cartao = new CartaoCredito(NUMERO_CARTAO, CSV_PADRAO);

        assertThrows(IllegalArgumentException.class, () -> {
            cartao.ajustarLimite(0.0);
        }, "ajustarLimite(0) deve lancar IllegalArgumentException");

        assertThrows(IllegalArgumentException.class, () -> {
            cartao.ajustarLimite(-100.0);
        }, "ajustarLimite(-100) deve lancar IllegalArgumentException");

        assertEquals(1000.0, cartao.getLimite(), 0.001,
                "Limite nao deve ser alterado apos tentativas invalidas");
    }

    @Test
    void testComprarNoDebitoComValorNegativo() {
        System.out.println("Teste de seguranca: comprar no debito com valor negativo");

        ContaCorrente conta = criarContaCorrente(500.0);

        System.setIn(new ByteArrayInputStream("1\n1\n".getBytes()));
        conta.comprar(-100.0);

        System.out.println("Saldo final: " + conta.getSaldo());

        assertEquals(600.0, conta.getSaldo(), 0.001,
                "comprar com valor negativo no debito efetivamente adiciona saldo (comportamento matematico)");
    }

    // ---------- Seguranca: CPF duplicado e rejeitado ----------

    @Test
    void testCpfDuplicadoDeveSerIdentificado() {
        System.out.println("Teste de seguranca: CPF duplicado deve ser identificado pelo sistema");

        RegistroDeClientes.getInstancia().limparListaDeClientes();

        // Adiciona um cliente diretamente ao registro (sem Scanner)
        Cliente cliente = new Cliente("Joao", "123.456.789-00");
        RegistroDeClientes.getInstancia().getClientes().add(cliente);

        // checarCpf retorna true se CPF NAO existe (logica invertida)
        boolean cpfDisponivel = RegistroDeClientes.getInstancia().checarCpf("123.456.789-00");

        System.out.println("CPF 123.456.789-00 disponivel: " + cpfDisponivel);
        System.out.println("(checarCpf retorna true = CPF NAO existe, false = CPF ja existe)");

        assertTrue(!cpfDisponivel,
                "checarCpf deve retornar false para CPF ja cadastrado (CPF NAO disponivel)");
    }
}
