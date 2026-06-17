package br.winxbank.test;

import org.junit.jupiter.api.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import br.winxbank.sistemaclientes.RegistroDeClientes;
import br.winxbank.tempo.Ano;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class E2EClientWinxTest {

    private static final String JAVA_CMD  = "java";
    private static final String JAR       = "target/winxbank-1.0.jar";
    private static final int TIMEOUT_S = 15;

    private static final File DIR_PROJETO = new File(System.getProperty("user.dir"));

    private static final String CPF_WINX      = "111.111.111-11";
    private static final String CPF_COMUM     = "222.222.222-22";
    private static final String CPF_SEM_PONTOS = "333.333.333-33";


    private String rodarPrograma(List<String> inputs) throws IOException, InterruptedException {

        ProcessBuilder pb = new ProcessBuilder(JAVA_CMD, "-jar", JAR);
        pb.redirectErrorStream(true);
        pb.directory(DIR_PROJETO);

        Process processo = pb.start();

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(processo.getOutputStream()))) {

            for (String linha : inputs) {
                writer.write(linha);
                writer.newLine();
                writer.flush();
            }
        }

        boolean terminou = processo.waitFor(TIMEOUT_S, TimeUnit.SECONDS);

        String saida = new String(processo.getInputStream().readAllBytes());

        if (!terminou) {
            processo.destroyForcibly();
            fail("Processo travou.\nSaída:\n" + saida);
        }

        return saida;
    }


    @BeforeEach
    void limparArquivosDeEstado() {
        RegistroDeClientes.getInstancia().limparListaDeClientes();
        Ano.getInstancia().setMesAtual("Janeiro");
    }


    @Test
    @Order(1)
    void cadastroComSaldoAltoDeveVirarClienteWinx() throws Exception {

        String saida = rodarPrograma(List.of(
                "1", "Bloom Winx", CPF_WINX, "1", "100000", "0"
        ));

        assertAll(
            () -> assertTrue(
                saida.toLowerCase().contains("parabéns") || saida.toLowerCase().contains("parabens"),
                "Deveria exibir mensagem de parabéns.\n" + saida
            ),
            () -> assertTrue(
                saida.toLowerCase().contains("winx"),
                "Deveria mencionar ClienteWinx.\n" + saida
            )
        );
    }


    @Test
    @Order(2)
    void compraPorClienteWinxAcumulaPontos() throws Exception {

        String cadastro = rodarPrograma(List.of(
                "1", "Stella Winx", CPF_WINX, "1", "200000", "17", "0"
        ));

        String conta = extrairNumeroConta(cadastro);
        assertNotNull(conta, "Conta não encontrada.\n" + cadastro);

        String saida = rodarPrograma(List.of(
                "2", CPF_WINX, "7", conta, "50", "17", "0"
        ));

        assertTrue(
            saida.toLowerCase().contains("ponto"),
            "Deveria acumular pontos.\n" + saida
        );
    }


    @Test
    @Order(3)
    void conversaoDePontosEmSaldoDeveDepositarNaConta() throws Exception {

        String cadastro = rodarPrograma(List.of(
                "1", "Flora Winx", CPF_WINX, "1", "150000", "17", "0"
        ));

        String conta = extrairNumeroConta(cadastro);
        assertNotNull(conta, "Conta não encontrada.\n" + cadastro);

        rodarPrograma(List.of("2", CPF_WINX, "7", conta, "100", "0"));

        String saida = rodarPrograma(List.of(
                "2", CPF_WINX, "14", conta, "17", "0"
        ));

        assertTrue(
            saida.toLowerCase().contains("ponto"),
            "Deveria processar conversão de pontos.\n" + saida
        );
    }


    @Test
    @Order(4)
    void converterSemPontosDeveExibirErro() throws Exception {

        String cadastro = rodarPrograma(List.of(
                "1", "Tecna Winx", CPF_SEM_PONTOS, "1", "120000", "17", "0"
        ));

        String conta = extrairNumeroConta(cadastro);
        assertNotNull(conta, "Conta não encontrada.\n" + cadastro);

        String saida = rodarPrograma(List.of(
                "2", CPF_SEM_PONTOS, "14", conta, "0"
        ));

        assertTrue(
            saida.toLowerCase().contains("ponto")
            || saida.toLowerCase().contains("insuf")
            || saida.toLowerCase().contains("erro"),
            "Deveria indicar erro de pontos.\n" + saida
        );
    }


    @Test
    @Order(5)
    void clienteComumNaoPodeConverterPontos() throws Exception {

        String cadastro = rodarPrograma(List.of(
                "1", "Musa Comum", CPF_COMUM, "1", "500", "17", "0"
        ));

        String conta = extrairNumeroConta(cadastro);
        assertNotNull(conta, "Conta não encontrada.\n" + cadastro);

        String saida = rodarPrograma(List.of(
                "2", CPF_COMUM, "14", conta, "0"
        ));

        assertTrue(
            saida.toLowerCase().contains("winx")
            || saida.toLowerCase().contains("não é cliente")
            || saida.toLowerCase().contains("erro"),
            "Cliente comum deveria receber erro.\n" + saida
        );
    }


    @Test
    @Order(6)
    void cadastroComSaldoBaixoNaoViraClienteWinx() throws Exception {

        String saida = rodarPrograma(List.of(
                "1", "Aisha Comum", CPF_COMUM, "2", "500", "17", "0"
        ));

        assertFalse(
            saida.toLowerCase().contains("parabéns") || saida.toLowerCase().contains("parabens"),
            "Não deveria promover ClienteWinx.\n" + saida
        );
    }


    private String extrairNumeroConta(String saida) {
        var pattern = java.util.regex.Pattern.compile("(?i)no[:\\s]*(\\d+)");
        var matcher = pattern.matcher(saida);

        return matcher.find() ? matcher.group(1) : null;
    }
}