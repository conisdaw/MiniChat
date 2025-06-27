package sever;

import data.UserSQL;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;

public class Register {
    public void handle(String jsonBody, OutputStream out, String dbPath) throws IOException, SQLException {
        String nickname = ServiceUtils.extractStringField(jsonBody,"nickname");
        String account = ServiceUtils.extractStringField(jsonBody, "account");
        String password = ServiceUtils.extractStringField(jsonBody, "password");
        String ip = ServiceUtils.extractStringField(jsonBody, "ip");
        int port = ServiceUtils.extractIntField(jsonBody, "port");
        if (UserSQL.register(nickname, account, password, ip, port)) {
            ServiceUtils.sendSuccessResponse(out, "Ciallo～(∠・ω< )⌒★注册成功");
        } else {
            ServiceUtils.sendSuccessResponse(out, "Ciallo～(∠・ω< )⌒★注册失败");
        }
    }
}
