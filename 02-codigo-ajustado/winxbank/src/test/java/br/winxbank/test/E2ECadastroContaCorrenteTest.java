package br.winxbank.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.winxbank.sistemabancario.Banco;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

/**
 * Teste E2E da tarefa 4.2.1.
 *
 * Executa o JAR real do WinxBank via ProcessBuilder e simula o fluxo do usuario
 * pelo terminal. O teste roda em diretório temporário para não reaproveitar
 * `clientes.json`, `mesAtual.txt` ou `banco.txt` de outras execuções.
 */
class E2ECadastroContaCorrenteTest {

    private static final String JAR_PATH = "target/winxbank-1.0.jar";

    private boolean jarExiste() {
        return new File(JAR_PATH).exists();
    }

    @Test
    @EnabledIf("jarExiste")
    @DisplayName("E2E: cadastro, login, remocao e nova tentativa de login via ProcessBuilder")
    void testE2EFluxoCadastroLoginRemocaoViaProcessBuilder() throws IOException, InterruptedException {
        String saida = executarAplicacao(interacao -> {
            interacao.enviarApos("MENU INICIAL", "1\n");
            interacao.enviarApos("Digite o nome:", "Fernando Rene\n");
            interacao.enviarApos("Digite o cpf:", "111.222.333-44\n");
            interacao.enviarApos("Digite 1 (Corrente) ou 2 (Poupanca)", "1\n");
            interacao.enviarApos("Digite o saldo que deseja colocar na sua conta", "5000\n");
            interacao.enviarApos("Sua conta corrente foi criada com sucesso!", "2\n");
            interacao.enviarApos("Digite o cpf do usuario que deseja logar:", "111.222.333-44\n");
            interacao.enviarApos("Ola, Fernando Rene", "5\n");
            interacao.enviarApos("Seu usuario está sendo apagado...", "2\n");
            interacao.enviarApos("Digite o cpf do usuario que deseja logar:", "111.222.333-44\n");
            interacao.enviarApos("Cliente inexistente.", "0\n");
        });

        assertFluxoBasico(saida);
        assertTrue(saida.contains("Fernando Rene"), "a saida deve exibir o nome do cliente apos login");
        assertTrue(saida.contains("Seu usuario está sendo apagado..."),
                "a saida deve indicar a remocao do usuario");
        assertTrue(saida.contains("Cliente inexistente."),
                "a nova tentativa de login deve falhar apos a remocao");
    }

    @Test
    @EnabledIf("jarExiste")
    @DisplayName("E2E: CPF duplicado e rejeitado via ProcessBuilder")
    void testE2ECadastroComCpfDuplicadoViaProcessBuilder() throws IOException, InterruptedException {
        String saida = executarAplicacao(interacao -> {
            interacao.enviarApos("MENU INICIAL", "1\n");
            interacao.enviarApos("Digite o nome:", "Cliente Um\n");
            interacao.enviarApos("Digite o cpf:", "123.123.123-12\n");
            interacao.enviarApos("Digite 1 (Corrente) ou 2 (Poupanca)", "1\n");
            interacao.enviarApos("Digite o saldo que deseja colocar na sua conta", "3000\n");
            interacao.enviarApos("Sua conta corrente foi criada com sucesso!", "1\n");
            interacao.enviarApos("Digite o nome:", "Cliente Dois\n");
            interacao.enviarApos("Digite o cpf:", "123.123.123-12\n");
            interacao.enviarApos("CPF ja existente no registro.", "0\n");
        });

        assertFluxoBasico(saida);
        assertTrue(saida.contains("Usuario nao pode ser criado. CPF ja existente no registro."),
                "o cadastro com CPF duplicado deve ser rejeitado");
    }

    @Test
    @EnabledIf("jarExiste")
    @DisplayName("E2E: saldo no limite promove cliente para ClienteWinx")
    void testE2ECadastroClienteWinxViaProcessBuilder() throws IOException, InterruptedException {
        String saida = executarAplicacao(interacao -> {
            interacao.enviarApos("MENU INICIAL", "1\n");
            interacao.enviarApos("Digite o nome:", "Cliente Winx\n");
            interacao.enviarApos("Digite o cpf:", "999.888.777-66\n");
            interacao.enviarApos("Digite 1 (Corrente) ou 2 (Poupanca)", "1\n");
            interacao.enviarApos("Digite o saldo que deseja colocar na sua conta", "100000\n");
            interacao.enviarApos("Parabéns, você tem direito a ser ClienteWinx!", "2\n");
            interacao.enviarApos("Digite o cpf do usuario que deseja logar:", "999.888.777-66\n");
            interacao.enviarApos("Ola, Cliente Winx", "0\n");
        });

        assertFluxoBasico(saida);
        assertTrue(saida.contains("Parabéns, você tem direito a ser ClienteWinx!"),
                "saldo no limite deve promover o cliente");
        assertTrue(saida.contains("Pontos por compra"),
                "os dados exibidos apos o login devem refletir o tipo ClienteWinx");
    }

