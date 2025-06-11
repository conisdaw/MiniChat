package clients;

import core.GetUserContent;

import java.net.SocketException;

public class CreationGroup {
    public void handle(String groupID, String groupName, String nickname, String userIds, String ips, String ports) throws SocketException {
        String[][] network = new String[2][];
        network[0] = ips.split(",");
        network[1] = ports.split(",");
        String myself = GetUserContent.personIP();
        for (int i = 0; i < network[0].length; i++) {
            if(network[0][i].equals(myself)) continue;
            ClientsUtils.sendRequest(
                    ClientsUtils.constructRequest(
                            "/group/creation",
                            JsonPayloadBuilder.buildCreateGroup(
                                    groupID,
                                    groupName,
                                    ports,
                                    ips,
                                    userIds,
                                    nickname
                            )
                    ),
                    network[0][i],
                    Integer.parseInt(network[1][i])
            );
        }
    }
}
