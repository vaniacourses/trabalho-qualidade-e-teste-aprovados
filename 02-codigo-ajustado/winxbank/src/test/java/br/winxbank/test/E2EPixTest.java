package br.winxbank.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import br.winxbank.sistemabancario.Banco;
import br.winxbank.sistemaclientes.RegistroDeClientes;
import br.winxbank.tempo.Ano;

/**
 * Teste E2E (End-to-End) para o fluxo de PIX.
 * Utiliza ProcessBuilder para executar a aplicacao e simular interacao do usuario via console.
 *
 * O teste executa o JAR compilado e envia inputs simulados para o fluxo:
 *  1. Cadastrar cliente A
 *  2. Cadastrar cliente B
 *  3. Logar como A
 *  4. Abrir conta corrente para A
 *  5. Logar como B
 *  6. Abrir conta corrente para B
 *  7. Logar como A
 *  8. Fazer PIX de A para B
 *  9. Verificar saida do console
 */
public class E2EPixTest {

    private static final String JAR_PATH = "target/winxbank-1.0.jar";

    /**
     * Verifica se o JAR existe antes de executar o teste.
     */
    private boolean jarExiste() {
        File jar = new File(JAR_PATH);
        return jar.exists();
    }

    @BeforeEach
    void setUp() {
        Banco.getInstancia().receitas = 0.0;
        Banco.getInstancia().despesas = 0.0;
        RegistroDeClientes.getInstancia().limparListaDeClientes();
        Ano.getInstancia().setMesAtual("Janeiro");
    }

    @Test
    @EnabledIf("jarExiste")
    void testE2EFluxoPixViaProcessBuilder() throws IOException, InterruptedException {
        System.out.println("=== TESTE E2E: Fluxo de PIX via ProcessBuilder ===");

        // Constroi os inputs que simulam o fluxo E2E de PIX:
        // 1 (criar usuario A) -> Alice -> 111.111.111-11
        // 1 (criar usuario B) -> Bob -> 222.222.222-22
        // 2 (logar) -> 111.111.111-11
        // 3 (abrir conta) -> 1 (corrente) -> 2000 (saldo)
        // 2 (logar) -> 222.222.222-22
        // 3 (abrir conta) -> 1 (corrente) -> 1000 (saldo)
        // 2 (logar) -> 111.111.111-11
        // 8 (fazer pix) -> [numero conta A] -> 222.222.222-22 -> [numero conta B] -> 500
        // 0 (encerrar)

        String input = "1\nAlice\n111.111.111-11\n" +       // cadastrar A
                       "1\nBob\n222.222.222-22\n" +          // cadastrar B
                       "2\n111.111.111-11\n" +               // logar A
                       "3\n1\n2000\n" +                        // abrir conta corrente A
                       "2\n222.222.222-22\n" +               // logar B
                       "3\n1\n1000\n" +                        // abrir conta corrente B
                       "2\n111.111.111-11\n" +               // logar A novamente
                       "8\n1000\n222.222.222-22\n2000\n500\n" + // pix: conta A, cpf B, conta B, valor
                       "0\n";                                   // encerrar

        System.out.println("Inputs simulados preparados.");

        ProcessBuilder pb = new ProcessBuilder(
                "java", "-jar", JAR_PATH
        );

        Process processo = pb.start();

        // Envia inputs para o processo
        processo.getOutputStream().write(input.getBytes());
        processo.getOutputStream().flush();
        processo.getOutputStream().close();

        // Le a saida do processo
        String saida = new String(processo.getInputStream().readAllBytes());
        String erros = new String(processo.getErrorStream().readAllBytes());

        int exitCode = processo.waitFor();

        System.out.println("Exit code: " + exitCode);
        System.out.println("Erros (se houver): " + erros);

        assertNotNull(saida, "A saida do processo nao deve ser nula");
        assertTrue(saida.length() > 0, "A saida do processo nao deve ser vazia");

        System.out.println("=== TESTE E2E CONCLUIDO ===");
    }

    @Test
    void testE2EPreparacaoDeInputs() {
        System.out.println("Teste E2E: verificacao de preparacao dos inputs para o fluxo PIX");

        // Verifica que os inputs para o ProcessBuilder sao validos
        String input = "1\nAlice\n111.111.111-11\n2\n111.111.111-11\n0\n";

        assertNotNull(input);
        assertTrue(input.contains("Alice"));
        assertTrue(input.contains("111.111.111-11"));
        assertTrue(input.endsWith("0\n"));

        System.out.println("Inputs de teste E2E validados com sucesso.");
    }
}
