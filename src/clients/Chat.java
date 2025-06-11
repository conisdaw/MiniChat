package clients;

import core.GetUserContent;
import java.net.SocketException;

public class Chat {
    public void handle (boolean isGroup, String message, String severIP, int severPort) throws SocketException {
        ClientsUtils.sendRequest(
                ClientsUtils.constructRequest(
                        "/chat",
                        JsonPayloadBuilder.buildPeerMessage
                                (isGroup, message, GetUserContent.personIP(), GetUserContent.personPort(), GetUserContent.UserID())),
                                severIP,
                                severPort
                );
    }
}
