package ui;

import service.SoundManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class LoginFrame extends JFrame {
    private JPasswordField passwordField;
    private JButton loginBtn;
    private JLabel errorLabel;
    private boolean adminMode = true;
    private int failCount = 0;
    private JButton adminBtn, employeeBtn;

    public LoginFrame() {
        setTitle("Inventory Management System — Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setUndecorated(true);
        setSize(460, 560);
        setLocationRelativeTo(null);
        setShape(new RoundRectangle2D.Double(0, 0, 460, 560, 20, 20));

        JPanel main = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, new Color(15, 23, 42), 0, getHeight(), new Color(30, 58, 138)));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        main.setOpaque(false);
        main.add(buildTop(),    BorderLayout.NORTH);
        main.add(buildForm(),   BorderLayout.CENTER);
        main.add(buildBottom(), BorderLayout.SOUTH);
        add(main);
        enableDrag(main);
    }

    // ─── Panels ──────────────────────────────────────────────────────────────

    private JPanel buildTop() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(28, 28, 16, 28));

        JLabel icon = new JLabel("📦");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 44));
        p.add(icon, BorderLayout.WEST);

        JPanel titles = new JPanel(new GridLayout(2, 1));
        titles.setOpaque(false);
        titles.setBorder(new EmptyBorder(4, 14, 0, 0));
        JLabel t1 = new JLabel("Inventory Management");
        t1.setFont(new Font("Segoe UI", Font.BOLD, 19));
        t1.setForeground(Color.WHITE);
        JLabel t2 = new JLabel("System");
        t2.setFont(new Font("Segoe UI", Font.BOLD, 19));
        t2.setForeground(new Color(96, 165, 250));
        titles.add(t1); titles.add(t2);
        p.add(titles, BorderLayout.CENTER);

        JButton close = new JButton("✕");
        close.setFont(new Font("Segoe UI", Font.BOLD, 15));
        close.setForeground(new Color(180, 180, 180));
        close.setContentAreaFilled(false);
        close.setBorderPainted(false);
        close.setFocusPainted(false);
        close.setCursor(new Cursor(Cursor.HAND_CURSOR));
        close.addActionListener(e -> { SoundManager.playClick(); System.exit(0); });
        p.add(close, BorderLayout.EAST);
        return p;
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(0, 36, 0, 36));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0; g.weightx = 1;

        // Welcome
        g.gridy = 0; g.insets = new Insets(0, 0, 2, 0);
        JLabel welcome = new JLabel("Welcome Back");
        welcome.setFont(new Font("Segoe UI", Font.BOLD, 24));
        welcome.setForeground(Color.WHITE);
        form.add(welcome, g);

        g.gridy = 1; g.insets = new Insets(0, 0, 22, 0);
        JLabel sub = new JLabel("Sign in to continue");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(new Color(180, 195, 220));
        form.add(sub, g);

        // Role selector label
        g.gridy = 2; g.insets = new Insets(0, 0, 6, 0);
        JLabel roleHint = new JLabel("Login as");
        roleHint.setFont(new Font("Segoe UI", Font.BOLD, 11));
        roleHint.setForeground(new Color(180, 195, 220));
        form.add(roleHint, g);

        // Role buttons
        g.gridy = 3; g.insets = new Insets(0, 0, 18, 0);
        JPanel roleRow = new JPanel(new GridLayout(1, 2, 10, 0));
        roleRow.setOpaque(false);
        adminBtn    = roleBtn("Admin",    "Full Access");
        employeeBtn = roleBtn("Employee", "Limited Access");
        adminBtn.addActionListener(e    -> { SoundManager.playClick(); adminMode = true;  updateRoleBtns(); });
        employeeBtn.addActionListener(e -> { SoundManager.playClick(); adminMode = false; updateRoleBtns(); });
        roleRow.add(adminBtn); roleRow.add(employeeBtn);
        form.add(roleRow, g);
        updateRoleBtns();

        // Password label
        g.gridy = 4; g.insets = new Insets(0, 0, 5, 0);
        JLabel passLbl = new JLabel("Password");
        passLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        passLbl.setForeground(new Color(180, 195, 220));
        form.add(passLbl, g);

        // Password field
        g.gridy = 5; g.insets = new Insets(0, 0, 6, 0);
        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        passwordField.setPreferredSize(new Dimension(0, 44));
        passwordField.setBackground(new Color(28, 42, 68));
        passwordField.setForeground(Color.WHITE);
        passwordField.setCaretColor(Color.WHITE);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 85, 130), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        passwordField.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) attemptLogin();
            }
        });
        form.add(passwordField, g);

        // Error
        g.gridy = 6; g.insets = new Insets(0, 0, 12, 0);
        errorLabel = new JLabel(" ");
        errorLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        errorLabel.setForeground(new Color(252, 100, 100));
        form.add(errorLabel, g);

        // Login button
        g.gridy = 7; g.insets = new Insets(0, 0, 0, 0);
        loginBtn = new JButton("Sign In");
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setBackground(new Color(37, 99, 235));
        loginBtn.setFocusPainted(false);
        loginBtn.setBorderPainted(false);
        loginBtn.setPreferredSize(new Dimension(0, 46));
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginBtn.addActionListener(e -> { SoundManager.playClick(); attemptLogin(); });
        form.add(loginBtn, g);

        return form;
    }

    private JPanel buildBottom() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 18, 0));
        JLabel hint = new JLabel("Admin: 1234   •   Employee: 0000");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        hint.setForeground(new Color(90, 120, 170));
        p.add(hint);
        return p;
    }

    // ─── Role buttons ─────────────────────────────────────────────────────────

    private JButton roleBtn(String label, String sub) {
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(60, 85, 130));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.dispose();

                FontMetrics fm1 = g.getFontMetrics(new Font("Segoe UI", Font.BOLD, 14));
                FontMetrics fm2 = g.getFontMetrics(new Font("Segoe UI", Font.PLAIN, 10));
                int y1 = getHeight()/2 - 3;
                int y2 = getHeight()/2 + fm2.getAscent() + 4;
                g.setColor(getForeground());
                g.setFont(new Font("Segoe UI", Font.BOLD, 14));
                g.drawString(label, (getWidth()-fm1.stringWidth(label))/2, y1);
                g.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                g.setColor(new Color(160,180,210));
                g.drawString(sub,   (getWidth()-fm2.stringWidth(sub))/2,   y2);
            }
        };
        btn.setPreferredSize(new Dimension(0, 58));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        return btn;
    }

    private void updateRoleBtns() {
        Color active   = new Color(37, 99, 235);
        Color inactive = new Color(28, 42, 68);
        adminBtn.setBackground(adminMode ? active : inactive);
        adminBtn.setForeground(adminMode ? Color.WHITE : new Color(180, 195, 220));
        employeeBtn.setBackground(!adminMode ? active : inactive);
        employeeBtn.setForeground(!adminMode ? Color.WHITE : new Color(180, 195, 220));
        adminBtn.repaint(); employeeBtn.repaint();
    }

    // ─── Login logic ─────────────────────────────────────────────────────────

    private void attemptLogin() {
        String pass = new String(passwordField.getPassword()).trim();
        if (pass.isEmpty()) { errorLabel.setText("Please enter your password."); return; }

        String expected = adminMode ? "1234" : "0000";
        String role     = adminMode ? "admin" : "employee";

        if (pass.equals(expected)) {
            errorLabel.setText(" ");
            loginBtn.setEnabled(false);
            loginBtn.setText("Loading…");
            SoundManager.playLoginSuccess();
            Timer t = new Timer(500, e -> {
                setVisible(false);
                new MainFrame(role).setVisible(true);
                dispose();
            });
            t.setRepeats(false); t.start();
        } else {
            failCount++;
            SoundManager.playLoginFail();
            if (failCount >= 3) {
                errorLabel.setText("Too many attempts — please wait 3 s.");
                loginBtn.setEnabled(false);
                Timer lock = new Timer(3000, e -> {
                    loginBtn.setEnabled(true);
                    loginBtn.setText("Sign In");
                    failCount = 0;
                    errorLabel.setText(" ");
                });
                lock.setRepeats(false); lock.start();
            } else {
                errorLabel.setText("Wrong password.  (" + failCount + " / 3 attempts)");
                shake();
            }
        }
    }

    private void shake() {
        Point orig = getLocation();
        int[] off  = {-9,9,-6,6,-3,3,-1,1,0};
        int[] idx  = {0};
        Timer t = new Timer(22, e -> {
            if (idx[0] < off.length) setLocation(orig.x + off[idx[0]++], orig.y);
            else { setLocation(orig); ((Timer)e.getSource()).stop(); }
        });
        t.start();
    }

    // ─── Drag to move ────────────────────────────────────────────────────────

    private void enableDrag(JPanel panel) {
        Point[] drag = {null};
        panel.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { drag[0] = e.getPoint(); }
        });
        panel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseDragged(MouseEvent e) {
                if (drag[0] == null) return;
                Point loc = getLocation();
                setLocation(loc.x + e.getX() - drag[0].x, loc.y + e.getY() - drag[0].y);
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
