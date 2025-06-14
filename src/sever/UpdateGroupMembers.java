package sever;

import data.GroupSQL;

import java.io.IOException;
import java.io.OutputStream;

public class UpdateGroupMembers {
    public void handle(String jsonBody, OutputStream out, String dbPath) throws IOException {
        String groupId = ServiceUtils.extractStringField(jsonBody, "groupId");
        String memberId = ServiceUtils.extractStringField(jsonBody, "memberId");
        String ip = ServiceUtils.extractStringField(jsonBody, "ip");
        int port = ServiceUtils.extractIntField(jsonBody, "port");
        GroupSQL.updateMemberNetwork(dbPath, groupId, memberId, ip, port);
        ServiceUtils.sendSuccessResponse(out);
    }
}
