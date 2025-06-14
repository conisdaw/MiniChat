package sever;

import data.ChatHistorySQL;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;

public class Chat {
    public void handle(String jsonBody, OutputStream out, String dbPath) throws IOException, SQLException {
        boolean isGroup = ServiceUtils.extractBooleanField(jsonBody, "isGroup");
        String message = ServiceUtils.extractStringField(jsonBody, "message");
        String ip = ServiceUtils.extractStringField(jsonBody, "ip");
        String peerID = ServiceUtils.extractStringField(jsonBody, "peerID");
        int port = ServiceUtils.extractIntField(jsonBody, "port");

        if(isGroup) {
            String groupID = ServiceUtils.extractStringField(jsonBody, "groupID");
            ChatHistorySQL.insertChatMessage(dbPath, true, groupID, peerID, "text", message, true, ip ,port);
        } else {
            ChatHistorySQL.insertChatMessage(dbPath, false, null, peerID, "text", message, true, ip ,port);
        }
        ServiceUtils.sendSuccessResponse(out);
    }

}
