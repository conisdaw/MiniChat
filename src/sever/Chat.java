package sever;

import data.*;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class Chat {
    public void handle(String jsonBody, OutputStream out, String dbPath) throws IOException, SQLException {
        boolean isGroup = ServiceUtils.extractBooleanField(jsonBody, "isGroup");
        String message = ServiceUtils.extractStringField(jsonBody, "message");
        String messageType = ServiceUtils.extractStringField(jsonBody, "messageType");
        String senderId = ServiceUtils.extractStringField(jsonBody, "senderId");
        String userId = ServiceUtils.extractStringField(jsonBody, "userId");

        if(isGroup) {
            String groupID = ServiceUtils.extractStringField(jsonBody, "groupID");
            List<String> userIds = GroupSQL.getGroupMemberIds(groupID);

            for (String user : userIds) {

                Map<String, String> users = UserSQL.getUserNetworkInfo(user);
                String userIp = users.get("ip");
                int userPort = Integer.parseInt(users.get("port"));


                if (MemberList.isOnline(userId)) {
                    if (!"text".equals(messageType)) FileSender.handle(message, message, userIp, userPort);

                    ClientsUtils.sendRequest(
                            ClientsUtils.constructRequest(
                                    "/chat",
                                    JsonPayloadBuilder.buildPeerMessage(
                                            true,
                                            messageType,
                                            message,
                                            senderId,
                                            groupID
                                    )
                            ),
                            userIp,
                            userPort
                    );
                } else {
                    String requestJson = JsonPayloadBuilder.buildPeerMessage(
                            true,
                            messageType,
                            message,
                            senderId,
                            null
                    );
                    RequestNotSent.addRequest(user, "/chat", requestJson);
                }
            }
        } else {
            if (UserMute.isUserMuted(userId)) {
                ServiceUtils.sendSuccessResponse(out,"Ciallo～(∠・ω< )⌒★你被禁言啦!!!");
                return;
            }
            if(Disable.isDisableUser(userId)) {
                ServiceUtils.sendSuccessResponse(out,"Ciallo～(∠・ω< )⌒★你被禁言啦!!!");
                return;
            }
            if (Disable.isDisableMessage(userId, message)) {
                ServiceUtils.sendSuccessResponse(out,"Ciallo～(∠・ω< )⌒★你被禁言啦!!!");
                return;
            }
            Map<String, String> user = UserSQL.getUserNetworkInfo(userId);
            String userIp = user.get("ip");
            int userPort = Integer.parseInt(user.get("port"));
            if (MemberList.isOnline(userId)) {
                if (!"text".equals(messageType)) FileSender.handle(message, message, userIp, userPort);

                ClientsUtils.sendRequest(
                        ClientsUtils.constructRequest(
                                "/chat",
                                JsonPayloadBuilder.buildPeerMessage(
                                        false,
                                        messageType,
                                        message,
                                        senderId,
                                        null
                                )
                        ),
                        userIp,
                        userPort
                );
            } else {
                String requestJson = JsonPayloadBuilder.buildPeerMessage(
                        false,
                        messageType,
                        message,
                        senderId,
                        null
                );
                RequestNotSent.addRequest(userId, "/chat", requestJson);
            }
        }
        ServiceUtils.sendSuccessResponse(out);
    }
}
