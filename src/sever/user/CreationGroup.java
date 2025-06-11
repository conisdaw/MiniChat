package sever.user;

import data.GroupSQL;
import sever.ServiceUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;

public class CreationGroup {
    public void handle(String jsonBody, OutputStream out, String dbPath) throws IOException, SQLException {
        String[][] network = new String[4][];
        String groupId = ServiceUtils.extractStringField(jsonBody, "groupId");
        String groupName = ServiceUtils.extractStringField(jsonBody, "groupName");
        network[0] = ServiceUtils.extractStringField(jsonBody, "ip").split(",");
        network[1] = ServiceUtils.extractStringField(jsonBody, "port").split(",");
        network[2] = ServiceUtils.extractStringField(jsonBody, "userID").split(","); // 用户ID
        network[3] = ServiceUtils.extractStringField(jsonBody, "nickname").split(",");

        GroupSQL.createGroup(dbPath, groupId, groupName, network[2][0], network[0][0], Integer.parseInt(network[1][0]));

        for (int i = 1; i < network[0].length; i++) GroupSQL.addMember(dbPath, groupId, network[2][i], network[0][i], Integer.parseInt(network[1][i]), network[3][i]);

        ServiceUtils.sendSuccessResponse(out);
    }
}
