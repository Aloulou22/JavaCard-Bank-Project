package com;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MyButton extends JButton {

    public MyButton (String text) {
        super(text);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setFont(new Font("Arial", Font.BOLD, 18));
        setForeground(Color.WHITE);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        addMouseEffects();
    }

    private void addMouseEffects() {
        // Add hover and click effects
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setForeground(Color.CYAN); // Change text color on hover
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setForeground(Color.WHITE); // Reset text color
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                setForeground(Color.YELLOW); // Change text color on click
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                setForeground(Color.CYAN); // Return to hover color
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw button background
        if (getModel().isRollover()) {
            g2d.setColor(new Color(255, 255, 255, 80)); // Glowing effect
        } else {
            g2d.setColor(new Color(50, 50, 50)); // Default button color
        }
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);

        // Draw border
        g2d.setColor(new Color(255, 255, 255, 150));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 25, 25);

        // Draw glowing effect
        if (getModel().isRollover()) {
            g2d.setColor(new Color(255, 255, 255, 100));
            for (int i = 0; i < 5; i++) {
                g2d.drawRoundRect(i, i, getWidth() - 2 * i, getHeight() - 2 * i, 25 - i, 25 - i);
            }
        }

        // Paint text
        super.paintComponent(g);
    }

    @Override
    protected void paintBorder(Graphics g) {
        // Override to skip painting the default border
    }


}
