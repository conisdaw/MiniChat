import core.Config;
import data.Database;
import gui.Manage;
import gui.ServerConnection;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ServerConnection network = new ServerConnection(null);
            network.setVisible(true);

            if (network.isSettingsSaved()) {
                Database.initializeDatabase(Config.DB_PATH);

                new Manage();
            } else {
                System.exit(0);
            }
        });
    }
}
