package sever;

import data.GroupSQL;
import data.MemberList;
import data.RequestNotSent;
import data.UserSQL;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public class DismissGroup {
    public void handle(String jsonBody, OutputStream out, String dbPath) throws IOException {
        String groupId = ServiceUtils.extractStringField(jsonBody, "groupId");
        List<String> groupMemberIds = GroupSQL.getGroupMemberIds(groupId);
        GroupSQL.deleteGroup(dbPath ,groupId);

        for (String friend : groupMemberIds) {
            if (MemberList.isOnline(friend)) {
                Map<String, String> temp = UserSQL.getUserNetworkInfo(friend);
                String ip = temp.get("ip");
                int port = Integer.parseInt(temp.get("port"));
                ClientsUtils.sendRequest(
                        ClientsUtils.constructRequest(
                                "/group/dismiss",
                                JsonPayloadBuilder.buildSimpleGroupOp(groupId)
                        ),
                        ip,
                        port
                );
            } else {
                String requestJson = JsonPayloadBuilder.buildSimpleGroupOp(groupId);
                RequestNotSent.addRequest(friend, "/group/dismiss", requestJson);
            }

        }

        ServiceUtils.sendSuccessResponse(out);
    }
}
