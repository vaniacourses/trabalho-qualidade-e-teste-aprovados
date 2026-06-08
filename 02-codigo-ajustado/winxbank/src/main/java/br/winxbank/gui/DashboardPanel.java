package br.winxbank.gui;

import br.winxbank.sistemabancario.Conta;
import br.winxbank.sistemabancario.ContaCorrente;
import br.winxbank.sistemaclientes.Cliente;
import br.winxbank.sistemaclientes.ClienteWinx;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.text.DecimalFormat;
import java.util.function.Supplier;

/**
 * Painel do cliente logado: dados do perfil, contas e todas as operações
 * bancárias (espelhando o menu do programa de console).
 */
public class DashboardPanel extends JPanel {

    private static final DecimalFormat MOEDA = new DecimalFormat("0.00");

    private final WinxBankGUI app;

    private final JLabel nomeLabel = new JLabel();
    private final JLabel cpfLabel = new JLabel();
    private final JLabel tipoLabel = new JLabel();
    private final JLabel pontosLabel = new JLabel();

    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"Numero", "Tipo", "Saldo", "Divida", "Cartao Deb.", "Cartao Cred.", "Fatura"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tabela = new JTable(modelo);

    public DashboardPanel(WinxBankGUI app) {
        this.app = app;
        setOpaque(false);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(24, 28, 12, 28));

