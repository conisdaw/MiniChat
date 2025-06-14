package sever;

import data.FriendsSQL;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;

public class CreateLink {
    public void handle(String jsonBody, OutputStream out, String pathDB) throws IOException, SQLException {
        String friend_id = ServiceUtils.extractStringField(jsonBody, "friend_id");
        String ip = ServiceUtils.extractStringField(jsonBody, "ip");
        int port = ServiceUtils.extractIntField(jsonBody, "port");
        FriendsSQL.addFriend(pathDB, friend_id, ip, port);
        ServiceUtils.sendSuccessResponse(out);
    }
}
