package br.winxbank.gui;

import br.winxbank.sistemaclientes.Cliente;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JLabel;

/**
 * Janela principal da interface gráfica do WinxBank.
 *
 * <p>Mantém o estado da sessão (cliente logado), navega entre a tela de login e
 * o painel do cliente (CardLayout) e concentra o console de saída onde são
 * exibidas as mensagens das operações — as mesmas do programa de console.</p>
 *
 * <p>Esta é a classe de entrada da GUI:
 * {@code mvn compile exec:java -Dexec.mainClass=br.winxbank.gui.WinxBankGUI}.</p>
 */
public class WinxBankGUI extends JFrame {

    private final WinxBankController controller = new WinxBankController();
    private Cliente session = new Cliente();

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);
    private final LoginPanel loginPanel;
    private final DashboardPanel dashboardPanel;

    private final JTextArea console = new JTextArea(6, 40);
    private final JLabel mesLabel = new JLabel();

    public WinxBankGUI() {
        super("WinxBank");
        controller.bootstrap();

        this.loginPanel = new LoginPanel(this);
        this.dashboardPanel = new DashboardPanel(this);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 680));
        getContentPane().setBackground(WinxTheme.FUNDO);
        setLayout(new BorderLayout());

        add(criarCabecalho(), BorderLayout.NORTH);

        cards.setOpaque(false);
        cards.add(loginPanel, "login");
        cards.add(dashboardPanel, "dashboard");
        add(cards, BorderLayout.CENTER);

        add(criarConsole(), BorderLayout.SOUTH);

        goToLogin();
        setLocationRelativeTo(null);
    }

    // ------------------------------- Layout ---------------------------------

    private JPanel criarCabecalho() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, WinxTheme.PRIMARIA_ESCURA,
                        getWidth(), 0, WinxTheme.ACENTO));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        header.setPreferredSize(new Dimension(0, 84));
        header.setBorder(BorderFactory.createEmptyBorder(0, 28, 0, 28));

        JPanel titulos = new JPanel();
        titulos.setOpaque(false);
        titulos.setLayout(new javax.swing.BoxLayout(titulos, javax.swing.BoxLayout.Y_AXIS));
        JLabel marca = new JLabel("WinxBank");
        marca.setFont(WinxTheme.FONTE_TITULO.deriveFont(26f));
        marca.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Sistema bancario digital");
        sub.setFont(WinxTheme.FONTE_PEQUENA);
        sub.setForeground(new Color(255, 255, 255, 200));
        marca.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        sub.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        // Glue acima e abaixo centraliza o titulo verticalmente no cabecalho.
        titulos.add(javax.swing.Box.createVerticalGlue());
        titulos.add(marca);
        titulos.add(sub);
        titulos.add(javax.swing.Box.createVerticalGlue());

        mesLabel.setFont(WinxTheme.FONTE_SUBTITULO);
        mesLabel.setForeground(Color.WHITE);

        header.add(titulos, BorderLayout.WEST);
        header.add(mesLabel, BorderLayout.EAST);
        return header;
    }

    private JPanel criarConsole() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(WinxTheme.FUNDO);
        wrapper.setBorder(BorderFactory.createEmptyBorder(8, 16, 16, 16));

        JLabel titulo = WinxTheme.rotulo("Saida do sistema");
        titulo.setBorder(BorderFactory.createEmptyBorder(0, 4, 6, 0));

        console.setEditable(false);
        console.setFont(WinxTheme.FONTE_MONO);
        console.setBackground(new Color(0x1E, 0x1B, 0x2E));
        console.setForeground(new Color(0xE8, 0xE4, 0xF6));
        console.setLineWrap(true);
        console.setWrapStyleWord(true);
        console.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JScrollPane scroll = new JScrollPane(console);
        scroll.setBorder(new WinxTheme.RoundedBorder(WinxTheme.BORDA, 12));
        scroll.setPreferredSize(new Dimension(0, 150));

        wrapper.add(titulo, BorderLayout.NORTH);
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    // ------------------------------- API ------------------------------------

    public WinxBankController controller() {
        return controller;
    }

    public Cliente session() {
        return session;
    }

    public boolean logado() {
        return session != null && session.getCpf() != null;
    }

    /** Aplica o resultado de uma operação: mostra a mensagem, atualiza sessão e telas. */
    public void aplicar(ActionResult resultado) {
        if (resultado.isSessionChanged()) {
            session = resultado.getUpdatedCliente();
        }
        appendOutput(resultado.getMessage(), resultado.isOk());
        refreshAll();
    }

    public void appendOutput(String texto, boolean ok) {
        if (texto == null || texto.isBlank()) {
            texto = ok ? "Operacao concluida." : "Operacao nao realizada.";
        }
        String prefixo = ok ? "[ok] " : "[!] ";
        console.append(prefixo + texto.replace("\n", "\n      ") + "\n");
        console.setCaretPosition(console.getDocument().getLength());
    }

    public void goToLogin() {
        session = new Cliente();
        refreshAll();
        cardLayout.show(cards, "login");
    }

    public void goToDashboard() {
        refreshAll();
        cardLayout.show(cards, "dashboard");
    }

    /** Atualiza cabeçalho e ambas as telas a partir do estado atual do modelo. */
    public void refreshAll() {
        mesLabel.setText("Mes atual:  " + controller.getMesAtual());
        loginPanel.atualizar();
        dashboardPanel.atualizar();
    }

    // ------------------------------- Main -----------------------------------

    public static void main(String[] args) {
        WinxTheme.aplicar();
        SwingUtilities.invokeLater(() -> new WinxBankGUI().setVisible(true));
    }
}
