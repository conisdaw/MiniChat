package clients;

import core.Config;
import core.GetUserContent;
import java.net.SocketException;

public class CreateLink {
    public void handle(String severIP, int severPort) throws SocketException {
        ClientsUtils.sendRequest(
                ClientsUtils.constructRequest(
                  "/createLink",
                       JsonPayloadBuilder.buildFriendRequest(
                               GetUserContent.UserID(),
                               Config.IP,
                               Config.PORT
                       )
                ),
                severIP,
                severPort
        );
    }
}
