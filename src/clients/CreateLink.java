package clients;

import core.Config;
import core.GetUserContent;
import java.net.SocketException;

public class CreateLink {
    public static String handle(String severIP, int severPort) throws SocketException {
        return ClientsUtils.sendRequest(
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
