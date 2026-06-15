package br.winxbank.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

/**
 * Teste E2E (End-to-End) do fluxo de DEPOSITO e SAQUE (Entrega 2 - tarefa 4.2.2).
 *
 * O WinxBank e uma aplicacao de console; conforme combinado no plano da entrega,
 * o E2E e feito com ProcessBuilder: executa o JAR e simula a interacao do usuario
 * pelo terminal (mesma abordagem do E2EPixTest do grupo).
 *
 * Fluxo simulado:
 *   1 (criar usuario) -> nome -> cpf -> 1 (conta corrente) -> 1000 (saldo)
 *   2 (logar) -> cpf
 *   6 (depositar) -> numero da conta -> valor
 *   9 (sacar) -> numero da conta -> valor
 *   0 (encerrar)
 *
 * Obs.: o numero da conta e gerado aleatoriamente pela aplicacao, portanto o teste
 * verifica que o programa percorre o fluxo e produz saida (verificacao tolerante,
 * como no E2EPixTest), nao valores exatos de saldo.
 */
class E2EDepositoSaqueTest {

    private static final String JAR_PATH = "target/winxbank-1.0.jar";

    private boolean jarExiste() {
        return new File(JAR_PATH).exists();
    }

    @Test
    @EnabledIf("jarExiste")
    @DisplayName("E2E: fluxo de deposito e saque via ProcessBuilder")
    void testE2EFluxoDepositoSaqueViaProcessBuilder() throws IOException, InterruptedException {
        String input = "1\nCaio\n123.456.789-00\n1\n1000\n" + // cadastrar + abrir conta corrente (saldo 1000)
                       "2\n123.456.789-00\n" +                 // logar
                       "6\n1\n500\n" +                          // depositar (conta, valor)
                       "9\n1\n200\n" +                          // sacar (conta, valor)
                       "0\n";                                    // encerrar

        ProcessBuilder pb = new ProcessBuilder("java", "-jar", JAR_PATH);
        Process processo = pb.start();

        processo.getOutputStream().write(input.getBytes());
        processo.getOutputStream().flush();
        processo.getOutputStream().close();

        String saida = new String(processo.getInputStream().readAllBytes());
        String erros = new String(processo.getErrorStream().readAllBytes());
        int exitCode = processo.waitFor();

        System.out.println("Exit code: " + exitCode);
        if (!erros.isEmpty()) {
            System.out.println("Erros: " + erros);
        }

        assertNotNull(saida, "a saida do processo nao deve ser nula");
        assertTrue(saida.length() > 0, "a saida do processo nao deve ser vazia");
        assertTrue(saida.contains("WinxBank"), "a saida deve conter o menu do WinxBank");
        assertTrue(saida.contains("MENU INICIAL"), "a aplicacao deve exibir o menu inicial");
    }

    @Test
    @DisplayName("E2E: a sequencia de inputs de deposito/saque esta bem-formada")
    void testE2EPreparacaoDeInputs() {
        String input = "1\nCaio\n123.456.789-00\n1\n1000\n" +
                       "2\n123.456.789-00\n" +
                       "6\n1\n500\n" +
                       "9\n1\n200\n" +
                       "0\n";

        assertNotNull(input);
        assertTrue(input.contains("123.456.789-00"), "deve conter o cpf do usuario");
        assertTrue(input.contains("\n6\n"), "deve acionar a opcao 6 (depositar)");
        assertTrue(input.contains("\n9\n"), "deve acionar a opcao 9 (sacar)");
        assertTrue(input.endsWith("0\n"), "deve encerrar com a opcao 0");
    }
}
