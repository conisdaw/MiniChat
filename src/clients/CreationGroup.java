package clients;

import core.Config;
import data.GroupSQL;

import java.net.SocketException;
import java.util.Random;
import java.util.UUID;

public class CreationGroup {

    public static void handle(String groupName, String nicknames, String userIds, String ips, String ports) throws SocketException {
        Random random = new Random();
        String groupID = (10000000 + random.nextInt(90000000)) + UUID.randomUUID().toString();
        GroupSQL.createGroup(Config.DB_PATH, groupID, groupName, Config.USER_ID, Config.IP, Config.PORT);
        String[][] network = new String[2][];
        network[0] = ips.split(",");
        network[1] = ports.split(",");
        String[][] userInformation = new String[2][];
        userInformation[0] = nicknames.split(",");
        userInformation[1] = userIds.split(",");
        for (int i = 0; i < network[0].length; i++) {
            GroupSQL.addMember(Config.DB_PATH,groupID,userInformation[1][i],network[0][i],Integer.parseInt(network[1][i]), userInformation[0][i]);
            ClientsUtils.sendRequest(
                    ClientsUtils.constructRequest(
                            "/group/creation",
                            JsonPayloadBuilder.buildCreateGroup(
                                    groupID,
                                    groupName,
                                    ports,
                                    ips,
                                    userIds,
                                    nicknames
                            )
                    ),
                    network[0][i],
                    Integer.parseInt(network[1][i])
            );
        }
    }
}
