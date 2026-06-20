package br.winxbank.gui;

import br.winxbank.sistemaclientes.Cliente;
import br.winxbank.sistemaclientes.ClienteWinx;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.text.DecimalFormat;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;

/**
 * Tela inicial: criação de usuário, login por CPF, listagem de clientes
 * registrados e visão geral do banco.
 */
public class LoginPanel extends JPanel {

    private static final DecimalFormat MOEDA = new DecimalFormat("0.00");

    private final WinxBankGUI app;
    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"Nome", "CPF", "Tipo", "Pontos", "Contas"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tabela = new JTable(modelo);
    private final JLabel receitasLabel = new JLabel();
    private final JLabel despesasLabel = new JLabel();

    public LoginPanel(WinxBankGUI app) {
        this.app = app;
        setOpaque(false);
        setLayout(new BorderLayout(18, 0));
        setBorder(BorderFactory.createEmptyBorder(24, 28, 12, 28));

        add(criarAreaClientes(), BorderLayout.CENTER);
        add(criarPainelBanco(), BorderLayout.EAST);
        atualizar();
    }

    private JPanel criarAreaClientes() {
        JPanel card = WinxTheme.card();
        card.setLayout(new BorderLayout(0, 14));

        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setOpaque(false);
        JPanel titulos = new JPanel();
        titulos.setOpaque(false);
        titulos.setLayout(new BoxLayout(titulos, BoxLayout.Y_AXIS));
        titulos.add(WinxTheme.titulo("Bem-vindo ao WinxBank"));
        titulos.add(WinxTheme.rotulo("Crie um usuario ou entre com o seu CPF para comecar."));
        cabecalho.add(titulos, BorderLayout.WEST);

        JPanel acoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acoes.setOpaque(false);
        JButton criar = WinxTheme.botaoAcento("Criar usuario");
        JButton entrar = WinxTheme.botaoPrimario("Entrar");
        criar.addActionListener(e -> criarUsuario());
        entrar.addActionListener(e -> entrar());
        acoes.add(criar);
        acoes.add(entrar);
        cabecalho.add(acoes, BorderLayout.EAST);

        tabela.setRowHeight(28);
        tabela.setFont(WinxTheme.FONTE_NORMAL);
        tabela.setForeground(WinxTheme.TEXTO);
        tabela.getTableHeader().setFont(WinxTheme.FONTE_SUBTITULO.deriveFont(13f));
        tabela.setShowVerticalLines(false);
        tabela.setGridColor(WinxTheme.BORDA);
        JScrollPane scroll = new JScrollPane(tabela);
        scroll.setBorder(new WinxTheme.RoundedBorder(WinxTheme.BORDA, 12));
        scroll.getViewport().setBackground(WinxTheme.CARD);

        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        rodape.setOpaque(false);
        JButton limpar = WinxTheme.botaoSecundario("Limpar clientes");
        limpar.addActionListener(e -> limparClientes());
        rodape.add(limpar);

        card.add(cabecalho, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        card.add(rodape, BorderLayout.SOUTH);
        return card;
    }

    private JPanel criarPainelBanco() {
        JPanel card = WinxTheme.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(240, 0));

        card.add(WinxTheme.subtitulo("Dados do banco"));
        card.add(javax.swing.Box.createVerticalStrut(14));

        card.add(WinxTheme.rotulo("Receitas"));
        receitasLabel.setFont(WinxTheme.FONTE_TITULO.deriveFont(20f));
        receitasLabel.setForeground(WinxTheme.SUCESSO);
        card.add(receitasLabel);
        card.add(javax.swing.Box.createVerticalStrut(14));

        card.add(WinxTheme.rotulo("Despesas"));
        despesasLabel.setFont(WinxTheme.FONTE_TITULO.deriveFont(20f));
        despesasLabel.setForeground(WinxTheme.ERRO);
        card.add(despesasLabel);
        card.add(javax.swing.Box.createVerticalGlue());
        return card;
    }

    // ------------------------------- Ações ----------------------------------

    private void criarUsuario() {
        Dialogs.NovoUsuario dados = Dialogs.novoUsuario(this);
        if (dados == null) {
            return;
        }
        app.aplicar(app.controller().cadastrarCliente(
                dados.nome(), dados.cpf(), dados.tipoConta(), dados.saldo()));
    }

    private void entrar() {
        String cpf = Dialogs.pedirCpf(this, "Entrar");
        if (cpf == null) {
            return;
        }
        ActionResult r = app.controller().login(cpf);
        app.aplicar(r);
        if (app.logado()) {
            app.goToDashboard();
        }
    }

    private void limparClientes() {
        int opc = JOptionPane.showConfirmDialog(this,
                "Tem certeza que deseja remover todos os clientes?",
                "Limpar clientes", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (opc == JOptionPane.YES_OPTION) {
            app.aplicar(app.controller().limparClientes());
        }
    }

    // ----------------------------- Atualização ------------------------------

    /** Recarrega a tabela de clientes e os números do banco a partir do modelo. */
    public void atualizar() {
        modelo.setRowCount(0);
        List<Cliente> clientes = app.controller().getClientes();
        for (Cliente c : clientes) {
            boolean winx = c.getClass() == ClienteWinx.class;
            modelo.addRow(new Object[]{
                    c.getNome(),
                    c.getCpf(),
                    winx ? "ClienteWinx" : "Comum",
                    winx ? ((ClienteWinx) c).getPontosDeCompra() : "-",
                    c.getContas() == null ? 0 : c.getContas().size()
            });
        }
        receitasLabel.setText("R$ " + MOEDA.format(app.controller().getReceitas()));
        despesasLabel.setText("R$ " + MOEDA.format(app.controller().getDespesas()));
    }
}
