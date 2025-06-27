package sever;

import data.MemberList;
import data.RequestNotSent;
import data.UserSQL;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Map;

public class CreateLink {
    public void handle(String jsonBody, OutputStream out, String pathDB) throws IOException, SQLException {
        String senderId = ServiceUtils.extractStringField(jsonBody, "senderId");
        String friendAccount = ServiceUtils.extractStringField(jsonBody, "friendAccount");
        String friendId = UserSQL.getUserId(friendAccount);
        String friendNickname = UserSQL.getUserNickname(friendId);
        String senderNickname = UserSQL.getUserNickname(senderId);


        if (MemberList.isOnline(friendId)) {
            Map<String, String> friend = UserSQL.getUserNetworkInfo(friendId);
            String friendIp = friend.get("ip");
            int friendPort = Integer.parseInt(friend.get("port"));

            ClientsUtils.sendRequest(
                    ClientsUtils.constructRequest(
                            "/createLink",
                            JsonPayloadBuilder.buildFriendRequest(
                                    senderNickname,
                                    senderId
                            )
                    ),
                    friendIp,
                    friendPort
            );
        } else {
            String requestJson = JsonPayloadBuilder.buildFriendRequest(
                    senderNickname,
                    senderId
            );
            RequestNotSent.addRequest(friendId, "/createLink", requestJson);
        }

        ServiceUtils.sendSuccessResponse(out, friendId + "," + friendNickname);
    }
}
