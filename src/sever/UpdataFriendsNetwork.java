package sever;

import core.Config;
import core.SetUserContent;
import data.FriendsSQL;

import java.io.OutputStream;
import java.sql.SQLException;

public class UpdataFriendsNetwork {
    public void handle(String jsonBody, OutputStream out) {
        String friendId = ServiceUtils.extractStringField(jsonBody, "friendId");
        String IP = ServiceUtils.extractStringField(jsonBody, "ip");
        int port = ServiceUtils.extractIntField(jsonBody, "port");
        try {
            FriendsSQL.updateNetwork(Config.DB_PATH, friendId, IP, port);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
