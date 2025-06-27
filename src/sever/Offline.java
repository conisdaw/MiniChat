package sever;

import core.Config;
import data.MemberList;
import data.UserSQL;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;

public class Offline {
    public void handle(String jsonBody, OutputStream out, String dbPath) throws IOException, SQLException {
        String userId = ServiceUtils.extractStringField(jsonBody,"userId");
        MemberList.offline(userId);
        String username = UserSQL.getUserNickname(userId);
        String massage = "[系统提醒]用户" + username + "已下线";
        ClientsUtils.sendRequest(
                ClientsUtils.constructRequest(
                        "/radioMode",
                        JsonPayloadBuilder.buildRadioMessage(
                                "系统",
                                massage,
                                "系统",
                                "系统"
                        )
                ),
                "127.0.0.1",
                Config.SERVER_PORT
        );
        ServiceUtils.sendSuccessResponse(out);
    }
}