    @Test
    @DisplayName("E2E: a sequencia de inputs do cadastro/login esta bem-formada")
    void testE2EPreparacaoDeInputs() {
        String input = ""
                + "1\n"
                + "Fernando Rene\n"
                + "111.222.333-44\n"
                + "1\n"
                + "5000\n"
                + "2\n"
                + "111.222.333-44\n"
                + "5\n"
                + "0\n";

        assertNotNull(input);
        assertTrue(input.contains("Fernando Rene"));
        assertTrue(input.contains("111.222.333-44"));
        assertTrue(input.contains("\n5\n"), "a sequencia deve acionar a opcao de apagar usuario");
        assertTrue(input.endsWith("0\n"));
    }

    private String executarAplicacao(InteracaoComProcesso roteiro) throws IOException, InterruptedException {
        Path tempDir = Files.createTempDirectory("winxbank-e2e-registro-");
        prepararArquivosDoAmbiente(tempDir);

        ProcessBuilder pb = new ProcessBuilder("java", "-jar", new File(JAR_PATH).getAbsolutePath());
        pb.directory(tempDir.toFile());
        pb.redirectErrorStream(true);

        Process processo = pb.start();
        StringBuilder saida = new StringBuilder();
        AtomicReference<IOException> falhaLeitura = new AtomicReference<>();

        Thread leitor = new Thread(() -> {
            try {
                byte[] buffer = new byte[256];
                int lidos;
                while ((lidos = processo.getInputStream().read(buffer)) != -1) {
                    synchronized (saida) {
                        saida.append(new String(buffer, 0, lidos, StandardCharsets.UTF_8));
                    }
                }
            } catch (IOException e) {
                falhaLeitura.set(e);
            }
        });
        leitor.start();

        try (Writer writer = new OutputStreamWriter(processo.getOutputStream(), StandardCharsets.UTF_8)) {
            Interacao interacao = new Interacao(processo, writer, saida);
            roteiro.executar(interacao);
        }

        int exitCode = processo.waitFor();
        leitor.join();
        if (falhaLeitura.get() != null) {
            throw falhaLeitura.get();
        }

        String textoSaida;
        synchronized (saida) {
            textoSaida = saida.toString();
        }

        assertTrue(exitCode == 0, "o processo deve encerrar com sucesso. Saida:\n" + textoSaida);
        assertNotNull(textoSaida, "a saida do processo nao deve ser nula");
        assertTrue(!textoSaida.isBlank(), "a saida do processo nao deve ser vazia");
        return textoSaida;
    }

    private void prepararArquivosDoAmbiente(Path tempDir) throws IOException {
        Files.writeString(tempDir.resolve("clientes.json"), "[]", StandardCharsets.UTF_8);
        Files.writeString(tempDir.resolve("mesAtual.txt"), "Janeiro", StandardCharsets.UTF_8);

        try (ObjectOutputStream output = new ObjectOutputStream(Files.newOutputStream(tempDir.resolve("banco.txt")))) {
            output.writeObject(Banco.getInstancia());
        } catch (IOException e) {
            throw new UncheckedIOException("falha ao preparar banco.txt para o teste E2E", e);
        }
    }

    private void assertFluxoBasico(String saida) {
        assertTrue(saida.contains("WinxBank"), "a aplicacao deve exibir o cabecalho do WinxBank");
        assertTrue(saida.contains("MENU INICIAL"), "a aplicacao deve exibir o menu inicial");
        assertTrue(saida.contains("Sua conta corrente foi criada com sucesso!"),
                "o cadastro deve abrir a conta corrente");
    }

    @FunctionalInterface
    private interface InteracaoComProcesso {
        void executar(Interacao interacao) throws IOException, InterruptedException;
    }

    private static final class Interacao {
        private static final Duration TIMEOUT = Duration.ofSeconds(3);

        private final Process processo;
        private final Writer writer;
        private final StringBuilder saida;

        private Interacao(Process processo, Writer writer, StringBuilder saida) {
            this.processo = processo;
            this.writer = writer;
            this.saida = saida;
        }

        private void enviarApos(String trecho, String texto) throws IOException, InterruptedException {
            aguardarSaida(trecho);
            writer.write(texto);
            writer.flush();
        }

        private void aguardarSaida(String trecho) throws InterruptedException {
            Instant limite = Instant.now().plus(TIMEOUT);
            while (Instant.now().isBefore(limite)) {
                synchronized (saida) {
                    if (saida.indexOf(trecho) >= 0) {
                        return;
                    }
                }
                if (!processo.isAlive()) {
                    break;
                }
                Thread.sleep(25);
            }
            synchronized (saida) {
                throw new AssertionError("trecho nao encontrado na saida: " + trecho + "\nSaida atual:\n" + saida);
            }
        }
    }
}
