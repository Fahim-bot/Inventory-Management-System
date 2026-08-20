package ui;

import javax.swing.*;
import java.awt.*;

/**
 * A JTextField that always shows grey hint text when empty and unfocused —
 * regardless of which Look-and-Feel is active.
 *
 * The rest of the app used to rely on FlatLaf's "JTextField.placeholderText"
 * client property, but that hint is invisible under the Nimbus fallback
 * (used whenever flatlaf-*.jar isn't present in lib/). This component paints
 * the placeholder itself, so it always shows up.
 */
public class PlaceholderTextField extends JTextField {

    private final String placeholder;

    public PlaceholderTextField(int columns, String placeholder) {
        super(columns);
        this.placeholder = placeholder;
        // Keep setting the FlatLaf property too, in case FlatLaf IS present —
        // no harm either way, and it avoids any double-painting under FlatLaf.
        putClientProperty("JTextField.placeholderText", placeholder);
    }

    public PlaceholderTextField(String text, String placeholder) {
        super(text);
        this.placeholder = placeholder;
        putClientProperty("JTextField.placeholderText", placeholder);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (placeholder == null || placeholder.isEmpty()) return;
        if (!getText().isEmpty()) return;
        // If FlatLaf is active it already paints the placeholder itself via the
        // client property, so skip our own painting to avoid drawing it twice.
        String lafName = UIManager.getLookAndFeel() == null ? "" :
                UIManager.getLookAndFeel().getClass().getName().toLowerCase();
        if (lafName.contains("flatlaf")) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(150, 160, 175));
        g2.setFont(getFont());
        Insets ins = getInsets();
        FontMetrics fm = g2.getFontMetrics();
        int x = ins.left + 2;
        int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(placeholder, x, y);
        g2.dispose();
    }
}
