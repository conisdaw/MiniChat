package sever;

import core.Config;
import data.FriendsSQL;
import data.ListIsUpdated;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;

public class CreateLink {
    public void handle(String jsonBody, OutputStream out, String pathDB) throws IOException, SQLException {
        String friend_id = ServiceUtils.extractStringField(jsonBody, "friend_id");
        String ip = ServiceUtils.extractStringField(jsonBody, "ip");
        String nickname = ServiceUtils.extractStringField(jsonBody, "nickname");
        int port = ServiceUtils.extractIntField(jsonBody, "port");
        FriendsSQL.addFriend(pathDB, friend_id, ip, port);
        FriendsSQL.updateNickname(pathDB, friend_id, nickname);
        ListIsUpdated.friendNotNull();
        ServiceUtils.sendSuccessResponse(out, Config.USER_ID + "," + Config.USER_NAME);
    }
}
