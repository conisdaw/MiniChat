package sever.user;

import data.FriendsSQL;
import sever.ServiceUtils;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;

public class UpdataFriendsNickname {
    public void handle(String jsonBody, OutputStream out, String dbPath) throws SQLException, IOException {
        String friend_id = ServiceUtils.extractStringField(jsonBody, "friendID");
        String nickname = ServiceUtils.extractStringField(jsonBody, "nickname");
        FriendsSQL.updateNickname(dbPath, friend_id, nickname);
        ServiceUtils.sendSuccessResponse(out);
    }
}
