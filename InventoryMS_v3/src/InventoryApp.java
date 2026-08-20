import ui.LoginFrame;

import javax.swing.*;

public class InventoryApp {
    public static void main(String[] args) {
        // Crisp fonts on Windows
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Try FlatLaf if present on classpath; fall back silently to Nimbus
        try {
            Class<?> flat = Class.forName("com.formdev.flatlaf.FlatLightLaf");
            UIManager.setLookAndFeel((javax.swing.LookAndFeel) flat.getDeclaredConstructor().newInstance());
        } catch (ClassNotFoundException ignored) {
            // FlatLaf jar not found — use Nimbus as a clean fallback
            try { UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel"); }
            catch (Exception e) { /* stay on Metal */ }
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
