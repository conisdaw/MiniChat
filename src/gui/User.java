package gui;

import javax.swing.*;

public class User {
    public void handle() {
        SwingUtilities.invokeLater(() -> {
            LoginSystem loginSystem = new LoginSystem();
            loginSystem.setVisible(true);
        });
    }
}