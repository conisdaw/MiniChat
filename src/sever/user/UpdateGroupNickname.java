package sever.user;

import data.GroupSQL;
import sever.ServiceUtils;

import java.io.IOException;
import java.io.OutputStream;

public class UpdateGroupNickname {
    public void handle (String jsonBody, OutputStream out, String dbPath) throws IOException {
        String groupId = ServiceUtils.extractStringField(jsonBody, "groupId");
        String memberId = ServiceUtils.extractStringField(jsonBody, "memberId");
        String nickname = ServiceUtils.extractStringField(jsonBody, "nickname");
        GroupSQL.updateMemberNickname(dbPath, groupId, memberId, nickname);
        ServiceUtils.sendSuccessResponse(out);
    }
}
