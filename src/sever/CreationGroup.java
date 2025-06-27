package sever;

import data.GroupSQL;
import data.MemberList;
import data.RequestNotSent;
import data.UserSQL;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class CreationGroup {
    public void handle(String jsonBody, OutputStream out, String dbPath) throws IOException, SQLException {
        Random random = new Random();
        String groupId;
        int maxAttempts = 10;
        int attempts = 0;

        do {
            groupId = (10000000 + random.nextInt(90000000)) + UUID.randomUUID().toString();
            attempts++;
            if (attempts > maxAttempts) {
                throw new IOException("无法生成唯一群组ID");
            }
        } while (GroupSQL.isGroupIdExists(dbPath, groupId));

        String groupName = ServiceUtils.extractStringField(jsonBody, "groupName");
        String senderId = ServiceUtils.extractStringField(jsonBody, "senderId");
        String friendId = ServiceUtils.extractStringField(jsonBody, "friendId");
        String nickname = ServiceUtils.extractStringField(jsonBody, "nickname");
        String[] friends = friendId.split(",");

        GroupSQL.createGroup(dbPath, groupId, groupName, senderId);

        for (String friend : friends) {
            GroupSQL.addMember(dbPath, groupId, friend);
            if (MemberList.isOnline(friend)) {
                Map<String, String> temp = UserSQL.getUserNetworkInfo(friend);
                String friendIp = temp.get("ip");
                int friendPort = Integer.parseInt(temp.get("port"));
                ClientsUtils.sendRequest(
                        ClientsUtils.constructRequest(
                                "/group/creation",
                                JsonPayloadBuilder.buildCreateGroup(
                                        groupId,
                                        groupName,
                                        friendId,
                                        nickname
                                )
                        ),
                        friendIp,
                        friendPort
                );
            } else {
                String requestJson = JsonPayloadBuilder.buildCreateGroup(
                        groupId,
                        groupName,
                        friendId,
                        nickname
                );
                RequestNotSent.addRequest(friend, "/group/creation", requestJson);
            }
        }

        ServiceUtils.sendSuccessResponse(out);
    }
}