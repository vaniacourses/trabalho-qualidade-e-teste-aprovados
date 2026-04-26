package br.winxbank.test;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import br.winxbank.tempo.Ano;
import br.winxbank.sistemabancario.Banco;
import br.winxbank.sistemabancario.ContaCorrente;
import br.winxbank.sistemabancario.Cartao;
import br.winxbank.sistemabancario.CartaoCredito;
import br.winxbank.sistemaclientes.Cliente;
import br.winxbank.sistemaclientes.RegistroDeClientes;
import br.winxbank.repository.ArquivoDeMesAtual;
import java.io.File;

public class AnoTest {

    @BeforeEach
    void setUp() {
        Ano.getInstancia().setMesAtual("Janeiro");
        RegistroDeClientes.getInstancia().limparListaDeClientes();
        Banco.getInstancia().setReceitas(0);
        Banco.getInstancia().setDespesas(0);
    }

    @Test
    void testGetInstanciaRetornaMesmaInstancia() {
        System.out.println("Teste get instancia retorna mesma instancia");
        Ano instancia1 = Ano.getInstancia();
        Ano instancia2 = Ano.getInstancia();
        assertSame(instancia1, instancia2, "Deve retornar a mesma instância (Singleton)");
    }

    @Test
    void testSetMesAtualValido() {
        System.out.println("Teste set mes atual valido");
        Ano.getInstancia().setMesAtual("Marco");
        assertEquals("Marco", Ano.getInstancia().getMesAtual());
        assertEquals(2, Ano.getInstancia().getIndexMesAtual());
    }

    @Test
    void testFazerMesPassarCicloDeCinco() {
        System.out.println("Teste fazer mes passar ciclo de cinco");
        
        int initialIndex = Ano.getInstancia().getIndexMesAtual();
        int seguranca = 0;
        while(Ano.getInstancia().getIndexMesAtual() == initialIndex && seguranca < 10) {
            Ano.getInstancia().fazerMesPassar();
            seguranca++;
        }
        
        int afterChangeIndex = Ano.getInstancia().getIndexMesAtual();
        assertNotEquals(initialIndex, afterChangeIndex, "O mês deve mudar após algumas chamadas");
        for(int i = 0; i < 4; i++) {
            Ano.getInstancia().fazerMesPassar();
            assertEquals(afterChangeIndex, Ano.getInstancia().getIndexMesAtual(), "O mês não deve mudar antes de 5 chamadas. Falhou na iteração: " + i);
        }
        
        Ano.getInstancia().fazerMesPassar();
        assertEquals((afterChangeIndex + 1) % 12, Ano.getInstancia().getIndexMesAtual(), "O mês deve mudar na 5ª chamada");
    }

    @Test
    void testFazerMesPassarResetaParaJaneiroAposDezembro() {
        System.out.println("Teste fazer mes passar reseta para janeiro apos dezembro");
        Ano.getInstancia().setMesAtual("Dezembro");
        int seguranca = 0;
        while(Ano.getInstancia().getIndexMesAtual() == 11 && seguranca < 10) {
            Ano.getInstancia().fazerMesPassar();
            seguranca++;
        }
        
        assertEquals(0, Ano.getInstancia().getIndexMesAtual(), "Deve voltar para Janeiro (0) após Dezembro (11)");
        assertEquals("Janeiro", Ano.getInstancia().getMesAtual());
    }

    @Test
    void testFazerMesPassarGeraMovimentacaoNoBanco() {
        System.out.println("Teste fazer mes passar gera movimentacao no banco");
        double saldoInicial = 1000.0;
        Cliente cliente = new Cliente("Test", "123.456.789-00");
        Cartao cartao = new Cartao(1234, 999);
        CartaoCredito cc = new CartaoCredito(1234, 999);
        ContaCorrente conta = new ContaCorrente(55555, saldoInicial, cartao, 0, cc);
        cliente.setContas(conta);
        RegistroDeClientes.getInstancia().getClientes().add(cliente);
        int initialIndex = Ano.getInstancia().getIndexMesAtual();
        while(Ano.getInstancia().getIndexMesAtual() == initialIndex) {
            Ano.getInstancia().fazerMesPassar();
        }
        double saldoEsperado = saldoInicial - 13.0; // taxaManutencaoConta na interface OperacoesAutomaticas é 13.0
        assertEquals(saldoEsperado, conta.getSaldo(), 0.001, "A taxa de manutenção deve ser descontada na mudança de mês");
        assertTrue(Banco.getInstancia().getReceitas() >= 13.0, "A receita do banco deve aumentar com a taxa");
    }

    @Test
    void testIntegracaoComArquivoDeMesAtual() {
        System.out.println("Teste integracao com arquivo de mes atual");
        String mesTeste = "Julho";
        Ano.getInstancia().setMesAtual(mesTeste);
        ArquivoDeMesAtual.getInstancia().escreverMesAtual();
        Ano.getInstancia().setMesAtual("Janeiro");
        ArquivoDeMesAtual.getInstancia().lerMesAtual();
        assertEquals(mesTeste, Ano.getInstancia().getMesAtual(), "O mês deve ser recuperado corretamente do arquivo");
        File file = new File("mesAtual.txt");
        if(file.exists()) {
            file.delete();
        }
    }
}
