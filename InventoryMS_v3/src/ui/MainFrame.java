package ui;

import model.*;
import service.FileStorage;
import service.SoundManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MainFrame extends JFrame {
    private final Inventory    inventory    = new Inventory();
    private final OrderManager orderManager = new OrderManager();
    private final FileStorage  storage;
    private final String       role;

    private Sidebar      sidebar;
    private StatusBar    statusBar;
    private JPanel       contentPanel;

    private DashboardPanel dashboardPanel;
    private ProductPanel   productPanel;
    private SalesPanel     salesPanel;
    private OrderPanel     orderPanel;
    private ReportPanel    reportPanel;

    private Timer clockTimer;

    public MainFrame(String role) {
        this.role    = role;
        this.storage = new FileStorage("data");
        initData();
        buildUI();
        startClock();
        wireShortcuts();
        navigateTo("Dashboard");
        sidebar.setActive("Dashboard");
    }

    // ─── Data ────────────────────────────────────────────────────────────────

    private void initData() {
        storage.ensureFiles();
        inventory.loadProducts(storage.loadProducts());
        inventory.loadSales(storage.loadSales());
        orderManager.loadOrders(storage.loadOrders());
        if (inventory.allProducts().isEmpty()) seedDemo();
    }

    private void seedDemo() {
        try {
            inventory.addProduct(new Product("P101", "Wireless Keyboard",  "Electronics", 25,  450, 650));
            inventory.addProduct(new Product("P102", "Gaming Mouse",       "Electronics", 40,  250, 380));
            inventory.addProduct(new Product("P103", "Notebook A4",        "Stationery",   8,   35,  60));
            inventory.addProduct(new Product("P104", "USB-C Cable",        "Electronics", 15,   80, 150));
            inventory.addProduct(new Product("P105", "Desk Lamp",          "Furniture",   12,  120, 200));
            inventory.addProduct(new Product("P106", "Wireless Charger",   "Electronics",  7,  150, 280));
            inventory.addProduct(new Product("P107", "Mechanical Keyboard","Electronics",  5,  900,1299));
            inventory.addProduct(new Product("P108", "Monitor Stand",      "Furniture",   20,  350, 550));
            inventory.sellProduct("P101", 3);
            inventory.sellProduct("P102", 5);
            inventory.sellProduct("P103", 2);
        } catch (Exception ignored) {}
    }

    // ─── UI ──────────────────────────────────────────────────────────────────

    private void buildUI() {
        setTitle("Inventory Management System");
        setSize(1280, 760);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1024, 680));

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BACKGROUND);

        // Sidebar
        sidebar = new Sidebar(role);
        sidebar.setPreferredSize(new Dimension(230, getHeight()));
        sidebar.addSidebarListener(this::navigateTo);
        sidebar.addLogoutListener(this::logout);
        root.add(sidebar, BorderLayout.WEST);

        // Content (CardLayout)
        contentPanel = new JPanel(new CardLayout());
        contentPanel.setBackground(Theme.BACKGROUND);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        dashboardPanel = new DashboardPanel(inventory, orderManager, role);
        productPanel   = new ProductPanel(inventory, role);
        salesPanel     = new SalesPanel(inventory, role);
        orderPanel     = new OrderPanel(inventory, orderManager, role);
        reportPanel    = new ReportPanel(inventory, orderManager);

        contentPanel.add(dashboardPanel, "Dashboard");
        contentPanel.add(productPanel,   "Products");
        contentPanel.add(salesPanel,     "Sales");
        contentPanel.add(orderPanel,     "Orders");
        if (role.equals("admin")) contentPanel.add(reportPanel, "Reports");
        root.add(contentPanel, BorderLayout.CENTER);

        // Status bar
        statusBar = new StatusBar(role);
        statusBar.addSaveListener(this::saveData);
        root.add(statusBar, BorderLayout.SOUTH);

        setContentPane(root);

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { saveData(); }
        });
    }

    // ─── Navigation ──────────────────────────────────────────────────────────

    private void navigateTo(String page) {
        CardLayout cl = (CardLayout) contentPanel.getLayout();
        cl.show(contentPanel, page);
        statusBar.setMessage("Viewing: " + page);

        // Refresh stale data when switching tabs
        switch (page) {
            case "Dashboard": dashboardPanel.refresh(); dashboardPanel.checkLowStockAlert(); break;
            case "Products":  productPanel.refreshTable();   break;
            case "Sales":     salesPanel.refreshTable();     break;
            case "Orders":    orderPanel.refreshTable();     break;
            case "Reports":   reportPanel.refresh();         break;
        }
    }

    // ─── Clock ───────────────────────────────────────────────────────────────

    private void startClock() {
        clockTimer = new Timer(1000, e ->
            statusBar.setTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss a"))));
        clockTimer.start();
    }

    // ─── Shortcuts ───────────────────────────────────────────────────────────

    private void wireShortcuts() {
        // Ctrl+S → save
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("ctrl S"), "save");
        getRootPane().getActionMap().put("save", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { saveData(); }
        });
    }

    // ─── Logout ──────────────────────────────────────────────────────────────

    private void logout() {
        saveData();
        if (clockTimer != null) clockTimer.stop();
        dispose();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }

    // ─── Save ────────────────────────────────────────────────────────────────

    private void saveData() {
        try {
            storage.saveAll(inventory, orderManager);
            SoundManager.playSave();
            statusBar.setMessage("Saved at " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error saving data: " + ex.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
