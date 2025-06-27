package sever;

import data.UserSQL;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Map;

public class Login {
    public void handle(String jsonBody, OutputStream out, String dbPath) throws IOException, SQLException {
        String account = ServiceUtils.extractStringField(jsonBody, "account");
        String password = ServiceUtils.extractStringField(jsonBody, "password");
        String userId = UserSQL.getUserId(account);
        String nickname = UserSQL.getUserNickname(userId);
        if (UserSQL.isAccountBlocked(account)) {
            ServiceUtils.sendSuccessResponse(out, "false,Ciallo～(∠・ω< )⌒★账号被禁用啦!!!");
            return;
        }
        Map<String, String> login = UserSQL.login(account, password);
        ServiceUtils.sendSuccessResponse(out, login.get("success") + "," + login.get("user_id") + "," + account + "," + nickname);
    }
}
