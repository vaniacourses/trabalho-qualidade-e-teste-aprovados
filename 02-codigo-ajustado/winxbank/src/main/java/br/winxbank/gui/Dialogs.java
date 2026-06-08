package br.winxbank.gui;

import br.winxbank.sistemabancario.Conta;
import br.winxbank.sistemabancario.ContaCorrente;
import br.winxbank.sistemaclientes.Cliente;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Component;
import java.awt.Dimension;
import java.text.DecimalFormat;
import java.util.List;

/**
 * Diálogos de entrada da GUI. Cada método monta um formulário, valida os dados
 * e devolve um registro tipado (ou {@code null} se o usuário cancelar).
 */
public final class Dialogs {

    private static final DecimalFormat MOEDA = new DecimalFormat("0.00");

    private Dialogs() {
    }

    // ------------------------------- Registros ------------------------------

    public record NovoUsuario(String nome, String cpf, int tipoConta, double saldo) {
    }

    public record ContaValor(int numeroConta, double valor) {
    }

    public record CompraDados(int numeroConta, double valor, int formaPagamento) {
    }

    public record PixDados(int contaOrigem, String cpfDestino, int contaDestino, double valor) {
    }

    // ------------------------------- Diálogos -------------------------------

    /** Formulário de criação de usuário: nome, cpf, tipo de conta e saldo inicial. */
    public static NovoUsuario novoUsuario(Component parent) {
        JTextField nome = campo();
        JTextField cpf = campo();
        JComboBox<String> tipo = new JComboBox<>(new String[]{"Corrente", "Poupanca"});
        tipo.setFont(WinxTheme.FONTE_NORMAL);
        JTextField saldo = campo();

        JPanel form = form(
                "Nome", nome,
                "CPF", cpf,
                "Tipo de conta", tipo,
                "Saldo inicial", saldo);

        if (!confirmar(parent, form, "Criar usuario")) {
            return null;
        }
        if (nome.getText().isBlank() || cpf.getText().isBlank()) {
            erro(parent, "Nome e CPF sao obrigatorios.");
            return null;
        }
        Double valor = parseValor(parent, saldo.getText());
        if (valor == null) {
            return null;
        }
        int tipoConta = tipo.getSelectedIndex() == 0 ? 1 : 2;
        return new NovoUsuario(nome.getText().trim(), cpf.getText().trim(), tipoConta, valor);
    }

    /** Formulário para abrir uma nova conta (tipo + saldo). */
    public static NovoUsuario abrirConta(Component parent) {
        JComboBox<String> tipo = new JComboBox<>(new String[]{"Corrente", "Poupanca"});
        tipo.setFont(WinxTheme.FONTE_NORMAL);
        JTextField saldo = campo();
        JPanel form = form("Tipo de conta", tipo, "Saldo inicial", saldo);
        if (!confirmar(parent, form, "Abrir conta")) {
            return null;
        }
        Double valor = parseValor(parent, saldo.getText());
        if (valor == null) {
            return null;
        }
        int tipoConta = tipo.getSelectedIndex() == 0 ? 1 : 2;
        return new NovoUsuario("", "", tipoConta, valor);
    }

    /** Seleciona uma conta do cliente e pede um valor (depósito, saque, etc.). */
    public static ContaValor contaEValor(Component parent, Cliente cliente, String titulo, String rotuloValor) {
        JComboBox<ContaItem> contas = comboContas(cliente);
        if (contas == null) {
            erro(parent, "Este cliente nao possui contas.");
            return null;
        }
        JTextField valor = campo();
        JPanel form = form("Conta", contas, rotuloValor, valor);
        if (!confirmar(parent, form, titulo)) {
            return null;
        }
        Double v = parseValor(parent, valor.getText());
        if (v == null) {
            return null;
        }
        return new ContaValor(((ContaItem) contas.getSelectedItem()).numero, v);
    }

    /** Seleciona apenas uma conta do cliente (extrato, informe, converter pontos, fechar). */
    public static Integer selecionarConta(Component parent, Cliente cliente, String titulo) {
        JComboBox<ContaItem> contas = comboContas(cliente);
        if (contas == null) {
            erro(parent, "Este cliente nao possui contas.");
            return null;
        }
        JPanel form = form("Conta", contas);
        if (!confirmar(parent, form, titulo)) {
            return null;
        }
        return ((ContaItem) contas.getSelectedItem()).numero;
    }

    /** Formulário de compra: conta, valor e (se conta corrente) forma de pagamento. */
    public static CompraDados compra(Component parent, Cliente cliente) {
        JComboBox<ContaItem> contas = comboContas(cliente);
        if (contas == null) {
            erro(parent, "Este cliente nao possui contas.");
            return null;
        }
        JTextField valor = campo();
        JComboBox<String> forma = new JComboBox<>(new String[]{"Debito", "Credito"});
        forma.setFont(WinxTheme.FONTE_NORMAL);
        JPanel form = form(
                "Conta", contas,
                "Valor da compra", valor,
                "Forma (so conta corrente)", forma);
        if (!confirmar(parent, form, "Comprar")) {
            return null;
        }
        Double v = parseValor(parent, valor.getText());
        if (v == null) {
            return null;
        }
        int formaPagamento = forma.getSelectedIndex() == 0 ? 1 : 2;
        return new CompraDados(((ContaItem) contas.getSelectedItem()).numero, v, formaPagamento);
    }

