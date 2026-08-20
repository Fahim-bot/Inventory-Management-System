package ui;

import service.SoundManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.function.Consumer;

public class Sidebar extends JPanel {
    private final String role;
    private Consumer<String> navListener;
    private Runnable logoutListener;
    private JButton activeButton;
    private final JPanel navPanel;

    private static final String[] ADMIN_PAGES    = {"Dashboard","Products","Sales","Orders","Reports"};
    private static final String[] EMPLOYEE_PAGES = {"Dashboard","Products","Sales","Orders"};

    public Sidebar(String role) {
        this.role = role;
        setBackground(Theme.SIDEBAR);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 0, 20, 0));

        // Logo / header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 20, 28, 20));
        JLabel logo = new JLabel("📦  InventoryMS");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logo.setForeground(Color.WHITE);
        header.add(logo, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // Nav buttons
        navPanel = new JPanel();
        navPanel.setOpaque(false);
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBorder(new EmptyBorder(0, 12, 0, 12));

        String[] pages = role.equals("admin") ? ADMIN_PAGES : EMPLOYEE_PAGES;
        for (String page : pages) navPanel.add(navBtn(page));

        navPanel.add(Box.createVerticalGlue());

        // Logout
        JButton logoutBtn = navBtn("Logout");
        logoutBtn.setForeground(new Color(252, 165, 165));
        logoutBtn.addActionListener(e -> {
            SoundManager.playClick();
            int choice = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION && logoutListener != null) logoutListener.run();
        });
        navPanel.add(logoutBtn);

        add(navPanel, BorderLayout.CENTER);
    }

    private JButton navBtn(String text) {
        JButton btn = new JButton(icon(text) + "   " + text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setForeground(Theme.TEXT_LIGHT);
        btn.setBackground(Theme.SIDEBAR);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(12, 14, 12, 14));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, btn.getPreferredSize().height + 8));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (btn != activeButton) btn.setBackground(Theme.SIDEBAR_HOVER);
            }
            @Override public void mouseExited(MouseEvent e) {
                if (btn != activeButton) btn.setBackground(Theme.SIDEBAR);
            }
            @Override public void mouseClicked(MouseEvent e) {
                if ("Logout".equals(text)) return;
                SoundManager.playClick();
                activate(btn);
                if (navListener != null) navListener.accept(text);
            }
        });
        return btn;
    }

    private void activate(JButton btn) {
        if (activeButton != null) {
            activeButton.setBackground(Theme.SIDEBAR);
            activeButton.setForeground(Theme.TEXT_LIGHT);
        }
        activeButton = btn;
        btn.setBackground(Theme.SIDEBAR_ACTIVE);
        btn.setForeground(Color.WHITE);
    }

    public void setActive(String page) {
        for (Component c : navPanel.getComponents()) {
            if (c instanceof JButton) {
                JButton btn = (JButton) c;
                // strip the icon prefix then compare
                String label = btn.getText().replaceAll("^.{1,4}\\s+", "").trim();
                if (label.equals(page)) { activate(btn); break; }
            }
        }
    }

    public void addSidebarListener(Consumer<String> listener) { this.navListener = listener; }
    public void addLogoutListener(Runnable listener) { this.logoutListener = listener; }

    private static String icon(String page) {
        switch (page) {
            case "Dashboard": return "📊";
            case "Products":  return "📦";
            case "Sales":     return "💰";
            case "Orders":    return "🚚";
            case "Reports":   return "📈";
            case "Logout":    return "🚪";
            default:          return "•";
        }
    }
}
