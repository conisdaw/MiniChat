package core;

import data.UserSQL;

public class SetUserContent {
    public static int setPort(int port) {
        UserSQL.updatePort(Config.DB_PATH, port, GetUserContent.UserID());
        return port;
    }
}