    /** Formulário de pix: conta de origem, CPF e conta de destino e valor. */
    public static PixDados pix(Component parent, Cliente cliente) {
        JComboBox<ContaItem> contas = comboContas(cliente);
        if (contas == null) {
            erro(parent, "Este cliente nao possui contas.");
            return null;
        }
        JTextField cpfDestino = campo();
        JTextField contaDestino = campo();
        JTextField valor = campo();
        JPanel form = form(
                "Conta de origem", contas,
                "CPF de destino", cpfDestino,
                "Numero da conta de destino", contaDestino,
                "Valor", valor);
        if (!confirmar(parent, form, "Fazer Pix")) {
            return null;
        }
        if (cpfDestino.getText().isBlank()) {
            erro(parent, "Informe o CPF de destino.");
            return null;
        }
        Integer numDestino = parseInteiro(parent, contaDestino.getText(), "numero da conta de destino");
        if (numDestino == null) {
            return null;
        }
        Double v = parseValor(parent, valor.getText());
        if (v == null) {
            return null;
        }
        return new PixDados(((ContaItem) contas.getSelectedItem()).numero,
                cpfDestino.getText().trim(), numDestino, v);
    }

    /** Pede um CPF (login). */
    public static String pedirCpf(Component parent, String titulo) {
        JTextField cpf = campo();
        JPanel form = form("CPF", cpf);
        if (!confirmar(parent, form, titulo)) {
            return null;
        }
        if (cpf.getText().isBlank()) {
            erro(parent, "Informe o CPF.");
            return null;
        }
        return cpf.getText().trim();
    }

    // ------------------------------- Internos -------------------------------

    /** Item de combo que exibe a conta de forma amigável e guarda o número. */
    private static final class ContaItem {
        final int numero;
        final String descricao;

        ContaItem(Conta conta) {
            this.numero = conta.getNumeroConta();
            String tipo = (conta.getClass() == ContaCorrente.class) ? "Corrente" : "Poupanca";
            this.descricao = "Conta " + conta.getNumeroConta() + " (" + tipo + ") — Saldo R$ "
                    + MOEDA.format(conta.getSaldo());
        }

        @Override
        public String toString() {
            return descricao;
        }
    }

    private static JComboBox<ContaItem> comboContas(Cliente cliente) {
        List<Conta> contas = cliente.getContas();
        if (contas == null || contas.isEmpty()) {
            return null;
        }
        JComboBox<ContaItem> combo = new JComboBox<>();
        for (Conta c : contas) {
            combo.addItem(new ContaItem(c));
        }
        combo.setFont(WinxTheme.FONTE_NORMAL);
        return combo;
    }

    private static JTextField campo() {
        JTextField f = new JTextField(18);
        f.setFont(WinxTheme.FONTE_NORMAL);
        f.setBorder(WinxTheme.bordaCampo());
        return f;
    }

    /** Monta um formulário vertical a partir de pares (rótulo, componente). */
    private static JPanel form(Object... pares) {
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setOpaque(false);
        for (int i = 0; i < pares.length; i += 2) {
            JLabel rotulo = WinxTheme.rotulo((String) pares[i]);
            rotulo.setAlignmentX(Component.LEFT_ALIGNMENT);
            JComponent comp = (JComponent) pares[i + 1];
            comp.setAlignmentX(Component.LEFT_ALIGNMENT);
            comp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
            painel.add(rotulo);
            painel.add(javax.swing.Box.createVerticalStrut(4));
            painel.add(comp);
            if (i + 2 < pares.length) {
                painel.add(javax.swing.Box.createVerticalStrut(12));
            }
        }
        // Usa a altura calculada pelo BoxLayout (evita cortar o ultimo campo) e
        // apenas garante uma largura minima confortavel para o dialogo.
        Dimension preferida = painel.getPreferredSize();
        painel.setPreferredSize(new Dimension(Math.max(320, preferida.width), preferida.height));
        return painel;
    }

    private static boolean confirmar(Component parent, JComponent form, String titulo) {
        int opc = JOptionPane.showConfirmDialog(parent, form, titulo,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        return opc == JOptionPane.OK_OPTION;
    }

    private static Double parseValor(Component parent, String texto) {
        try {
            return Double.parseDouble(texto.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            erro(parent, "Valor numerico invalido: \"" + texto + "\"");
            return null;
        }
    }

    private static Integer parseInteiro(Component parent, String texto, String campo) {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            erro(parent, "Valor invalido para " + campo + ": \"" + texto + "\"");
            return null;
        }
    }

    private static void erro(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Atencao", JOptionPane.WARNING_MESSAGE);
    }
}
