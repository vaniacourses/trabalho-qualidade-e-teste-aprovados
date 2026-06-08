package br.winxbank.gui;

import br.winxbank.exception.*;
import br.winxbank.repository.ArquivoBanco;
import br.winxbank.repository.ArquivoDeClientes;
import br.winxbank.repository.ArquivoDeMesAtual;
import br.winxbank.sistemabancario.*;
import br.winxbank.sistemaclientes.Cliente;
import br.winxbank.sistemaclientes.ClienteWinx;
import br.winxbank.sistemaclientes.RegistroDeClientes;
import br.winxbank.tempo.Ano;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Orquestra as operações do WinxBank para a interface gráfica, espelhando
 * fielmente o fluxo do {@code switch} do {@link br.winxbank.Main} de console.
 *
 * <p>Não altera nenhuma classe de domínio: recebe os valores já coletados nos
 * formulários da GUI e, para os métodos que leem do teclado, usa a
 * {@link ConsoleBridge}. Cada operação reproduz a mesma sequência do menu de
 * console: avança o tempo ({@link Ano#fazerMesPassar()}), executa a regra e, ao
 * final, persiste exatamente como o bloco {@code finally} do {@code Main}.</p>
 */
public class WinxBankController {

    private static final DecimalFormat MOEDA = new DecimalFormat("0.00");

    /** Carrega os dados persistidos, igual ao início do {@code Main}. */
    public void bootstrap() {
        ArquivoDeClientes.getInstancia().readjason();
        ArquivoDeMesAtual.getInstancia().lerMesAtual();
        try {
            ArquivoBanco.getInstancia().construirBanco();
        } catch (Exception e) {
            // Arquivo do banco ausente/ilegível: o banco inicia zerado, como no Main.
        }
    }

    // ----------------------------- MENU INICIAL -----------------------------

    /** Caso 1: cadastra um cliente (lê nome, cpf, tipo e saldo via ponte de console). */
    public ActionResult cadastrarCliente(String nome, String cpf, int tipoConta, double saldo) {
        tick();
        String entrada = nome + "\n" + cpf + "\n" + tipoConta + "\n"
                + ConsoleBridge.localizedNumber(saldo) + "\n";
        String saida = ConsoleBridge.run(entrada,
                () -> RegistroDeClientes.getInstancia().cadastrarCliente());
        persist();
        return ActionResult.ok(saida.isBlank() ? "Cliente cadastrado." : saida.trim());
    }

    /** Caso 2: efetua login por CPF, devolvendo o cliente da sessão. */
    public ActionResult login(String cpf) {
        tick();
        try {
            Cliente cliente = RegistroDeClientes.getInstancia().retornarCliente(cpf);
            if (cliente == null) {
                return ActionResult.error("Cliente inexistente.");
            }
            Cliente atual;
            if (cliente.getClass() == ClienteWinx.class) {
                atual = new ClienteWinx(cliente);
            } else {
                atual = new Cliente(cliente);
            }
            atual.setContas(cliente.getContas());
            return ActionResult.okWithSession("Login efetuado. Bem-vindo, " + atual.getNome() + "!", atual);
        } catch (NullPointerException e) {
            return ActionResult.error("Cliente inexistente.");
        } finally {
            persist();
        }
    }

    // -------------------------------- MENU ----------------------------------

    /** Caso 3: abre uma nova conta para o cliente logado (lê tipo e saldo via ponte). */
    public ActionResult abrirConta(Cliente atual, int tipoConta, double saldo) {
        tick();
        if (naoLogado(atual)) {
            return ActionResult.error(new YouAreNotLoggedInException().getMessage());
        }
        Cliente sessao = atual;
        StringBuilder msg = new StringBuilder();
        try {
            Conta[] holder = new Conta[1];
            String entrada = tipoConta + "\n" + ConsoleBridge.localizedNumber(saldo) + "\n";
            String saida = ConsoleBridge.run(entrada,
                    () -> holder[0] = Banco.getInstancia().abrirNovaConta());
            msg.append(saida.trim());

            Conta conta = holder[0];
            if (conta == null) {
                return ActionResult.error("Opcao invalida ao abrir conta.");
            }
            Movimentacao mov = new Movimentacao(conta.getSaldo(), Movimentacao.TipoDaMovimentacao.ENTRADA);
            conta.setExtrato(mov);
            if (conta.getClass() == ContaPoupanca.class) {
                ((ContaPoupanca) conta).setInformeRendimento(mov);
            }
            sessao.setContas(conta);
            if (conta.getSaldo() >= 100000 || sessao.acessarContas().getSaldo() >= 100000) {
                ClienteWinx winx = new ClienteWinx(sessao.getNome(), sessao.getCpf(), 0);
                winx.setContas(sessao.getContas());
                sessao = winx;
                msg.append("\nParabens, voce tem direito a ser ClienteWinx!");
            }
        } finally {
            atualizar(sessao);
            persist();
        }
        return ActionResult.okWithSession(msg.toString().trim(), sessao);
    }

    /** Caso 4: fecha uma conta do cliente logado (lê o número da conta via ponte). */
    public ActionResult fecharConta(Cliente atual, int numeroConta) {
        tick();
        if (naoLogado(atual)) {
            return ActionResult.error(new YouAreNotLoggedInException().getMessage());
        }
        try {
            String saida = ConsoleBridge.run(numeroConta + "\n",
                    () -> Banco.getInstancia().fecharConta(atual));
            return ActionResult.ok(saida.trim());
        } finally {
            atualizar(atual);
            persist();
        }
    }

    /** Caso 5: apaga o usuário logado e encerra a sessão (logout). */
    public ActionResult apagarUsuario(Cliente atual) {
        tick();
        if (naoLogado(atual)) {
            return ActionResult.error(new YouAreNotLoggedInException().getMessage());
        }
        try {
            RegistroDeClientes.getInstancia().removerCliente(atual);
            return ActionResult.okWithSession("Usuario apagado com sucesso.", new Cliente());
        } finally {
            persist();
        }
    }

    /** Caso 6: deposita um valor em uma conta. */
    public ActionResult depositar(Cliente atual, int numeroConta, double valor) {
        tick();
        if (naoLogado(atual)) {
            return ActionResult.error(new YouAreNotLoggedInException().getMessage());
        }
        try {
            Conta conta = atual.selecionarConta(numeroConta);
            if (conta == null) {
                return ActionResult.error(new BankAccountNotFoundException().getMessage());
            }
            conta.depositar(valor);
            conta.setExtrato(new Movimentacao(valor, Movimentacao.TipoDaMovimentacao.ENTRADA));
            return ActionResult.ok("Deposito de R$ " + MOEDA.format(valor) + " realizado. Saldo atual: R$ "
                    + MOEDA.format(conta.getSaldo()));
        } finally {
            atualizar(atual);
            persist();
        }
    }

    /** Caso 7: realiza uma compra (lê forma de pagamento/confirmação via ponte). */
    public ActionResult comprar(Cliente atual, int numeroConta, double valor, int formaPagamento) {
        tick();
        if (naoLogado(atual)) {
            return ActionResult.error(new YouAreNotLoggedInException().getMessage());
        }
        try {
            Conta conta = atual.selecionarConta(numeroConta);
            if (conta == null) {
                return ActionResult.error(new BankAccountNotFoundException().getMessage());
            }
            if (valor > conta.getSaldo()) {
                return ActionResult.error(new ValueIsHigherThanBalanceException().getMessage());
            }
            if (atual.getClass() == ClienteWinx.class) {
                ((ClienteWinx) atual).obterPontosDeCompra();
            }
            // ContaCorrente pergunta debito/credito e depois confirmacao; ContaPoupanca so confirma.
            String entrada = (conta.getClass() == ContaCorrente.class)
                    ? formaPagamento + "\n1\n"
                    : "1\n";
            String saida = ConsoleBridge.run(entrada, () -> conta.comprar(valor));
            conta.setExtrato(new Movimentacao(valor, Movimentacao.TipoDaMovimentacao.SAIDA));
            return ActionResult.ok(saida.trim());
        } finally {
            atualizar(atual);
            persist();
        }
    }

    /** Caso 8: realiza um pix para a conta de outro cliente. */
    public ActionResult pix(Cliente atual, int numeroContaOrigem, String cpfDestino,
                            int numeroContaDestino, double valor) {
        tick();
        if (naoLogado(atual)) {
            return ActionResult.error(new YouAreNotLoggedInException().getMessage());
        }
        Cliente destino = null;
        try {
            Conta conta = atual.selecionarConta(numeroContaOrigem);
            if (conta == null) {
                return ActionResult.error(new BankAccountNotFoundException().getMessage());
            }
            destino = RegistroDeClientes.getInstancia().retornarCliente(cpfDestino);
            if (destino == null) {
                return ActionResult.error(new ClientNotFoundException().getMessage());
            }
            Conta conta2 = destino.selecionarConta(numeroContaDestino);
            if (conta2 == null) {
                return ActionResult.error(new BankAccountNotFoundException().getMessage());
            }
            if (valor > conta2.getSaldo()) {
                return ActionResult.error(new ValueIsHigherThanBalanceException().getMessage());
            }
            conta.fazerPix(conta2, valor);
            conta.setExtrato(new Movimentacao(valor, Movimentacao.TipoDaMovimentacao.SAIDA));
            conta2.setExtrato(new Movimentacao(valor, Movimentacao.TipoDaMovimentacao.ENTRADA));
            return ActionResult.ok("Pix de R$ " + MOEDA.format(valor) + " enviado para o CPF " + cpfDestino + ".");
        } finally {
            atualizar(atual);
            if (destino != null) {
                atualizar(destino);
            }
            persist();
        }
    }

    /** Caso 9: saca um valor de uma conta. */
    public ActionResult sacar(Cliente atual, int numeroConta, double valor) {
        tick();
        if (naoLogado(atual)) {
            return ActionResult.error(new YouAreNotLoggedInException().getMessage());
        }
        try {
            Conta conta = atual.selecionarConta(numeroConta);
            if (conta == null) {
                return ActionResult.error(new BankAccountNotFoundException().getMessage());
            }
            if (valor > conta.getSaldo()) {
                return ActionResult.error(new ValueIsHigherThanBalanceException().getMessage());
            }
            conta.sacar(valor);
            conta.setExtrato(new Movimentacao(valor, Movimentacao.TipoDaMovimentacao.SAIDA));
            return ActionResult.ok("Saque de R$ " + MOEDA.format(valor) + " realizado. Saldo atual: R$ "
                    + MOEDA.format(conta.getSaldo()));
        } finally {
            atualizar(atual);
            persist();
        }
    }

    /** Caso 10: paga a fatura do cartão de crédito de uma conta corrente. */
    public ActionResult pagarFatura(Cliente atual, int numeroConta, double valor) {
        tick();
        if (naoLogado(atual)) {
            return ActionResult.error(new YouAreNotLoggedInException().getMessage());
        }
        try {
            Conta conta = atual.selecionarConta(numeroConta);
            if (conta == null) {
                return ActionResult.error(new BankAccountNotFoundException().getMessage());
            }
            if (conta.getClass() != ContaCorrente.class) {
                return ActionResult.error(new BankAccountIsNotCurrentAccountException().getMessage());
            }
            ContaCorrente corrente = (ContaCorrente) conta;
            corrente.pagarFatura(valor);
            corrente.setExtrato(new Movimentacao(valor, Movimentacao.TipoDaMovimentacao.SAIDA));
            return ActionResult.ok("Fatura paga em R$ " + MOEDA.format(valor) + ". Fatura atual: R$ "
                    + MOEDA.format(corrente.getCartaoCredito().getFatura()));
        } finally {
            atualizar(atual);
            persist();
        }
    }

    /** Caso 11: ajusta o limite do cartão de crédito (lê o novo limite via ponte). */
    public ActionResult ajustarLimite(Cliente atual, int numeroConta, double novoLimite) {
        tick();
        if (naoLogado(atual)) {
            return ActionResult.error(new YouAreNotLoggedInException().getMessage());
        }
        try {
            Conta conta = atual.selecionarConta(numeroConta);
            if (conta == null) {
                return ActionResult.error(new BankAccountNotFoundException().getMessage());
            }
            if (conta.getClass() != ContaCorrente.class) {
                return ActionResult.error(new BankAccountIsNotCurrentAccountException().getMessage());
            }
            String saida = ConsoleBridge.run(ConsoleBridge.localizedNumber(novoLimite) + "\n",
                    () -> ((ContaCorrente) conta).getCartaoCredito().ajustarLimite());
            return ActionResult.ok(saida.trim().isEmpty()
                    ? "Limite ajustado para R$ " + MOEDA.format(novoLimite) + "."
                    : saida.trim());
        } finally {
            persist();
        }
    }

    /** Caso 12: paga uma parcela de empréstimo. */
    public ActionResult pagarParcelaEmprestimo(Cliente atual, int numeroConta, double valor) {
        tick();
        if (naoLogado(atual)) {
            return ActionResult.error(new YouAreNotLoggedInException().getMessage());
        }
        try {
            Conta conta = atual.selecionarConta(numeroConta);
            if (conta == null) {
                return ActionResult.error(new BankAccountNotFoundException().getMessage());
            }
            conta.pagarParcelaDeEmprestimo(valor);
            conta.setExtrato(new Movimentacao(valor, Movimentacao.TipoDaMovimentacao.SAIDA));
            return ActionResult.ok("Parcela de R$ " + MOEDA.format(valor) + " paga. Divida atual: R$ "
                    + MOEDA.format(conta.getDividaDeEmprestimo()));
        } finally {
            atualizar(atual);
            persist();
        }
    }

    /** Caso 13: requisita um empréstimo. */
    public ActionResult requisitarEmprestimo(Cliente atual, int numeroConta, double valor) {
        tick();
        if (naoLogado(atual)) {
            return ActionResult.error(new YouAreNotLoggedInException().getMessage());
        }
        try {
            Conta conta = atual.selecionarConta(numeroConta);
            if (conta == null) {
                return ActionResult.error(new BankAccountNotFoundException().getMessage());
            }
            conta.requisitarEmprestimo(valor);
            return ActionResult.ok("Emprestimo de R$ " + MOEDA.format(valor) + " requisitado. Divida atual: R$ "
                    + MOEDA.format(conta.getDividaDeEmprestimo()));
        } finally {
            atualizar(atual);
            persist();
        }
    }

    /** Caso 14: converte pontos de compra (ClienteWinx) em saldo. */
    public ActionResult converterPontos(Cliente atual, int numeroConta) {
        tick();
        if (naoLogado(atual)) {
            return ActionResult.error(new YouAreNotLoggedInException().getMessage());
        }
        if (atual.getClass() != ClienteWinx.class) {
            return ActionResult.error(new ClientFoundIsNotClientWinxException().getMessage());
        }
        if (((ClienteWinx) atual).getPontosDeCompra() == 0) {
            return ActionResult.error(new NotEnaughPurchasePoints().getMessage());
        }
        try {
            Conta conta = atual.selecionarConta(numeroConta);
            if (conta == null) {
                return ActionResult.error(new BankAccountNotFoundException().getMessage());
            }
            ((ClienteWinx) atual).converterPontosEmSaldo(conta);
            return ActionResult.ok("Pontos convertidos em saldo. Saldo atual: R$ " + MOEDA.format(conta.getSaldo()));
        } finally {
            atualizar(atual);
            persist();
        }
    }

    /** Caso 15: gera o arquivo de extrato de uma conta. */
    public ActionResult gerarExtrato(Cliente atual, int numeroConta) {
        tick();
        if (naoLogado(atual)) {
            return ActionResult.error(new YouAreNotLoggedInException().getMessage());
        }
        try {
            Conta conta = atual.selecionarConta(numeroConta);
            if (conta == null) {
                return ActionResult.error(new BankAccountNotFoundException().getMessage());
            }
            conta.gerarExtrato();
            return ActionResult.ok("Extrato gerado no arquivo: " + numeroConta + "extrato.txt");
        } catch (Exception e) {
            return ActionResult.error("Falha ao gerar extrato: " + e.getMessage());
        } finally {
            persist();
        }
    }

    /** Caso 16: gera o informe de rendimento de uma conta poupança. */
    public ActionResult gerarInformeRendimento(Cliente atual, int numeroConta) {
        tick();
        if (naoLogado(atual)) {
            return ActionResult.error(new YouAreNotLoggedInException().getMessage());
        }
        try {
            Conta conta = atual.selecionarConta(numeroConta);
            if (conta == null) {
                return ActionResult.error(new BankAccountNotFoundException().getMessage());
            }
            if (conta.getClass() != ContaPoupanca.class) {
                return ActionResult.error(new BankAccountIsNotSavingsAccountException().getMessage());
            }
            ((ContaPoupanca) conta).gerarInformeRendimento();
            return ActionResult.ok("Informe de rendimento gerado no arquivo: " + numeroConta + "informe.txt");
        } catch (Exception e) {
            return ActionResult.error("Falha ao gerar informe: " + e.getMessage());
        } finally {
            persist();
        }
    }

    /** Caso 18: limpa o registro de clientes. */
    public ActionResult limparClientes() {
        tick();
        try {
            RegistroDeClientes.getInstancia().limparListaDeClientes();
            return ActionResult.ok("Lista de clientes limpa.");
        } finally {
            persist();
        }
    }

    // ----------------------------- CONSULTAS --------------------------------

    public ArrayList<Cliente> getClientes() {
        return RegistroDeClientes.getInstancia().getClientes();
    }

    public String getMesAtual() {
        return Ano.getInstancia().getMesAtual();
    }

    public double getReceitas() {
        return Banco.getInstancia().getReceitas();
    }

    public double getDespesas() {
        return Banco.getInstancia().getDespesas();
    }

    // ------------------------------- INTERNOS -------------------------------

    /** Avança o tempo a cada operação, exatamente como o {@code Main} faz a cada item do menu. */
    private void tick() {
        Ano.getInstancia().fazerMesPassar();
    }

    private boolean naoLogado(Cliente cliente) {
        return cliente == null || cliente.getCpf() == null;
    }

    private void atualizar(Cliente cliente) {
        try {
            RegistroDeClientes.getInstancia().atualizarCliente(cliente);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** Replica o bloco {@code finally} do {@code Main}: grava clientes, mês e banco. */
    private void persist() {
        try {
            ArquivoDeClientes.getInstancia().escreverJson(RegistroDeClientes.getInstancia().getClientes());
            ArquivoDeMesAtual.getInstancia().escreverMesAtual();
            ArquivoBanco.getInstancia().atualizarArquivo(Banco.getInstancia());
        } catch (Exception e) {
            // Mantém o comportamento tolerante do Main: falha de I/O não derruba a aplicação.
        }
    }
}
