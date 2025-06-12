package core;

import data.UserSQL;
import java.util.List;
import java.util.Map;

public class UserLogical {

    public String register(String account, String password, String nickname) {
        return UserSQL.initialize(Config.DB_PATH, account, password, nickname);
    }

    public boolean login(String account, String password) {
        List<String> userIds = UserSQL.getAllUserIds(Config.DB_PATH);

        for (String userId : userIds) {
            Map<String, Object> userInfo = UserSQL.getUserInfo(Config.DB_PATH, userId);
            if (userInfo != null) {
                String storedAccount = (String) userInfo.get("account");
                String storedPassword = (String) userInfo.get("password");

                if (account.equals(storedAccount) && password.equals(storedPassword)) {
                    return true;
                }
            }
        }
        return false;
    }
}