package br.winxbank.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.*;
import java.util.regex.*;

import org.junit.jupiter.api.Test;

public class SistemaCartaoCreditoPBTest {

    @Test
    void cenario1_deveAceitarCompraDentroDoLimite() throws Exception {
        String resultado = executarFluxoCompra("99988877761", "500");

        assertTrue(resultado.contains("Sua conta corrente foi criada com sucesso"));
        assertTrue(resultado.contains("Valor creditado"));
        assertFatura(resultado, "500");
    }

    @Test
    void cenario2_deveAceitarCompraExatamenteNoLimite() throws Exception {
        String resultado = executarFluxoCompra("99988877762", "1000");

        assertTrue(resultado.contains("Sua conta corrente foi criada com sucesso"));
        assertTrue(resultado.contains("Valor creditado"));
        assertFatura(resultado, "1000");
    }

    @Test
    void cenario3_naoDeveUltrapassarLimiteComCompraAcimaDoLimite() throws Exception {
        String resultado = executarFluxoCompra("99988877763", "1000.01");

        assertTrue(resultado.contains("Sua conta corrente foi criada com sucesso"));
        assertFatura(resultado, "0");
    }

    @Test
    void cenario4_deveAceitarComprasAcumuladasAteOLimite() throws Exception {
        String resultado = executarFluxoCompra("99988877764", "800", "200");

        assertTrue(resultado.contains("Sua conta corrente foi criada com sucesso"));
        assertTrue(resultado.contains("Valor creditado"));
        assertFatura(resultado, "1000");
    }

    @Test
    void cenario5_naoDeveUltrapassarLimiteComComprasAcumuladas() throws Exception {
        String resultado = executarFluxoCompra("99988877765", "800", "200.01");

        assertTrue(resultado.contains("Sua conta corrente foi criada com sucesso"));
        assertFatura(resultado, "800");
    }

    private String executarFluxoCompra(String cpf, String... valoresCompra) throws Exception {

        ProcessBuilder pb = new ProcessBuilder(
                "java",
                "-cp",
                "target/classes;target/dependency/*",
                "br.winxbank.Main"
        );

        pb.redirectErrorStream(true);
        Process processo = pb.start();

        BufferedWriter entrada = new BufferedWriter(new OutputStreamWriter(processo.getOutputStream()));
        BufferedReader saida = new BufferedReader(new InputStreamReader(processo.getInputStream()));

        enviarBloco(entrada,
                "18",
                "1",
                "Joao PB",
                cpf,
                "1",
                "5000",
                "2",
                cpf
        );

        Thread.sleep(3000);

        String saidaInicial = lerDisponivel(saida);
        int numeroConta = extrairNumeroConta(saidaInicial);

        for (String valor : valoresCompra) {
            enviarBloco(entrada,
                    "7",
                    String.valueOf(numeroConta),
                    valor,
                    "2",
                    "1"
            );

            Thread.sleep(1500);
        }

        enviarBloco(entrada,
                "17",
                "0"
        );

        Thread.sleep(3000);

        String saidaFinal = lerDisponivel(saida);

        processo.destroy();

        String resultado = saidaInicial + saidaFinal;

        System.out.println("SAÍDA DO PROCESSO:");
        System.out.println(resultado);

        return resultado;
    }

    private void enviarBloco(BufferedWriter entrada, String... valores) throws IOException {
        for (String valor : valores) {
            entrada.write(valor);
            entrada.newLine();
        }
        entrada.flush();
    }

    private String lerDisponivel(BufferedReader saida) throws IOException {
        StringBuilder resultado = new StringBuilder();

        while (saida.ready()) {
            resultado.append(saida.readLine()).append("\n");
        }

        return resultado.toString();
    }

    private int extrairNumeroConta(String texto) {
        Pattern pattern = Pattern.compile("ContaCorrenteno:\\s*(\\d+)");
        Matcher matcher = pattern.matcher(texto);

        int numeroConta = -1;

        while (matcher.find()) {
            numeroConta = Integer.parseInt(matcher.group(1));
        }

        if (numeroConta != -1) {
            return numeroConta;
        }

        throw new IllegalStateException("Número da conta não encontrado na saída:\n" + texto);
    }

    private void assertFatura(String resultado, String valorEsperado) {
        String valorComVirgula = valorEsperado.replace(".", ",");

        if (!valorComVirgula.contains(",")) {
            valorComVirgula = valorComVirgula + ",00";
        }

        String valorComPonto = valorEsperado;

        if (!valorComPonto.contains(".")) {
            valorComPonto = valorComPonto + ".00";
        }

        assertTrue(
                resultado.contains("fatura: " + valorComVirgula)
                        || resultado.contains("fatura: " + valorComPonto),
                "Fatura esperada não encontrada. Esperado: "
                        + valorComVirgula
                        + " ou "
                        + valorComPonto
                        + "\nSaída:\n"
                        + resultado
        );
    }
}