package ui;

import model.Inventory;
import model.SaleRecord;
import service.SoundManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;

public class SalesPanel extends JPanel {
    private final Inventory inventory;
    private final DefaultTableModel tableModel;
    private JTable salesTable;
    private JLabel totalRevenueLabel;
    private JLabel totalProfitLabel;
    private JTextField searchField;

    public SalesPanel(Inventory inventory, String role) {
        this.inventory = inventory;
        setBackground(Theme.BACKGROUND);
        setLayout(new BorderLayout(0, 12));

        add(buildHeader(), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(buildSellBar(),  BorderLayout.NORTH);
        center.add(buildSearchBar(), BorderLayout.CENTER);
        tableModel = buildModel();
        salesTable = buildTable();
        JScrollPane scroll = new JScrollPane(salesTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        center.add(scroll, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        add(buildFooter(), BorderLayout.SOUTH);
        refreshTable();
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel t = new JLabel("Sales Management");
        t.setFont(Theme.HEADER_FONT);
        t.setForeground(Theme.TEXT_PRIMARY);
        JLabel s = new JLabel("Record sales and view transaction history");
        s.setFont(Theme.SMALL_FONT);
        s.setForeground(Theme.TEXT_SECONDARY);
        p.add(t, BorderLayout.NORTH);
        p.add(s, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildSellBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(0, 0, 4, 0));

        JTextField pidField = field("Product ID");
        JTextField qtyField = field("Quantity");

        JButton sellBtn = actionBtn("💰  Record Sale", Theme.SUCCESS);
        JButton refBtn  = actionBtn("🔄  Refresh",     Theme.TEXT_SECONDARY);

        bar.add(pidField); bar.add(qtyField); bar.add(sellBtn); bar.add(refBtn);

        sellBtn.addActionListener(e -> {
            try {
                String pid = pidField.getText().trim();
                int qty = Integer.parseInt(qtyField.getText().trim());
                SaleRecord sale = inventory.sellProduct(pid, qty);
                SoundManager.playSaleSuccess();   // this IS the feedback sound — no extra click needed
                refreshTable();
                pidField.setText(""); qtyField.setText("");
                NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.of("en", "IN"));
                JOptionPane.showMessageDialog(this,
                    "Sale recorded!\n\nProduct : " + sale.getProductName() +
                    "\nQuantity: " + sale.getQuantity() +
                    "\nRevenue : " + nf.format(sale.getRevenue()) +
                    "\nProfit  : " + nf.format(sale.getProfit()),
                    "Sale Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                SoundManager.playError();
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Sale Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        refBtn.addActionListener(e -> refreshTable());
        return bar;
    }

    private JPanel buildSearchBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bar.setOpaque(false);
        searchField = new PlaceholderTextField(25, "🔍  Search sale records…");
        searchField.setFont(Theme.BODY_FONT);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 210, 230), 1),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)));
        bar.add(searchField);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { filter(); }
            @Override public void removeUpdate(DocumentEvent e)  { filter(); }
            @Override public void changedUpdate(DocumentEvent e) { filter(); }
        });
        return bar;
    }

    private void filter() {
        String t = searchField.getText().trim();
        TableRowSorter<DefaultTableModel> s = new TableRowSorter<>(tableModel);
        salesTable.setRowSorter(s);
        s.setRowFilter(t.isEmpty() ? null : RowFilter.regexFilter("(?i)" + t));
    }

    private DefaultTableModel buildModel() {
        return new DefaultTableModel(
                new String[]{"Sale ID","Product","Quantity","Revenue","Profit","Date & Time"}, 0) {
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
        JPanel f = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 6));
        f.setOpaque(false);
        totalRevenueLabel = new JLabel("Total Revenue: ₹0.00");
        totalRevenueLabel.setFont(Theme.TITLE_FONT);
        totalRevenueLabel.setForeground(Theme.SUCCESS);
        totalProfitLabel = new JLabel("Total Profit: ₹0.00");
        totalProfitLabel.setFont(Theme.TITLE_FONT);
        totalProfitLabel.setForeground(Theme.PRIMARY);
        f.add(totalRevenueLabel);
        f.add(totalProfitLabel);
        return f;
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        double rev = 0, prof = 0;
        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.of("en", "IN"));

        // Sort newest sale first
        java.util.List<SaleRecord> sorted = new java.util.ArrayList<>(inventory.allSales());
        sorted.sort((a, b) -> b.getSoldAt().compareTo(a.getSoldAt()));

        for (SaleRecord s : sorted) {
            tableModel.addRow(new Object[]{
                s.getSaleId(), s.getProductName(), s.getQuantity(),
                nf.format(s.getRevenue()), nf.format(s.getProfit()),
                s.getSoldAt().format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))
            });
            rev  += s.getRevenue();
            prof += s.getProfit();
        }
        totalRevenueLabel.setText("Total Revenue: " + nf.format(rev));
        totalProfitLabel.setText("Total Profit: "   + nf.format(prof));
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private JTextField field(String hint) {
        JTextField tf = new PlaceholderTextField(12, hint);
        tf.setFont(Theme.BODY_FONT);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 210, 230), 1),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)));
        return tf;
    }

    private JButton actionBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(Theme.BODY_FONT);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setBorder(new EmptyBorder(7, 16, 7, 16));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setOpaque(true);
        return b;
    }
}
