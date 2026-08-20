package ui;

import java.awt.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import model.*;
import service.SoundManager;

public class DashboardPanel extends JPanel { //
    private final Inventory inventory;
    private final OrderManager orderManager;
    private final boolean isAdmin;

    private JLabel totalProductsLabel;
    private JLabel lowStockLabel;
    private JLabel pendingOrdersLabel;
    private JLabel revenueLabel;
    private JLabel profitLabel;
    private JLabel stockValueLabel;
    private JLabel todaySalesLabel;
    private JLabel overdueOrdersLabel;

    public DashboardPanel(Inventory inventory, OrderManager orderManager, String role) {
        this.inventory    = inventory;
        this.orderManager = orderManager;
        this.isAdmin      = "admin".equals(role);
        setBackground(Theme.BACKGROUND);
        setLayout(new BorderLayout(0, 20));
        setBorder(new EmptyBorder(0, 0, 10, 0));
        initComponents();
        refresh();
    }

    private void initComponents() {
        // Page header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Dashboard");
        title.setFont(Theme.HEADER_FONT);
        title.setForeground(Theme.TEXT_PRIMARY);
        JLabel sub = new JLabel("Real-time overview of your inventory");
        sub.setFont(Theme.SMALL_FONT);
        sub.setForeground(Theme.TEXT_SECONDARY);
        header.add(title, BorderLayout.NORTH);
        header.add(sub, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        // Stat cards — admins see the full financial picture; employees see
        // operational stats only (no revenue, sales, or stock-value figures).
        int cols = isAdmin ? 4 : 2;
        int rows = isAdmin ? 2 : 2;
        JPanel cards = new JPanel(new GridLayout(rows, cols, 16, 16));
        cards.setOpaque(false);

        totalProductsLabel = statLabel();
        lowStockLabel      = statLabel();
        pendingOrdersLabel = statLabel();
        overdueOrdersLabel = statLabel();

        cards.add(card("📦", "Total Products",   totalProductsLabel, Theme.GRADIENT_BLUE));
        cards.add(card("⚠️",  "Low Stock Items",  lowStockLabel,      Theme.GRADIENT_ORANGE));
        cards.add(card("🚚", "Pending Orders",   pendingOrdersLabel, Theme.GRADIENT_PURPLE));
        cards.add(card("🔴", "Overdue Orders",   overdueOrdersLabel, Theme.GRADIENT_RED));

        if (isAdmin) {
            revenueLabel    = statLabel();
            profitLabel     = statLabel();
            stockValueLabel = statLabel();
            todaySalesLabel = statLabel();

            cards.add(card("💰", "Total Revenue", revenueLabel,    Theme.GRADIENT_GREEN));
            cards.add(card("📈", "Net Profit",    profitLabel,     Theme.GRADIENT_PINK));
            cards.add(card("💎", "Stock Value",   stockValueLabel, Theme.GRADIENT_BLUE));
            cards.add(card("📊", "Today's Sales", todaySalesLabel, Theme.GRADIENT_ORANGE));
        }

        add(cards, BorderLayout.CENTER);

        // Quick-action strip
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        actions.setOpaque(false);
        actions.setBorder(new EmptyBorder(6, 0, 0, 0));
        actions.add(actionBtn("🔄  Refresh Data", Theme.INFO, e -> refresh()));
        add(actions, BorderLayout.SOUTH);
    }

    private JLabel statLabel() {
        JLabel lbl = new JLabel("0");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lbl.setForeground(Color.WHITE);
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        return lbl;
    }

    private JPanel card(String icon, String title, JLabel valueLabel, Color grad) {
        JPanel p = new JPanel(new BorderLayout(0, 4)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, grad, getWidth(), getHeight(), grad.darker()));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), Theme.CARD_RADIUS, Theme.CARD_RADIUS);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel ico = new JLabel(icon);
        ico.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
        ico.setForeground(new Color(255, 255, 255, 210));
        p.add(ico, BorderLayout.NORTH);

        p.add(valueLabel, BorderLayout.CENTER);

        JLabel lbl = new JLabel(title);
        lbl.setFont(Theme.SMALL_FONT);
        lbl.setForeground(new Color(255, 255, 255, 190));
        p.add(lbl, BorderLayout.SOUTH);
        return p;
    }

    private JButton actionBtn(String text, Color bg, java.awt.event.ActionListener al) {
        JButton btn = new JButton(text);
        btn.setFont(Theme.BODY_FONT);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setBorder(new EmptyBorder(9, 20, 9, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        btn.addActionListener(al);
        return btn;
    }

    public void refresh() {
        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.of("en", "IN"));

        totalProductsLabel.setText(String.valueOf(inventory.allProducts().size()));
        lowStockLabel.setText(String.valueOf(inventory.lowStockProducts().size()));
        pendingOrdersLabel.setText(String.valueOf(orderManager.pendingOrders().size()));

        long overdue = orderManager.allOrders().stream()
                .filter(CustomerOrder::isOverdue).count();
        overdueOrdersLabel.setText(String.valueOf(overdue));

        if (!isAdmin) return; // employees don't see revenue/sales/stock-value figures

        double revenue = 0, profit = 0, stockVal = 0, todaySales = 0;
        for (SaleRecord s : inventory.allSales()) {
            revenue += s.getRevenue();
            profit  += s.getProfit();
            if (s.getSoldAt().toLocalDate().equals(LocalDate.now()))
                todaySales += s.getRevenue();
        }
        for (Product p : inventory.allProducts())
            stockVal += p.getPurchasePrice() * p.getQuantity();

        revenueLabel.setText(nf.format(revenue));
        profitLabel.setText(nf.format(profit));
        stockValueLabel.setText(nf.format(stockVal));
        todaySalesLabel.setText(nf.format(todaySales));
    }

    private boolean alertPlayed = false;

    /** Shows a popup (with sound) listing any low-stock products — once per session only. */
    public void checkLowStockAlert() {
        if (alertPlayed) return;
        List<Product> low = inventory.lowStockProducts();
        if (low.isEmpty()) return;

        alertPlayed = true;
        SoundManager.playLowStockAlert();
        StringBuilder sb = new StringBuilder("⚠️  The following products are running low on stock:\n\n");
        for (Product p : low) {
            sb.append(String.format("  •  %-22s  %d unit%s left\n",
                    p.getName(), p.getQuantity(), p.getQuantity() == 1 ? "" : "s"));
        }
        sb.append("\nRestock these soon from the Products tab.");
        JOptionPane.showMessageDialog(this, sb.toString(), "Low Stock Alert", JOptionPane.WARNING_MESSAGE);
    }
}
