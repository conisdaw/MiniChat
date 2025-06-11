package sever.user;
import data.GroupSQL;
import sever.ServiceUtils;
import java.io.IOException;
import java.io.OutputStream;

public class UpdateGroupName {
    public void handle(String jsonBody, OutputStream out, String dbPath) throws IOException {
        String groupId = ServiceUtils.extractStringField(jsonBody, "groupId");
        String GroupName = ServiceUtils.extractStringField(jsonBody, "GroupName");
        GroupSQL.updateGroupName(dbPath, groupId, GroupName);
        ServiceUtils.sendSuccessResponse(out);
    }
}
