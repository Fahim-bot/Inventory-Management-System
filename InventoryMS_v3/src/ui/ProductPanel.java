package ui;

import model.Inventory;
import model.Product;
import service.SoundManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ProductPanel extends JPanel {
    private final Inventory inventory;
    private final String role;
    private final DefaultTableModel tableModel;
    private JTable productTable;
    private JTextField searchField;
    private JLabel statusLabel;

    public ProductPanel(Inventory inventory, String role) {
        this.inventory = inventory;
        this.role      = role;
        setBackground(Theme.BACKGROUND);
        setLayout(new BorderLayout(0, 12));

        add(buildHeader(), BorderLayout.NORTH);

        tableModel   = buildTableModel();
        productTable = buildTable();
        JScrollPane scroll = new JScrollPane(productTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);

        // Wrap toolbar + table in a CENTER sub-panel
        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(buildToolbar(), BorderLayout.NORTH);
        center.add(scroll, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        statusLabel = new JLabel("Total Products: 0");
        statusLabel.setFont(Theme.SMALL_FONT);
        statusLabel.setForeground(Theme.TEXT_SECONDARY);
        statusLabel.setBorder(new EmptyBorder(6, 0, 0, 0));
        add(statusLabel, BorderLayout.SOUTH);

        refreshTable();
        wireSearch();
    }

    // ─── Header ──────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel title = new JLabel("Product Management");
        title.setFont(Theme.HEADER_FONT);
        title.setForeground(Theme.TEXT_PRIMARY);
        JLabel sub = new JLabel("Manage your product catalogue");
        sub.setFont(Theme.SMALL_FONT);
        sub.setForeground(Theme.TEXT_SECONDARY);
        p.add(title, BorderLayout.NORTH);
        p.add(sub,   BorderLayout.SOUTH);
        return p;
    }

    // ─── Toolbar ─────────────────────────────────────────────────────────────

    private JPanel buildToolbar() {
        JPanel bar = new JPanel();
        bar.setLayout(new BoxLayout(bar, BoxLayout.Y_AXIS));
        bar.setOpaque(false);

        JPanel searchRow = new JPanel(new BorderLayout());
        searchRow.setOpaque(false);
        searchField = new PlaceholderTextField(22, "🔍  Search by ID, name or category…");
        searchField.setFont(Theme.BODY_FONT);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 210, 230), 1),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)));
        searchRow.add(searchField, BorderLayout.WEST);
        bar.add(searchRow);
        bar.add(Box.createVerticalStrut(8));

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        btns.setOpaque(false);
        JButton addBtn     = toolBtn("➕  Add",     Theme.PRIMARY);
        JButton editBtn    = toolBtn("✏️  Edit",     Theme.INFO);
        JButton deleteBtn  = toolBtn("🗑️  Delete",  Theme.DANGER);
        JButton restockBtn = toolBtn("📦  Restock", Theme.SUCCESS);
        JButton refreshBtn = toolBtn("🔄  Refresh", Theme.TEXT_SECONDARY);

        if (!role.equals("admin")) {
            deleteBtn.setEnabled(false);
            deleteBtn.setBackground(new Color(200, 200, 200));
        }

        btns.add(addBtn); btns.add(editBtn); btns.add(deleteBtn);
        btns.add(restockBtn); btns.add(refreshBtn);
        bar.add(btns);

        addBtn.addActionListener(e     -> showAddDialog());
        editBtn.addActionListener(e    -> showEditDialog());
        deleteBtn.addActionListener(e  -> deleteSelected());
        restockBtn.addActionListener(e -> showRestockDialog());
        refreshBtn.addActionListener(e -> refreshTable());

        return bar;
    }

    private JButton toolBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(Theme.BODY_FONT);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setBorder(new EmptyBorder(7, 14, 7, 14));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setOpaque(true);
        return b;
    }

    // ─── Table ───────────────────────────────────────────────────────────────

    private DefaultTableModel buildTableModel() {
        return new DefaultTableModel(
                new String[]{"ID","Name","Category","Qty","Buy Price","Sell Price","Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    private JTable buildTable() {
        JTable t = new JTable(tableModel);
        t.setRowHeight(38);
        t.setFont(Theme.BODY_FONT);
        t.setBackground(Color.WHITE);
        t.setGridColor(new Color(240, 243, 248));
        t.setSelectionBackground(new Color(37, 99, 235, 45));
        t.setSelectionForeground(Theme.TEXT_PRIMARY);
        t.setShowVerticalLines(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setAutoCreateRowSorter(true);
        styleHeader(t);
        t.getColumnModel().getColumn(6).setCellRenderer(new StatusRenderer());
        t.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) showEditDialog();
            }
        });
        return t;
    }

    private void styleHeader(JTable t) {
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        t.getTableHeader().setForeground(Theme.TEXT_PRIMARY);
        t.getTableHeader().setBackground(new Color(248, 250, 252));
        t.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(200, 210, 230)));
        t.getTableHeader().setPreferredSize(new Dimension(0, 38));
    }

    // ─── Live search ─────────────────────────────────────────────────────────

    private void wireSearch() {
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { filterTable(); }
            @Override public void removeUpdate(DocumentEvent e)  { filterTable(); }
            @Override public void changedUpdate(DocumentEvent e) { filterTable(); }
        });
    }

    private void filterTable() {
        String text = searchField.getText().trim();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        productTable.setRowSorter(sorter);
        sorter.setRowFilter(text.isEmpty() ? null : RowFilter.regexFilter("(?i)" + text));
    }

    // ─── Data refresh ────────────────────────────────────────────────────────

    public void refreshTable() {
        tableModel.setRowCount(0);
        for (Product p : inventory.allProducts()) {
            tableModel.addRow(new Object[]{
                p.getId(), p.getName(), p.getCategory(), p.getQuantity(),
                String.format("₹%.2f", p.getPurchasePrice()),
                String.format("₹%.2f", p.getSellingPrice()),
                p.isLowStock() ? "⚠️ Low Stock" : "✅ In Stock"
            });
        }
        statusLabel.setText("Total Products: " + inventory.allProducts().size());
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private String selectedId() {
        int row = productTable.getSelectedRow();
        if (row == -1) return null;
        return tableModel.getValueAt(productTable.convertRowIndexToModel(row), 0).toString();
    }

    private JTextField[] buildForm(JPanel form, Product existing) {
        form.setBackground(Color.WHITE);
        form.setLayout(new GridBagLayout());
        form.setBorder(new EmptyBorder(12, 20, 12, 20));
        GridBagConstraints g = new GridBagConstraints();
        g.fill    = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        g.gridx   = 0;

        String[][] defs = {
            {"Product ID",     existing != null ? existing.getId()                          : ""},
            {"Product Name",   existing != null ? existing.getName()                        : ""},
            {"Category",       existing != null ? existing.getCategory()                    : ""},
            {"Quantity",       existing != null ? String.valueOf(existing.getQuantity())     : "0"},
            {"Buy Price (₹)",  existing != null ? String.valueOf(existing.getPurchasePrice()): "0.00"},
            {"Sell Price (₹)", existing != null ? String.valueOf(existing.getSellingPrice()) : "0.00"}
        };

        JTextField[] fields = new JTextField[defs.length];
        for (int i = 0; i < defs.length; i++) {
            // Label
            g.gridy  = i * 2;
            g.insets = new Insets(i == 0 ? 0 : 10, 0, 0, 0);
            JLabel lbl = new JLabel(defs[i][0]);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lbl.setForeground(Theme.TEXT_PRIMARY);
            form.add(lbl, g);

            // Field — minimum 44px tall so text is readable
            g.gridy  = i * 2 + 1;
            g.insets = new Insets(4, 0, 0, 0);
            JTextField tf = new JTextField(defs[i][1]);
            tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            tf.setMinimumSize(new Dimension(200, 44));
            tf.setPreferredSize(new Dimension(400, 44));
            tf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 210, 230), 1),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)));
            if (existing != null && i == 0) {
                tf.setEditable(false);
                tf.setBackground(new Color(245, 247, 250));
                tf.setForeground(Theme.TEXT_SECONDARY);
            }
            form.add(tf, g);
            fields[i] = tf;
        }
        return fields;
    }

    // ─── Dialogs ─────────────────────────────────────────────────────────────

    private void showAddDialog() {
        JPanel form = new JPanel();
        JTextField[] f = buildForm(form, null);
        CustomDialog dlg = new CustomDialog(SwingUtilities.getWindowAncestor(this),
                "Add New Product", "Fill in the product details below");
        dlg.setContent(form);
        if (dlg.showDialog() == CustomDialog.OK_OPTION) {
            try {
                inventory.addProduct(new Product(
                        f[0].getText().trim(),
                        f[1].getText().trim(),
                        f[2].getText().trim(),
                        Integer.parseInt(f[3].getText().trim()),
                        Double.parseDouble(f[4].getText().trim()),
                        Double.parseDouble(f[5].getText().trim())));
                SoundManager.playStockAdded();
                refreshTable();
                info("Product added successfully!");
            } catch (Exception ex) { error("Error: " + ex.getMessage()); SoundManager.playError(); }
        }
    }

    private void showEditDialog() {
        String id = selectedId();
        if (id == null) { warn("Please select a product first."); return; }
        Product p = inventory.searchById(id);
        if (p == null) { error("Product not found."); return; }

        JPanel form = new JPanel();
        JTextField[] f = buildForm(form, p);
        CustomDialog dlg = new CustomDialog(SwingUtilities.getWindowAncestor(this),
                "Edit Product", "Update details for: " + p.getName());
        dlg.setContent(form);
        if (dlg.showDialog() == CustomDialog.OK_OPTION) {
            try {
                p.updateDetails(
                        f[1].getText().trim(),
                        f[2].getText().trim(),
                        Double.parseDouble(f[4].getText().trim()),
                        Double.parseDouble(f[5].getText().trim()));
                SoundManager.playSave();
                refreshTable();
                info("Product updated successfully!");
            } catch (Exception ex) { error("Error: " + ex.getMessage()); SoundManager.playError(); }
        }
    }

    private void deleteSelected() {
        if (!role.equals("admin")) { warn("Only administrators can delete products."); return; }
        String id = selectedId();
        if (id == null) { warn("Please select a product first."); return; }
        int c = JOptionPane.showConfirmDialog(this,
                "Delete product \"" + id + "\"? This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (c == JOptionPane.YES_OPTION) {
            inventory.deleteProduct(id);
            SoundManager.playDelete();
            refreshTable();
            info("Product deleted.");
        }
    }

    private void showRestockDialog() {
        String id = selectedId();
        if (id == null) { warn("Please select a product first."); return; }
        String qty = JOptionPane.showInputDialog(this, "Quantity to add:", "Restock", JOptionPane.QUESTION_MESSAGE);
        if (qty != null && !qty.trim().isEmpty()) {
            try {
                int q = Integer.parseInt(qty.trim());
                if (q <= 0) { error("Quantity must be positive."); return; }
                inventory.purchaseStock(id, q);
                SoundManager.playStockAdded();
                refreshTable();
                info("Stock updated successfully!");
            } catch (NumberFormatException ex) { error("Please enter a valid number."); }
              catch (Exception ex) { error("Error: " + ex.getMessage()); SoundManager.playError(); }
        }
    }

    private void info(String msg)  { JOptionPane.showMessageDialog(this, msg, "Success",    JOptionPane.INFORMATION_MESSAGE); }
    private void warn(String msg)  { JOptionPane.showMessageDialog(this, msg, "Notice",     JOptionPane.WARNING_MESSAGE); }
    private void error(String msg) { JOptionPane.showMessageDialog(this, msg, "Error",      JOptionPane.ERROR_MESSAGE); }

    // ─── Status cell renderer ─────────────────────────────────────────────────

    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            super.getTableCellRendererComponent(t, v, sel, foc, row, col);
            String s = v != null ? v.toString() : "";
            if (s.contains("Low")) { setForeground(Theme.DANGER);   setFont(getFont().deriveFont(Font.BOLD)); }
            else                   { setForeground(Theme.SUCCESS); }
            return this;
        }
    }
}
