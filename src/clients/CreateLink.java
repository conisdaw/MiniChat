package clients;

import core.GetUserContent;
import java.net.SocketException;

public class CreateLink {
    public void handle(String severIP, int severPort) throws SocketException {
        ClientsUtils.sendRequest(
                ClientsUtils.constructRequest(
                  "/createLink",
                       JsonPayloadBuilder.buildFriendRequest(
                               GetUserContent.UserID(),
                               GetUserContent.personIP(),
                               GetUserContent.personPort()
                       )
                ),
                severIP,
                severPort
        );
    }
}