        add(criarPerfil(), BorderLayout.NORTH);
        add(criarContas(), BorderLayout.CENTER);
        add(criarOperacoes(), BorderLayout.SOUTH);
    }

    private JPanel criarPerfil() {
        JPanel card = WinxTheme.card();
        card.setLayout(new BorderLayout());

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        nomeLabel.setFont(WinxTheme.FONTE_TITULO);
        nomeLabel.setForeground(WinxTheme.PRIMARIA_ESCURA);
        cpfLabel.setFont(WinxTheme.FONTE_NORMAL);
        cpfLabel.setForeground(WinxTheme.TEXTO_SUAVE);
        JPanel tags = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        tags.setOpaque(false);
        tipoLabel.setFont(WinxTheme.FONTE_PEQUENA.deriveFont(java.awt.Font.BOLD));
        tipoLabel.setForeground(WinxTheme.PRIMARIA);
        pontosLabel.setFont(WinxTheme.FONTE_PEQUENA.deriveFont(java.awt.Font.BOLD));
        pontosLabel.setForeground(WinxTheme.ACENTO);
        tags.add(tipoLabel);
        tags.add(pontosLabel);
        info.add(nomeLabel);
        info.add(cpfLabel);
        info.add(javax.swing.Box.createVerticalStrut(4));
        info.add(tags);

        JButton logout = WinxTheme.botaoSecundario("Sair");
        logout.addActionListener(e -> app.goToLogin());
        JPanel topo = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 0, 0));
        topo.setOpaque(false);
        topo.add(logout);

        card.add(info, BorderLayout.WEST);
        card.add(topo, BorderLayout.EAST);
        return card;
    }

    private JPanel criarContas() {
        JPanel card = WinxTheme.card();
        card.setLayout(new BorderLayout(0, 12));
        card.add(WinxTheme.subtitulo("Minhas contas"), BorderLayout.NORTH);

        tabela.setRowHeight(28);
        tabela.setFont(WinxTheme.FONTE_NORMAL);
        tabela.setForeground(WinxTheme.TEXTO);
        tabela.getTableHeader().setFont(WinxTheme.FONTE_SUBTITULO.deriveFont(13f));
        tabela.setShowVerticalLines(false);
        tabela.setGridColor(WinxTheme.BORDA);
        JScrollPane scroll = new JScrollPane(tabela);
        scroll.setBorder(new WinxTheme.RoundedBorder(WinxTheme.BORDA, 12));
        scroll.getViewport().setBackground(WinxTheme.CARD);
        scroll.setPreferredSize(new Dimension(0, 180));
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private JPanel criarOperacoes() {
        JPanel card = WinxTheme.card();
        card.setLayout(new BorderLayout(0, 12));
        card.add(WinxTheme.subtitulo("Operacoes"), BorderLayout.NORTH);

        JPanel grade = new JPanel(new GridLayout(0, 4, 10, 10));
        grade.setOpaque(false);

        grade.add(acao("Abrir conta", this::abrirConta));
        grade.add(acao("Fechar conta", this::fecharConta));
        grade.add(acao("Depositar", this::depositar));
        grade.add(acao("Comprar", this::comprar));
        grade.add(acao("Fazer Pix", this::pix));
        grade.add(acao("Sacar", this::sacar));
        grade.add(acao("Pagar fatura", this::pagarFatura));
        grade.add(acao("Ajustar limite", this::ajustarLimite));
        grade.add(acao("Pagar parcela", this::pagarParcela));
        grade.add(acao("Requisitar emprestimo", this::requisitarEmprestimo));
        grade.add(acao("Converter pontos", this::converterPontos));
        grade.add(acao("Gerar extrato", this::gerarExtrato));
        grade.add(acao("Gerar informe", this::gerarInforme));
        grade.add(acaoPerigo("Apagar usuario", this::apagarUsuario));

        card.add(grade, BorderLayout.CENTER);
        return card;
    }

    private JButton acao(String texto, Runnable handler) {
        JButton b = WinxTheme.botaoPrimario(texto);
        b.addActionListener(e -> handler.run());
        return b;
    }

    private JButton acaoPerigo(String texto, Runnable handler) {
        JButton b = WinxTheme.botaoSecundario(texto);
        b.setForeground(WinxTheme.ERRO);
        b.addActionListener(e -> handler.run());
        return b;
    }

    // ------------------------------- Ações ----------------------------------

    private void abrirConta() {
        Dialogs.NovoUsuario d = Dialogs.abrirConta(this);
        if (d == null) {
            return;
        }
        app.aplicar(app.controller().abrirConta(app.session(), d.tipoConta(), d.saldo()));
    }

    private void fecharConta() {
        Integer conta = Dialogs.selecionarConta(this, app.session(), "Fechar conta");
        if (conta == null) {
            return;
        }
        app.aplicar(app.controller().fecharConta(app.session(), conta));
    }

    private void depositar() {
        Dialogs.ContaValor d = Dialogs.contaEValor(this, app.session(), "Depositar", "Valor do deposito");
        if (d == null) {
            return;
        }
        app.aplicar(app.controller().depositar(app.session(), d.numeroConta(), d.valor()));
    }

    private void comprar() {
        Dialogs.CompraDados d = Dialogs.compra(this, app.session());
        if (d == null) {
            return;
        }
        app.aplicar(app.controller().comprar(app.session(), d.numeroConta(), d.valor(), d.formaPagamento()));
    }

    private void pix() {
        Dialogs.PixDados d = Dialogs.pix(this, app.session());
        if (d == null) {
            return;
        }
        app.aplicar(app.controller().pix(app.session(), d.contaOrigem(), d.cpfDestino(), d.contaDestino(), d.valor()));
    }

    private void sacar() {
        Dialogs.ContaValor d = Dialogs.contaEValor(this, app.session(), "Sacar", "Valor do saque");
        if (d == null) {
            return;
        }
        app.aplicar(app.controller().sacar(app.session(), d.numeroConta(), d.valor()));
    }

    private void pagarFatura() {
        Dialogs.ContaValor d = Dialogs.contaEValor(this, app.session(), "Pagar fatura", "Valor a pagar");
        if (d == null) {
            return;
        }
        app.aplicar(app.controller().pagarFatura(app.session(), d.numeroConta(), d.valor()));
    }

    private void ajustarLimite() {
        Dialogs.ContaValor d = Dialogs.contaEValor(this, app.session(), "Ajustar limite", "Novo limite");
        if (d == null) {
            return;
        }
        app.aplicar(app.controller().ajustarLimite(app.session(), d.numeroConta(), d.valor()));
    }

    private void pagarParcela() {
        Dialogs.ContaValor d = Dialogs.contaEValor(this, app.session(), "Pagar parcela", "Valor da parcela");
        if (d == null) {
            return;
        }
        app.aplicar(app.controller().pagarParcelaEmprestimo(app.session(), d.numeroConta(), d.valor()));
    }

    private void requisitarEmprestimo() {
        Dialogs.ContaValor d = Dialogs.contaEValor(this, app.session(), "Requisitar emprestimo", "Valor do emprestimo");
        if (d == null) {
            return;
        }
        app.aplicar(app.controller().requisitarEmprestimo(app.session(), d.numeroConta(), d.valor()));
    }

    private void converterPontos() {
        Integer conta = Dialogs.selecionarConta(this, app.session(), "Converter pontos em saldo");
        if (conta == null) {
            return;
        }
        app.aplicar(app.controller().converterPontos(app.session(), conta));
    }

    private void gerarExtrato() {
        Integer conta = Dialogs.selecionarConta(this, app.session(), "Gerar extrato");
        if (conta == null) {
            return;
        }
        app.aplicar(app.controller().gerarExtrato(app.session(), conta));
    }

    private void gerarInforme() {
        Integer conta = Dialogs.selecionarConta(this, app.session(), "Gerar informe de rendimento");
        if (conta == null) {
            return;
        }
        app.aplicar(app.controller().gerarInformeRendimento(app.session(), conta));
    }

    private void apagarUsuario() {
        int opc = JOptionPane.showConfirmDialog(this,
                "Tem certeza que deseja apagar o seu usuario? Esta acao nao pode ser desfeita.",
                "Apagar usuario", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (opc != JOptionPane.YES_OPTION) {
            return;
        }
        executarComLogout(() -> app.controller().apagarUsuario(app.session()));
    }

    /** Executa uma operação que pode encerrar a sessão e navega para o login se necessário. */
    private void executarComLogout(Supplier<ActionResult> operacao) {
        app.aplicar(operacao.get());
        if (!app.logado()) {
            app.goToLogin();
        }
    }

    // ----------------------------- Atualização ------------------------------

    /** Reconstrói o perfil e a tabela de contas a partir do cliente logado. */
    public void atualizar() {
        Cliente cliente = app.session();
        if (cliente == null || cliente.getCpf() == null) {
            modelo.setRowCount(0);
            return;
        }
        nomeLabel.setText(cliente.getNome());
        cpfLabel.setText("CPF: " + cliente.getCpf());
        boolean winx = cliente.getClass() == ClienteWinx.class;
        tipoLabel.setText(winx ? "★ ClienteWinx" : "Cliente comum");
        pontosLabel.setText(winx ? (((ClienteWinx) cliente).getPontosDeCompra() + " pontos") : "");

        modelo.setRowCount(0);
        if (cliente.getContas() != null) {
            for (Conta conta : cliente.getContas()) {
                boolean corrente = conta.getClass() == ContaCorrente.class;
                ContaCorrente cc = corrente ? (ContaCorrente) conta : null;
                modelo.addRow(new Object[]{
                        conta.getNumeroConta(),
                        corrente ? "Corrente" : "Poupanca",
                        "R$ " + MOEDA.format(conta.getSaldo()),
                        "R$ " + MOEDA.format(conta.getDividaDeEmprestimo()),
                        conta.getCartao().getNumero(),
                        corrente ? cc.getCartaoCredito().getNumero() : "-",
                        corrente ? "R$ " + MOEDA.format(cc.getCartaoCredito().getFatura()) : "-"
                });
            }
        }
    }
}
