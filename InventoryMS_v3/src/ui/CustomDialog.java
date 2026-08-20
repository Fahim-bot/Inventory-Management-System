package ui;

import service.SoundManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class CustomDialog extends JDialog {
    public static final int OK_OPTION     = 0;
    public static final int CANCEL_OPTION = 1;

    private int result = CANCEL_OPTION;
    private final JPanel contentPanel;

    public CustomDialog(Window parent, String title, String message) {
        super(parent, title, ModalityType.APPLICATION_MODAL);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        JPanel main = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // shadow
                g2.setColor(new Color(0, 0, 0, 25));
                g2.fillRoundRect(5, 5, getWidth() - 2, getHeight() - 2, Theme.CORNER_RADIUS, Theme.CORNER_RADIUS);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 7, getHeight() - 7, Theme.CORNER_RADIUS, Theme.CORNER_RADIUS);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        main.setOpaque(false);
        main.setBorder(new EmptyBorder(24, 26, 24, 26));

        /* ── Header ── */
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Theme.TEXT_PRIMARY);
        header.add(titleLabel, BorderLayout.WEST);

        JButton closeBtn = new JButton("✕");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        closeBtn.setForeground(Theme.TEXT_SECONDARY);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> { result = CANCEL_OPTION; dispose(); });
        header.add(closeBtn, BorderLayout.EAST);

        JLabel msgLabel = new JLabel(message);
        msgLabel.setFont(Theme.SMALL_FONT);
        msgLabel.setForeground(Theme.TEXT_SECONDARY);
        msgLabel.setBorder(new EmptyBorder(5, 0, 14, 0));

        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.add(header,   BorderLayout.NORTH);
        north.add(msgLabel, BorderLayout.SOUTH);
        main.add(north, BorderLayout.NORTH);

        /* ── Scrollable content — tall forms no longer get clipped ── */
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBackground(Color.WHITE);

        JScrollPane scroll = new JScrollPane(contentPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getViewport().setBackground(Color.WHITE);
        // Allow JComboBox popups to extend beyond the scroll pane
        scroll.getViewport().putClientProperty("JComponent.sizeVariant", "regular");
        main.add(scroll, BorderLayout.CENTER);

        /* ── Buttons ── */
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);
        btnRow.setBorder(new EmptyBorder(16, 0, 0, 0));

        JButton okBtn     = styledBtn("  Save  ",   Theme.PRIMARY);
        JButton cancelBtn = styledBtn("  Cancel  ", Theme.TEXT_SECONDARY);

        okBtn.addActionListener(e -> {
            SoundManager.playSave();
            result = OK_OPTION;
            dispose();
        });
        cancelBtn.addActionListener(e -> { result = CANCEL_OPTION; dispose(); });

        btnRow.add(cancelBtn);
        btnRow.add(okBtn);
        main.add(btnRow, BorderLayout.SOUTH);

        bind("ENTER",  "ok",     okBtn);
        bind("ESCAPE", "cancel", cancelBtn);

        add(main);
        // Larger default — all 6 product fields + header + buttons fit comfortably
        setSize(640, 600);
        setLocationRelativeTo(parent);
    }

    private JButton styledBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setBorder(new EmptyBorder(10, 22, 10, 22));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setOpaque(true);
        return b;
    }

    private void bind(String key, String name, JButton btn) {
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                     .put(KeyStroke.getKeyStroke(key), name);
        getRootPane().getActionMap().put(name, new AbstractAction() {
            public void actionPerformed(ActionEvent e) { btn.doClick(); }
        });
    }

    public void setContent(JPanel content) {
        contentPanel.removeAll();
        contentPanel.add(content, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public int showDialog() {
        setVisible(true);
        return result;
    }
}
