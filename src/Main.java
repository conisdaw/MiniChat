import gui.LoginSystem;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginSystem loginSystem = new LoginSystem();
            loginSystem.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            loginSystem.setVisible(true);
        });
    }
}
