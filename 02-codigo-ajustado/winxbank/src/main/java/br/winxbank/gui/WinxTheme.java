package br.winxbank.gui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Tema visual do WinxBank: paleta, fontes e fábricas de componentes estilizados.
 *
 * <p>Usa apenas Swing puro (Look &amp; Feel Nimbus quando disponível), sem
 * adicionar dependências ao projeto.</p>
 */
public final class WinxTheme {

    // Paleta "Winx" — tons de roxo/índigo com acento magenta e ciano.
    public static final Color FUNDO = new Color(0xF4, 0xF2, 0xFB);
    public static final Color CARD = Color.WHITE;
    public static final Color PRIMARIA = new Color(0x6C, 0x4F, 0xD8);
    public static final Color PRIMARIA_ESCURA = new Color(0x4B, 0x33, 0xA8);
    public static final Color ACENTO = new Color(0xE6, 0x4A, 0xA8);
    public static final Color CIANO = new Color(0x2E, 0xC4, 0xC6);
    public static final Color TEXTO = new Color(0x2B, 0x27, 0x40);
    public static final Color TEXTO_SUAVE = new Color(0x6E, 0x69, 0x86);
    public static final Color SUCESSO = new Color(0x1E, 0x9E, 0x63);
    public static final Color ERRO = new Color(0xD2, 0x3F, 0x3F);
    public static final Color BORDA = new Color(0xE2, 0xDE, 0xF2);

    public static final Font FONTE_TITULO = new Font("SansSerif", Font.BOLD, 22);
    public static final Font FONTE_SUBTITULO = new Font("SansSerif", Font.BOLD, 15);
    public static final Font FONTE_NORMAL = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font FONTE_PEQUENA = new Font("SansSerif", Font.PLAIN, 12);
    public static final Font FONTE_MONO = new Font("Monospaced", Font.PLAIN, 12);

    private WinxTheme() {
    }

    /** Aplica o Look &amp; Feel Nimbus e ajustes globais de cor. */
    public static void aplicar() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
            UIManager.put("control", FUNDO);
            UIManager.put("nimbusBase", PRIMARIA_ESCURA);
            UIManager.put("nimbusBlueGrey", new Color(0xD6, 0xD0, 0xEC));
            UIManager.put("nimbusFocus", ACENTO);
            UIManager.put("nimbusSelectionBackground", PRIMARIA);
            UIManager.put("Table.alternateRowColor", new Color(0xF6, 0xF4, 0xFC));
            UIManager.put("defaultFont", FONTE_NORMAL);
        } catch (Exception e) {
            // Sem Nimbus: segue com o Look & Feel padrão da plataforma.
        }
    }

    /** Botão primário (preenchido) com cantos arredondados. */
    public static JButton botaoPrimario(String texto) {
        return botao(texto, PRIMARIA, Color.WHITE);
    }

    /** Botão de acento, para a ação de destaque da tela. */
    public static JButton botaoAcento(String texto) {
        return botao(texto, ACENTO, Color.WHITE);
    }

    /** Botão secundário (claro), para ações de apoio. */
    public static JButton botaoSecundario(String texto) {
        JButton b = botao(texto, CARD, PRIMARIA_ESCURA);
        b.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(BORDA, 12),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)));
        return b;
    }

    private static JButton botao(String texto, Color fundo, Color frente) {
        JButton b = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = fundo;
                if (getModel().isPressed()) {
                    base = base.darker();
                } else if (getModel().isRollover()) {
                    base = mesclar(base, Color.WHITE, 0.12);
                }
                g2.setColor(base);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setForeground(frente);
        b.setFont(FONTE_SUBTITULO.deriveFont(13f));
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setOpaque(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));
        return b;
    }

    /** Painel branco com cantos arredondados e leve borda — um "card". */
    public static JPanel card() {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.setColor(BORDA);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        return p;
    }

    public static JLabel titulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(FONTE_TITULO);
        l.setForeground(PRIMARIA_ESCURA);
        return l;
    }

    public static JLabel subtitulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(FONTE_SUBTITULO);
        l.setForeground(TEXTO);
        return l;
    }

    public static JLabel rotulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(FONTE_NORMAL);
        l.setForeground(TEXTO_SUAVE);
        return l;
    }

    public static Border bordaCampo() {
        return BorderFactory.createCompoundBorder(
                new RoundedBorder(BORDA, 10),
                BorderFactory.createEmptyBorder(6, 10, 6, 10));
    }

    static Color mesclar(Color a, Color b, double t) {
        return new Color(
                (int) (a.getRed() + (b.getRed() - a.getRed()) * t),
                (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t));
    }

    /** Borda arredondada simples reutilizável por campos e botões. */
    public static final class RoundedBorder implements Border {
        private final Color cor;
        private final int raio;

        public RoundedBorder(Color cor, int raio) {
            this.cor = cor;
            this.raio = raio;
        }

        @Override
        public void paintBorder(java.awt.Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(cor);
            g2.drawRoundRect(x, y, w - 1, h - 1, raio, raio);
            g2.dispose();
        }

        @Override
        public java.awt.Insets getBorderInsets(java.awt.Component c) {
            return new java.awt.Insets(4, 8, 4, 8);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }

        public static void apply(JComponent comp, int raio) {
            comp.setBorder(new RoundedBorder(BORDA, raio));
        }
    }
}
