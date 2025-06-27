package sever;

import core.Config;
import data.MemberList;
import data.RequestNotSent;
import data.UserSQL;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class Online {
    public void handle(String jsonBody) throws IOException, SQLException {
        String userId = ServiceUtils.extractStringField(jsonBody,"userId");
        String userIp = ServiceUtils.extractStringField(jsonBody, "ip");
        int userPort = ServiceUtils.extractIntField(jsonBody, "port");
        String username = UserSQL.getUserNickname(userId);
        MemberList.online(userId);
        UserSQL.updateNetwork(userId, userIp, userPort);
        String massage = "[系统提醒]用户" + username + "已上线";

        if (RequestNotSent.hasPendingRequests(userId)) {
            List<RequestNotSent.PendingRequest> requests =
                    RequestNotSent.getAndRemoveRequests(userId);

            for (RequestNotSent.PendingRequest req : requests) {
                if ("/chat".equals(req.getRequestUrl())) {
                    if(!"text".equals(ServiceUtils.extractStringField(req.getJsonBody(), "messageType"))) {
                        String message = ServiceUtils.extractStringField(req.getJsonBody(), "message");
                        FileSender.handle(message, message, userIp, userPort);
                        ClientsUtils.sendRequest(
                                ClientsUtils.constructRequest(req.getRequestUrl(), req.getJsonBody()),
                                userIp,
                                userPort
                        );
                    } else {
                        ClientsUtils.sendRequest(
                                ClientsUtils.constructRequest(req.getRequestUrl(), req.getJsonBody()),
                                userIp,
                                userPort
                        );
                    }
                } else {
                    ClientsUtils.sendRequest(
                            ClientsUtils.constructRequest(req.getRequestUrl(), req.getJsonBody()),
                            userIp,
                            userPort
                    );
                }
            }
        }

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

    }
}
