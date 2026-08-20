package ui;

import model.*;
import service.SoundManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import java.util.Locale;

public class ReportPanel extends JPanel {
    private final Inventory inventory;
    private final OrderManager orderManager;
    private final DefaultTableModel tableModel;
    private JTable reportTable;
    private JLabel summaryLabel;

    public ReportPanel(Inventory inventory, OrderManager orderManager) {
        this.inventory    = inventory;
        this.orderManager = orderManager;
        setBackground(Theme.BACKGROUND);
        setLayout(new BorderLayout(0, 12));

        add(buildHeader(), BorderLayout.NORTH);

        tableModel  = buildModel();
        reportTable = buildTable();
        JScrollPane scroll = new JScrollPane(reportTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(buildToolbar(), BorderLayout.NORTH);
        center.add(scroll,         BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
        refresh();
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel t = new JLabel("Reports & Analytics");
        t.setFont(Theme.HEADER_FONT);
        t.setForeground(Theme.TEXT_PRIMARY);
        JLabel s = new JLabel("Sales performance and inventory insights");
        s.setFont(Theme.SMALL_FONT);
        s.setForeground(Theme.TEXT_SECONDARY);
        p.add(t, BorderLayout.NORTH);
        p.add(s, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bar.setOpaque(false);
        JButton refreshBtn      = toolBtn("🔄  Refresh",       Theme.PRIMARY);
        JButton topProductsBtn  = toolBtn("🏆  Top Products",  Theme.SUCCESS);
        JButton stockBtn        = toolBtn("📊  Stock Analysis", Theme.INFO);
        JButton orderSummaryBtn = toolBtn("🚚  Order Summary",  Theme.GRADIENT_PURPLE);
        bar.add(refreshBtn); bar.add(topProductsBtn); bar.add(stockBtn); bar.add(orderSummaryBtn);
        refreshBtn.addActionListener(e      -> refresh());
        topProductsBtn.addActionListener(e  -> showTopProducts());
        stockBtn.addActionListener(e        -> showStockAnalysis());
        orderSummaryBtn.addActionListener(e -> showOrderSummary());
        return bar;
    }

    private JButton toolBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(Theme.BODY_FONT);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setBorder(new EmptyBorder(8, 18, 8, 18));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setOpaque(true);
        return b;
    }

    private DefaultTableModel buildModel() {
        return new DefaultTableModel(
                new String[]{"Product","Total Sold","Revenue","Profit","Avg Price"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    private JTable buildTable() {
        JTable t = new JTable(tableModel);
        t.setRowHeight(36);
        t.setFont(Theme.BODY_FONT);
        t.setBackground(Color.WHITE);
        t.setGridColor(new Color(240, 243, 248));
        t.setSelectionBackground(new Color(37, 99, 235, 45));
        t.setSelectionForeground(Theme.TEXT_PRIMARY);
        t.setShowVerticalLines(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setAutoCreateRowSorter(true);
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        t.getTableHeader().setBackground(new Color(248, 250, 252));
        t.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(200, 210, 230)));
        t.getTableHeader().setPreferredSize(new Dimension(0, 38));
        return t;
    }

    private JPanel buildFooter() {
        JPanel f = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 6));
        f.setOpaque(false);
        summaryLabel = new JLabel("—");
        summaryLabel.setFont(Theme.TITLE_FONT);
        summaryLabel.setForeground(Theme.TEXT_PRIMARY);
        f.add(summaryLabel);
        return f;
    }

    // ─── Refresh ─────────────────────────────────────────────────────────────

    public void refresh() {
        tableModel.setRowCount(0);
        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.of("en", "IN"));
        Map<String, Stat> stats = new LinkedHashMap<>();

        for (SaleRecord s : inventory.allSales()) {
            Stat st = stats.computeIfAbsent(s.getProductId(), k -> new Stat(s.getProductName()));
            st.add(s);
        }

        // Sort: product with the most recent sale at the top
        List<Stat> sorted = new java.util.ArrayList<>(stats.values());
        sorted.sort((a, b) -> b.lastSaleTime.compareTo(a.lastSaleTime));

        double totalRev = 0, totalProfit = 0;
        for (Stat st : sorted) {
            tableModel.addRow(new Object[]{
                st.name, st.qty,
                nf.format(st.revenue),
                nf.format(st.profit),
                nf.format(st.qty > 0 ? st.revenue / st.qty : 0)
            });
            totalRev    += st.revenue;
            totalProfit += st.profit;
        }
        summaryLabel.setText(String.format(
            "%d product(s) sold  |  Revenue: %s  |  Profit: %s",
            stats.size(), nf.format(totalRev), nf.format(totalProfit)));
    }

    // ─── Dialogs ─────────────────────────────────────────────────────────────

    private void showTopProducts() {
        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.of("en", "IN"));
        Map<String, Stat> map = new LinkedHashMap<>();
        for (SaleRecord s : inventory.allSales()) {
            map.computeIfAbsent(s.getProductId(), k -> new Stat(s.getProductName())).add(s);
        }
        List<Stat> list = new ArrayList<>(map.values());
        list.sort((a, b) -> Integer.compare(b.qty, a.qty));

        StringBuilder sb = new StringBuilder("🏆  Top Selling Products\n\n");
        int rank = 1;
        for (Stat st : list.subList(0, Math.min(5, list.size()))) {
            sb.append(String.format("%d.  %s\n    Sold: %d units  |  Revenue: %s  |  Profit: %s\n\n",
                rank++, st.name, st.qty, nf.format(st.revenue), nf.format(st.profit)));
        }
        if (list.isEmpty()) sb.append("No sales recorded yet.");
        JOptionPane.showMessageDialog(this, sb.toString(), "Top Products", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showStockAnalysis() {
        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.of("en", "IN"));
        StringBuilder sb = new StringBuilder("📊  Stock Analysis\n\n");

        sb.append("⚠️  LOW STOCK (< 10 units):\n");
        List<Product> low = inventory.lowStockProducts();
        if (low.isEmpty()) sb.append("   None — all products well stocked.\n");
        else for (Product p : low)
            sb.append(String.format("   %-20s  %d units\n", p.getName(), p.getQuantity()));

        double val = 0;
        for (Product p : inventory.allProducts()) val += p.getPurchasePrice() * p.getQuantity();
        sb.append(String.format("\n📦  Total Products : %d\n", inventory.allProducts().size()));
        sb.append(String.format("💎  Total Stock Value: %s\n", nf.format(val)));

        JOptionPane.showMessageDialog(this, sb.toString(), "Stock Analysis", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showOrderSummary() {
        long pending   = orderManager.allOrders().stream().filter(o -> o.getStatus() == OrderStatus.PENDING).count();
        long delivered = orderManager.allOrders().stream().filter(o -> o.getStatus() == OrderStatus.DELIVERED).count();
        long cancelled = orderManager.allOrders().stream().filter(o -> o.getStatus() == OrderStatus.CANCELLED).count();
        long overdue   = orderManager.allOrders().stream().filter(CustomerOrder::isOverdue).count();

        String msg = String.format(
            "🚚  Order Summary\n\n" +
            "  Total Orders  : %d\n" +
            "  ✅ Delivered  : %d\n" +
            "  🕐 Pending    : %d\n" +
            "  ❌ Cancelled  : %d\n" +
            "  ⚠️  Overdue   : %d\n",
            orderManager.allOrders().size(), delivered, pending, cancelled, overdue);
        JOptionPane.showMessageDialog(this, msg, "Order Summary", JOptionPane.INFORMATION_MESSAGE);
    }

    // ─── Inner stat accumulator ───────────────────────────────────────────────

    private static class Stat {
        String name; int qty; double revenue, profit;
        java.time.LocalDateTime lastSaleTime = java.time.LocalDateTime.MIN;
        Stat(String name) { this.name = name; }
        void add(SaleRecord s) {
            qty      += s.getQuantity();
            revenue  += s.getRevenue();
            profit   += s.getProfit();
            if (s.getSoldAt().isAfter(lastSaleTime)) lastSaleTime = s.getSoldAt();
        }
    }
}
