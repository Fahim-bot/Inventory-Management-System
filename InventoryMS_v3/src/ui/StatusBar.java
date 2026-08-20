package ui;

import service.SoundManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class StatusBar extends JPanel {
    private final JLabel statusLabel;
    private final JLabel timeLabel;
    private Runnable saveListener;

    public StatusBar(String role) {
        setBackground(new Color(248, 250, 252));
        setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 200)));
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(0, 36));

        statusLabel = new JLabel("Ready");
        statusLabel.setFont(Theme.SMALL_FONT);
        statusLabel.setForeground(Theme.TEXT_SECONDARY);
        statusLabel.setBorder(new EmptyBorder(0, 16, 0, 0));
        add(statusLabel, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        right.setOpaque(false);
        right.setBorder(new EmptyBorder(0, 0, 0, 16));

        JButton saveBtn = new JButton("💾  Save");
        saveBtn.setFont(Theme.SMALL_FONT);
        saveBtn.setForeground(Theme.PRIMARY);
        saveBtn.setBorderPainted(false);
        saveBtn.setContentAreaFilled(false);
        saveBtn.setFocusPainted(false);
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveBtn.addActionListener(e -> {
            if (saveListener != null) saveListener.run();
        });
        right.add(saveBtn);

        JButton soundBtn = new JButton("🔊  Sound: ON");
        soundBtn.setFont(Theme.SMALL_FONT);
        soundBtn.setForeground(Theme.TEXT_SECONDARY);
        soundBtn.setBorderPainted(false);
        soundBtn.setContentAreaFilled(false);
        soundBtn.setFocusPainted(false);
        soundBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        soundBtn.addActionListener(e -> {
            boolean nowOn = !SoundManager.isEnabled();
            SoundManager.setEnabled(nowOn);
            soundBtn.setText(nowOn ? "🔊  Sound: ON" : "🔇  Sound: OFF");
            if (nowOn) SoundManager.playClick();
        });
        right.add(soundBtn);

        JLabel conn = new JLabel("🟢 Local Storage");
        conn.setFont(Theme.SMALL_FONT);
        conn.setForeground(Theme.SUCCESS);
        right.add(conn);

        JLabel user = new JLabel("👤 " + (role.equals("admin") ? "Admin" : "Employee"));
        user.setFont(Theme.SMALL_FONT);
        user.setForeground(Theme.TEXT_SECONDARY);
        right.add(user);

        timeLabel = new JLabel("🕐 --:--");
        timeLabel.setFont(Theme.SMALL_FONT);
        timeLabel.setForeground(Theme.TEXT_SECONDARY);
        right.add(timeLabel);

        add(right, BorderLayout.EAST);
    }

    public void setMessage(String msg) { statusLabel.setText(msg); }
    public void setTime(String time)   { timeLabel.setText("🕐 " + time); }
    public void addSaveListener(Runnable listener) { this.saveListener = listener; }
}
