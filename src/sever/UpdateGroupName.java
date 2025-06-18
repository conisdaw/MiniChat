package sever;
import data.GroupSQL;
import data.ListIsUpdated;

import java.io.IOException;
import java.io.OutputStream;

public class UpdateGroupName {
    public void handle(String jsonBody, OutputStream out, String dbPath) throws IOException {
        String groupId = ServiceUtils.extractStringField(jsonBody, "groupId");
        String GroupName = ServiceUtils.extractStringField(jsonBody, "GroupName");
        GroupSQL.updateGroupName(dbPath, groupId, GroupName);
        ListIsUpdated.groupNotNull();
        ServiceUtils.sendSuccessResponse(out);
    }
}
