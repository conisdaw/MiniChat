package sever.user;

import data.GroupSQL;
import sever.ServiceUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;

public class DismissGroup {
    public void handle(String jsonBody, OutputStream out, String dbPath) throws IOException {
        String groupId = ServiceUtils.extractStringField(jsonBody, "groupId");
        GroupSQL.deleteGroup(dbPath ,groupId);
        ServiceUtils.sendSuccessResponse(out);
    }
}
