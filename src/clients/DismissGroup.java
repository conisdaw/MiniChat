package clients;

import core.Config;
import data.GroupSQL;

public class DismissGroup {
    public static boolean handle(String groupID, String severIP, int severPort) {
        if(GroupSQL.isOwner(Config.DB_PATH, groupID, Config.USER_ID)) {
            ClientsUtils.sendRequest(
                ClientsUtils.constructRequest(
                        "/group/dismiss",
                        JsonPayloadBuilder.buildSimpleGroupOp(groupID)
                ),
                severIP,
                severPort
        );
        return true;
        } else {
            return false;
        }
    }
}
