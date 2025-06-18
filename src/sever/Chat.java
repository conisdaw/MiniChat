package sever;

import core.ChatCore;
import data.ChatHistorySQL;
import data.UnreadMessages;
import data.ChatMessage;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.sql.Timestamp;

public class Chat {
    public void handle(String jsonBody, OutputStream out, String dbPath) throws IOException, SQLException {
        boolean isGroup = ServiceUtils.extractBooleanField(jsonBody, "isGroup");
        String message = ServiceUtils.extractStringField(jsonBody, "message");
        String messageType = ServiceUtils.extractStringField(jsonBody, "messageType");
        String ip = ServiceUtils.extractStringField(jsonBody, "ip");
        String peerID = ServiceUtils.extractStringField(jsonBody, "peerID");
        int port = ServiceUtils.extractIntField(jsonBody, "port");

        if(isGroup) {
            String groupID = ServiceUtils.extractStringField(jsonBody, "groupID");
            ChatHistorySQL.insertChatMessage(dbPath, true, groupID, peerID, messageType, message, false, ip, port);

            // 存储到未读消息
            ChatMessage chatMsg = new ChatMessage();
            chatMsg.setSenderId(peerID);
            chatMsg.setMessageType(messageType);
            chatMsg.setContent(message);
            chatMsg.setTimestamp(new Timestamp(System.currentTimeMillis()));
            UnreadMessages.addGroupMessage(groupID, chatMsg);
        } else {
            if (ChatCore.isFriendUnblocked(peerID)){
                ChatHistorySQL.insertChatMessage(dbPath, false, null, peerID, messageType, message, false, ip, port);

                // 存储到未读消息
                ChatMessage chatMsg = new ChatMessage();
                chatMsg.setSenderId(peerID);
                chatMsg.setMessageType(messageType);
                chatMsg.setContent(message);
                chatMsg.setTimestamp(new Timestamp(System.currentTimeMillis()));
                UnreadMessages.addFriendMessage(peerID, chatMsg);
            } else {
                ServiceUtils.sendSuccessResponse(out, "你已被拉黑");
            }
        }
        ServiceUtils.sendSuccessResponse(out);
    }
}
