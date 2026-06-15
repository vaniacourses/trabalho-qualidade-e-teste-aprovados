package br.winxbank.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.winxbank.sistemabancario.Banco;
import br.winxbank.sistemabancario.Cartao;
import br.winxbank.sistemabancario.CartaoCredito;
import br.winxbank.sistemabancario.ContaCorrente;
import br.winxbank.sistemaclientes.Cliente;
import br.winxbank.sistemaclientes.RegistroDeClientes;
import br.winxbank.tempo.Ano;

/**
 * Teste de requisito NAO FUNCIONAL - Desempenho (Entrega 2 - tarefa 4.3.1).
 *
 * Mede o tempo de execucao de Banco.movimentarEntreBancoConta para 100, 1.000 e
 * 10.000 clientes usando System.nanoTime().
 *
 * BASELINE aceitavel definido: processar 10.000 clientes em ate 2.000 ms.
 * A operacao e O(n) sobre a carteira de clientes/contas, entao o tempo deve
 * crescer de forma aproximadamente linear.
 */
class DesempenhoBancoTest {

    private static final long BASELINE_10K_MS = 2000L;

    @BeforeEach
    void setUp() {
        Banco.getInstancia().receitas = 0.0;
        Banco.getInstancia().despesas = 0.0;
        RegistroDeClientes.getInstancia().limparListaDeClientes();
        Ano.getInstancia().setMesAtual("Janeiro");
    }

    private void popularClientes(int quantidade) {
        RegistroDeClientes.getInstancia().limparListaDeClientes();
        for (int i = 0; i < quantidade; i++) {
            Cliente cliente = new Cliente("Cliente" + i, "cpf" + i);
            cliente.setContas(new ContaCorrente(i, 1000.0,
                    new Cartao(i, 100), 0, new CartaoCredito(i, 100)));
            RegistroDeClientes.getInstancia().getClientes().add(cliente);
        }
    }

    private long medirMovimentacao(int quantidade) {
        popularClientes(quantidade);
        long inicio = System.nanoTime();
        Banco.getInstancia().movimentarEntreBancoConta();
        long fimNs = System.nanoTime();
        long ms = (fimNs - inicio) / 1_000_000L;
        System.out.println("movimentarEntreBancoConta com " + quantidade + " clientes: " + ms + " ms");
        return ms;
    }

    @Test
    @DisplayName("Desempenho: tempo cresce de forma aceitavel de 100 a 10.000 clientes")
    void testDesempenhoMovimentacao() {
        // Aquecimento da JVM para medicao mais estavel
        popularClientes(100);
        Banco.getInstancia().movimentarEntreBancoConta();

        long t100 = medirMovimentacao(100);
        long t1000 = medirMovimentacao(1000);
        long t10000 = medirMovimentacao(10000);

        assertTrue(t100 >= 0 && t1000 >= 0 && t10000 >= 0, "tempos medidos devem ser validos");
        assertTrue(t10000 <= BASELINE_10K_MS,
                "10.000 clientes devem ser processados em ate " + BASELINE_10K_MS + " ms (medido: " + t10000 + " ms)");
    }
}
