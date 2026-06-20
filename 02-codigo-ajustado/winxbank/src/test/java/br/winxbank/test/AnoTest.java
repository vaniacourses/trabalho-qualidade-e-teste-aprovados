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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.lang.reflect.Field;

public class AnoTest {

	@BeforeEach
	void setUp() throws Exception {
	    Ano.getInstancia().setMesAtual("Janeiro");
	    RegistroDeClientes.getInstancia().limparListaDeClientes();
	    Banco.getInstancia().setReceitas(0);
	    Banco.getInstancia().setDespesas(0);

	    Field field = Ano.class.getDeclaredField("count");
	    field.setAccessible(true);
	    field.setLong(Ano.getInstancia(), 0L);
	}

    @Test
    void testGetInstanciaRetornaMesmaInstancia() {
        Ano instancia1 = Ano.getInstancia();
        Ano instancia2 = Ano.getInstancia();
        assertSame(instancia1, instancia2, "Deve retornar a mesma instância (Singleton)");
    }

    @Test
    void testSetMesAtualValido() {
        Ano.getInstancia().setMesAtual("Marco");
        assertEquals("Marco", Ano.getInstancia().getMesAtual());
        assertEquals(2, Ano.getInstancia().getIndexMesAtual());
    }

    @Test
    void testFazerMesPassarCicloDeCinco() {        
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
        double saldoEsperado = saldoInicial - 13.0;
        assertEquals(saldoEsperado, conta.getSaldo(), 0.001, "A taxa de manutenção deve ser descontada na mudança de mês");
        assertTrue(Banco.getInstancia().getReceitas() >= 13.0, "A receita do banco deve aumentar com a taxa");
    }

    @Test
    void testIntegracaoComArquivoDeMesAtual() {
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
    
    @Test
    void testSetMesAtualInvalido() {
        Ano ano = Ano.getInstancia();

        ano.setMesAtual("Janeiro");
        int indexAnterior = ano.getIndexMesAtual();

        ano.setMesAtual("Inválido");

        assertEquals("Inválido", ano.getMesAtual());
        assertEquals(indexAnterior, ano.getIndexMesAtual());
    }
    
    @ParameterizedTest
    @CsvSource({
            "Janeiro,0",
            "Fevereiro,1",
            "Marco,2",
            "Abril,3",
            "Maio,4",
            "Junho,5",
            "Julho,6",
            "Agosto,7",
            "Setembro,8",
            "Outubro,9",
            "Novembro,10",
            "Dezembro,11"
    })
    void testTodosOsMeses(String mes, int indiceEsperado) {

        Ano.getInstancia().setMesAtual(mes);

        assertEquals(mes, Ano.getInstancia().getMesAtual());
        assertEquals(indiceEsperado,
                Ano.getInstancia().getIndexMesAtual());
    }
    
    @Test
    void testRetornoFazerMesPassar() {

        Ano.getInstancia().setMesAtual("Janeiro");

        int retorno = Ano.getInstancia().fazerMesPassar();

        assertEquals(
                Ano.getInstancia().getIndexMesAtual(),
                retorno
        );
    }
    
    @Test
    void testOverflowDoContador() throws Exception {

        Field field = Ano.class.getDeclaredField("count");
        field.setAccessible(true);

        field.setLong(Ano.getInstancia(), Long.MAX_VALUE);

        Ano.getInstancia().fazerMesPassar();

        long valorAtual = field.getLong(Ano.getInstancia());

        assertEquals(0L, valorAtual);
    }
    
    @Test
    void testNaoDeveMudarMesQuandoCountNaoForMultiploDeCinco()
            throws Exception {

        Field field = Ano.class.getDeclaredField("count");
        field.setAccessible(true);

        field.setLong(Ano.getInstancia(), 1);

        Ano.getInstancia().setMesAtual("Janeiro");

        int indexAnterior =
                Ano.getInstancia().getIndexMesAtual();

        Ano.getInstancia().fazerMesPassar();

        assertEquals(
                indexAnterior,
                Ano.getInstancia().getIndexMesAtual()
        );
    }
    
    @Test
    void testMesAtualDeveSerAtualizadoAoPassarMes() {

        Ano.getInstancia().setMesAtual("Janeiro");

        int indiceInicial =
                Ano.getInstancia().getIndexMesAtual();

        while (Ano.getInstancia().getIndexMesAtual()
                == indiceInicial) {

            Ano.getInstancia().fazerMesPassar();
        }

        assertEquals(
                "Fevereiro",
                Ano.getInstancia().getMesAtual()
        );
    }
}
