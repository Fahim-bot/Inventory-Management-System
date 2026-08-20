package ui;

import model.*;
import service.SoundManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class OrderPanel extends JPanel {
    private final Inventory inventory;
    private final OrderManager orderManager;
    private final String role;
    private final DefaultTableModel tableModel;
    private JTable orderTable;
    private JTextField searchField;
    private JLabel statusLabel;

    public OrderPanel(Inventory inventory, OrderManager orderManager, String role) {
        this.inventory    = inventory;
        this.orderManager = orderManager;
        this.role         = role;
        setBackground(Theme.BACKGROUND);
        setLayout(new BorderLayout(0, 12));

        add(buildHeader(), BorderLayout.NORTH);

        tableModel = buildModel();
        orderTable = buildTable();
        JScrollPane scroll = new JScrollPane(orderTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(buildToolbar(), BorderLayout.NORTH);
        center.add(scroll, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        statusLabel = new JLabel("Total Orders: 0");
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
        JLabel t = new JLabel("Order Management");
        t.setFont(Theme.HEADER_FONT);
        t.setForeground(Theme.TEXT_PRIMARY);
        JLabel s = new JLabel("Manage customer orders and deliveries");
        s.setFont(Theme.SMALL_FONT);
        s.setForeground(Theme.TEXT_SECONDARY);
        p.add(t, BorderLayout.NORTH);
        p.add(s, BorderLayout.SOUTH);
        return p;
    }

    // ─── Toolbar ─────────────────────────────────────────────────────────────

    private JPanel buildToolbar() {
        JPanel bar = new JPanel();
        bar.setLayout(new BoxLayout(bar, BoxLayout.Y_AXIS));
        bar.setOpaque(false);

        JPanel searchRow = new JPanel(new BorderLayout());
        searchRow.setOpaque(false);
        searchField = new PlaceholderTextField(22, "🔍  Search orders…");
        searchField.setFont(Theme.BODY_FONT);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 210, 230), 1),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)));
        searchRow.add(searchField, BorderLayout.WEST);
        bar.add(searchRow);
        bar.add(Box.createVerticalStrut(8));

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        btns.setOpaque(false);
        JButton newBtn      = toolBtn("➕  New Order",  Theme.PRIMARY);
        JButton editBtn     = toolBtn("✏️  Edit",       Theme.INFO);
        JButton deliverBtn  = toolBtn("✅  Deliver",    Theme.SUCCESS);
        JButton cancelBtn   = toolBtn("❌  Cancel",     Theme.DANGER);
        JButton pendingBtn  = toolBtn("↩️  Reset Pending", Theme.WARNING);
        JButton assignBtn   = toolBtn("👤  Assign",     Theme.INFO);
        JButton deleteBtn   = toolBtn("🗑️  Delete",     Theme.DANGER);
        JButton refreshBtn  = toolBtn("🔄  Refresh",    Theme.TEXT_SECONDARY);
        if (!role.equals("admin")) {
            deleteBtn.setEnabled(false);
            deleteBtn.setBackground(new Color(200, 200, 200));
        }
        btns.add(newBtn); btns.add(editBtn); btns.add(deliverBtn); btns.add(cancelBtn);
        btns.add(pendingBtn); btns.add(assignBtn); btns.add(deleteBtn); btns.add(refreshBtn);
        bar.add(btns);

        newBtn.addActionListener(e     -> showNewOrderDialog());
        editBtn.addActionListener(e    -> showEditOrderDialog());
        deliverBtn.addActionListener(e -> markStatus(OrderStatus.DELIVERED));
        cancelBtn.addActionListener(e  -> markStatus(OrderStatus.CANCELLED));
        pendingBtn.addActionListener(e -> markStatus(OrderStatus.PENDING));
        assignBtn.addActionListener(e  -> showAssignDialog());
        deleteBtn.addActionListener(e  -> deleteSelected());
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

    private DefaultTableModel buildModel() {
        return new DefaultTableModel(
                new String[]{"Order ID","Customer","Address","Product","Qty","Deadline","Status","Assigned To"}, 0) {
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
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        t.getTableHeader().setBackground(new Color(248, 250, 252));
        t.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(200, 210, 230)));
        t.getTableHeader().setPreferredSize(new Dimension(0, 38));
        t.getColumnModel().getColumn(6).setCellRenderer(new StatusRenderer());
        return t;
    }

    // ─── Live search ─────────────────────────────────────────────────────────

    private void wireSearch() {
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { filter(); }
            @Override public void removeUpdate(DocumentEvent e)  { filter(); }
            @Override public void changedUpdate(DocumentEvent e) { filter(); }
        });
    }

    private void filter() {
        String text = searchField.getText().trim();
        TableRowSorter<DefaultTableModel> s = new TableRowSorter<>(tableModel);
        orderTable.setRowSorter(s);
        s.setRowFilter(text.isEmpty() ? null : RowFilter.regexFilter("(?i)" + text));
    }

    // ─── Data refresh ────────────────────────────────────────────────────────

    public void refreshTable() {
        tableModel.setRowCount(0);
        for (CustomerOrder o : orderManager.allOrders()) {
            tableModel.addRow(new Object[]{
                o.getOrderId(),
                o.getCustomer().getName(),
                o.getCustomer().getAddress(),
                o.getProductName(),
                o.getQuantity(),
                o.getDeadline().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                o.getStatus().name(),
                o.getAssignedTo().isEmpty() ? "Unassigned" : o.getAssignedTo()
            });
        }
        statusLabel.setText("Total Orders: " + orderManager.allOrders().size());
    }

    // ─── Dialogs ─────────────────────────────────────────────────────────────

    private String selectedOrderId() {
        int row = orderTable.getSelectedRow();
        if (row == -1) return null;
        return tableModel.getValueAt(orderTable.convertRowIndexToModel(row), 0).toString();
    }

    private void showNewOrderDialog() {
        if (inventory.allProducts().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No products in the catalogue yet. Add a product first.",
                    "No Products", JOptionPane.WARNING_MESSAGE);
            return;
        }
        OrderFormFields f = orderForm(null);
        CustomDialog dlg = new CustomDialog(SwingUtilities.getWindowAncestor(this),
                "New Order", "Create a new customer order");
        dlg.setContent(f.panel);
        if (dlg.showDialog() == CustomDialog.OK_OPTION) {
            try {
                Product prod = (Product) f.productCombo.getSelectedItem();
                if (prod == null) throw new IllegalArgumentException("Please select a product.");
                int qty      = Integer.parseInt(f.rest[0].getText().trim());
                String name  = f.rest[1].getText().trim();
                String phone = f.rest[2].getText().trim();
                String addr  = f.rest[3].getText().trim();
                LocalDate dl = LocalDate.parse(f.rest[4].getText().trim());
                String assigned = f.rest.length > 5 ? f.rest[5].getText().trim() : "";

                Customer cust = new Customer("C" + System.currentTimeMillis(), name, phone, addr);
                CustomerOrder order = new CustomerOrder(
                        "O" + orderManager.nextOrderNumber(),
                        cust, prod.getId(), prod.getName(), qty, dl, OrderStatus.PENDING, assigned);
                orderManager.addOrder(order);
                SoundManager.playOrderCreated();
                refreshTable();
                JOptionPane.showMessageDialog(this, "Order created successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                SoundManager.playError();
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showEditOrderDialog() {
        String id = selectedOrderId();
        if (id == null) { JOptionPane.showMessageDialog(this, "Please select an order first.",
                "No Selection", JOptionPane.WARNING_MESSAGE); return; }
        CustomerOrder o = orderManager.findOrder(id);
        if (o == null) { JOptionPane.showMessageDialog(this, "Order not found: " + id,
                "Error", JOptionPane.ERROR_MESSAGE); return; }

        OrderFormFields f = orderForm(o);
        CustomDialog dlg = new CustomDialog(SwingUtilities.getWindowAncestor(this),
                "Edit Order", "Update order " + o.getOrderId());
        dlg.setContent(f.panel);
        if (dlg.showDialog() == CustomDialog.OK_OPTION) {
            try {
                Product prod = (Product) f.productCombo.getSelectedItem();
                if (prod == null) throw new IllegalArgumentException("Please select a product.");
                int qty      = Integer.parseInt(f.rest[0].getText().trim());
                String name  = f.rest[1].getText().trim();
                String phone = f.rest[2].getText().trim();
                String addr  = f.rest[3].getText().trim();
                LocalDate dl = LocalDate.parse(f.rest[4].getText().trim());
                String assigned = f.rest.length > 5 ? f.rest[5].getText().trim() : o.getAssignedTo();

                Customer cust = new Customer(o.getCustomer().getId(), name, phone, addr);
                CustomerOrder updated = new CustomerOrder(o.getOrderId(), cust, prod.getId(), prod.getName(),
                        qty, dl, o.getStatus(), assigned);
                orderManager.replaceOrder(o.getOrderId(), updated);
                SoundManager.playSave();
                refreshTable();
                JOptionPane.showMessageDialog(this, "Order " + o.getOrderId() + " updated.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                SoundManager.playError();
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /** Holds the components built by orderForm(): the product dropdown plus
     *  the remaining plain text fields (quantity, name, phone, address,
     *  deadline, and — for admins — assigned employee). */
    private static class OrderFormFields {
        JPanel panel;
        JComboBox<Product> productCombo;
        JTextField[] rest;
    }

    /** Builds the New/Edit Order form. Pass null for a blank New Order form,
     *  or an existing CustomerOrder to pre-fill values for editing.
     *  The product is chosen from a dropdown of real catalogue products
     *  (showing name, stock, and price) instead of a free-typed ID, so it's
     *  always clear which products are actually available to order. */
    private OrderFormFields orderForm(CustomerOrder existing) {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(8, 16, 8, 16));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1; g.gridx = 0;

        OrderFormFields result = new OrderFormFields();
        result.panel = form;

        // ── Row 0: Product dropdown ─────────────────────────────────────
        g.gridy = 0; g.insets = new Insets(5, 0, 0, 0);
        JLabel prodLbl = new JLabel("Product");
        prodLbl.setFont(Theme.BODY_FONT); prodLbl.setForeground(Theme.TEXT_PRIMARY);
        form.add(prodLbl, g);

        g.gridy = 1; g.insets = new Insets(4, 0, 0, 0);
        java.util.List<Product> products = new java.util.ArrayList<>(inventory.allProducts());
        JComboBox<Product> productCombo = new JComboBox<>(products.toArray(new Product[0]));
        productCombo.setFont(Theme.BODY_FONT);
        productCombo.setPreferredSize(new Dimension(400, 44));
        productCombo.setMinimumSize(new Dimension(200, 44));
        // Allow popup to render outside the dialog window boundary
        productCombo.setLightWeightPopupEnabled(false);
        productCombo.setMaximumRowCount(6);
        productCombo.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean sel, boolean focus) {
                super.getListCellRendererComponent(list, value, index, sel, focus);
                if (value instanceof Product) {
                    Product p = (Product) value;
                    setText(String.format("%s  —  %s   (%d in stock, ₹%.2f)",
                            p.getId(), p.getName(), p.getQuantity(), p.getSellingPrice()));
                }
                return this;
            }
        });
        if (existing != null) {
            for (Product p : products) {
                if (p.getId().equals(existing.getProductId())) { productCombo.setSelectedItem(p); break; }
            }
        }
        form.add(productCombo, g);
        result.productCombo = productCombo;

        // ── Remaining plain text fields ──────────────────────────────────
        String[][] defs = {
            {"Quantity",       existing != null ? String.valueOf(existing.getQuantity()) : "1"},
            {"Customer Name",  existing != null ? existing.getCustomer().getName()    : ""},
            {"Customer Phone", existing != null ? existing.getCustomer().getPhone()   : ""},
            {"Customer Address", existing != null ? existing.getCustomer().getAddress() : ""},
            {"Deadline (YYYY-MM-DD)", existing != null ? existing.getDeadline().toString()
                                                        : LocalDate.now().plusDays(3).toString()}
        };
        int extra = role.equals("admin") ? 1 : 0;
        JTextField[] fields = new JTextField[defs.length + extra];
        for (int i = 0; i < defs.length; i++) {
            int row = i + 1; // offset by the product dropdown row
            g.gridy = row * 2;   g.insets = new Insets(5, 0, 0, 0);
            JLabel lbl = new JLabel(defs[i][0]);
            lbl.setFont(Theme.BODY_FONT); lbl.setForeground(Theme.TEXT_PRIMARY);
            form.add(lbl, g);
            g.gridy = row * 2 + 1; g.insets = new Insets(4, 0, 0, 0);
            JTextField tf = new JTextField(defs[i][1]);
            tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            tf.setPreferredSize(new Dimension(400, 44));
            tf.setMinimumSize(new Dimension(200, 44));
            tf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 210, 230), 1),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)));
            form.add(tf, g);
            fields[i] = tf;
        }
        if (role.equals("admin")) {
            int row = defs.length + 1;
            g.gridy = row * 2;   g.insets = new Insets(5, 0, 0, 0);
            JLabel lbl = new JLabel("Assign To Employee");
            lbl.setFont(Theme.BODY_FONT); lbl.setForeground(Theme.TEXT_PRIMARY);
            form.add(lbl, g);
            g.gridy = row * 2 + 1; g.insets = new Insets(2, 0, 0, 0);
            JTextField tf = new JTextField(existing != null ? existing.getAssignedTo() : "");
            tf.setFont(Theme.BODY_FONT);
            tf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 210, 230), 1),
                    BorderFactory.createEmptyBorder(7, 9, 7, 9)));
            form.add(tf, g);
            fields[defs.length] = tf;
        }
        result.rest = fields;
        return result;
    }

    private void deleteSelected() {
        if (!role.equals("admin")) { JOptionPane.showMessageDialog(this,
                "Only administrators can delete orders.", "Access Denied", JOptionPane.WARNING_MESSAGE); return; }
        String id = selectedOrderId();
        if (id == null) { JOptionPane.showMessageDialog(this, "Please select an order first.",
                "No Selection", JOptionPane.WARNING_MESSAGE); return; }
        int c = JOptionPane.showConfirmDialog(this,
                "Delete order \"" + id + "\"? This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (c == JOptionPane.YES_OPTION) {
            orderManager.deleteOrder(id);
            SoundManager.playDelete();
            refreshTable();
            JOptionPane.showMessageDialog(this, "Order deleted.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void markStatus(OrderStatus status) {
        String id = selectedOrderId();
        if (id == null) { JOptionPane.showMessageDialog(this, "Please select an order first.",
                "No Selection", JOptionPane.WARNING_MESSAGE); return; }
        CustomerOrder o = orderManager.findOrder(id);
        if (o == null) return;
        o.setStatus(status);
        if (status == OrderStatus.DELIVERED)      SoundManager.playOrderDelivered();
        else if (status == OrderStatus.CANCELLED) SoundManager.playOrderCancelled();
        else if (status == OrderStatus.PENDING)   SoundManager.playOrderCreated();
        refreshTable();
        JOptionPane.showMessageDialog(this, "Order " + id + " marked as " + status.name() + ".",
                "Status Updated", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showAssignDialog() {
        if (!role.equals("admin")) { JOptionPane.showMessageDialog(this,
                "Only admins can assign orders.", "Access Denied", JOptionPane.WARNING_MESSAGE); return; }
        String id = selectedOrderId();
        if (id == null) { JOptionPane.showMessageDialog(this, "Please select an order first.",
                "No Selection", JOptionPane.WARNING_MESSAGE); return; }
        CustomerOrder o = orderManager.findOrder(id);
        if (o == null) return;
        String emp = JOptionPane.showInputDialog(this, "Employee name to assign:",
                o.getAssignedTo().isEmpty() ? "" : o.getAssignedTo());
        if (emp != null) {
            o.setAssignedTo(emp.trim());
            SoundManager.playSave();
            refreshTable();
        }
    }

    // ─── Status renderer ─────────────────────────────────────────────────────

    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            super.getTableCellRendererComponent(t, v, sel, foc, row, col);
            String s = v != null ? v.toString() : "";
            switch (s) {
                case "PENDING":   setForeground(Theme.WARNING); break;
                case "DELIVERED": setForeground(Theme.SUCCESS); break;
                case "CANCELLED": setForeground(Theme.DANGER);  break;
                default:          setForeground(Theme.TEXT_PRIMARY);
            }
            setFont(getFont().deriveFont(Font.BOLD));
            return this;
        }
    }
}
