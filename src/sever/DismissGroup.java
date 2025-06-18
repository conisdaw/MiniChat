package sever;

import data.GroupSQL;
import data.ListIsUpdated;

import java.io.IOException;
import java.io.OutputStream;

public class DismissGroup {
    public void handle(String jsonBody, OutputStream out, String dbPath) throws IOException {
        String groupId = ServiceUtils.extractStringField(jsonBody, "groupId");
        GroupSQL.deleteGroup(dbPath ,groupId);
        ListIsUpdated.groupNotNull();
        ServiceUtils.sendSuccessResponse(out);
    }
}
