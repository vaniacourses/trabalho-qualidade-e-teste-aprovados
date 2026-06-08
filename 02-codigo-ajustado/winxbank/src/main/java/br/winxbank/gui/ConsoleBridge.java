package br.winxbank.gui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormatSymbols;

/**
 * Ponte entre a GUI e a lógica de domínio que lê do {@code System.in} e escreve
 * no {@code System.out}.
 *
 * <p>Vários métodos do WinxBank (ex.: {@code Banco.abrirNovaConta()},
 * {@code Conta.comprar()}, {@code RegistroDeClientes.cadastrarCliente()},
 * {@code Banco.fecharConta()} e {@code CartaoCredito.ajustarLimite()}) leem
 * respostas do teclado via {@link java.util.Scanner} e imprimem no console.
 * Para reaproveitar exatamente esse mesmo código <b>sem alterá-lo</b>, a GUI
 * injeta as respostas coletadas em formulários no {@code System.in} e captura
 * tudo o que for impresso, devolvendo o texto para ser exibido no painel de
 * saída.</p>
 *
 * <p>O redirecionamento é global ao processo, portanto as ações aqui executadas
 * são síncronas e curtas (rodam na própria thread de eventos do Swing).</p>
 */
public final class ConsoleBridge {

    private ConsoleBridge() {
    }

    /** Ação que pode lançar exceção verificada, executada com o console redirecionado. */
    @FunctionalInterface
    public interface BridgedAction {
        void run() throws Exception;
    }

    /**
     * Executa {@code action} alimentando o {@code System.in} com {@code input} e
     * capturando tudo que for escrito em {@code System.out} / {@code System.err}.
     *
     * @param input texto entregue ao Scanner (cada resposta separada por "\n")
     * @param action trecho que chama o método de domínio desejado
     * @return o que foi impresso durante a execução
     */
    public static String run(String input, BridgedAction action) {
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;

        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        PrintStream sink = new PrintStream(captured, true, StandardCharsets.UTF_8);

        try {
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            System.setOut(sink);
            System.setErr(sink);
            action.run();
        } catch (Exception e) {
            sink.println("[excecao] " + e.getMessage());
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
            System.setErr(originalErr);
            sink.flush();
        }
        return captured.toString(StandardCharsets.UTF_8);
    }

    /**
     * Formata um valor {@code double} usando o separador decimal do locale
     * padrão da JVM, para que {@code Scanner.nextDouble()} consiga interpretá-lo
     * exatamente como faria com a digitação manual no terminal.
     *
     * @param valor valor numérico
     * @return representação textual pronta para alimentar o Scanner
     */
    public static String localizedNumber(double valor) {
        char separador = new DecimalFormatSymbols().getDecimalSeparator();
        return String.valueOf(valor).replace('.', separador);
    }
}
