package com.stickynotev2;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;

public class Sandbox {
    static void main() {
        SwingUtilities.invokeLater(() -> {
            FlatLightLaf.setup();
            JFrame frame = new JFrame();
            frame.setSize(400,400);
            frame.setVisible(true);
        });
    }
}
