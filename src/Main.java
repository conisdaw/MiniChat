import core.Config;
import data.Database;
import gui.LoginSystem;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // 新增数据库初始化检查
        Database.initializeDatabase(Config.DB_PATH);

        SwingUtilities.invokeLater(() -> {
            LoginSystem loginSystem = new LoginSystem();
            loginSystem.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            loginSystem.setVisible(true);
        });
    }
}
