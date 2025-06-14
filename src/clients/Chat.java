package clients;

import core.Config;
import core.GetUserContent;

public class Chat {
    public static void handle(boolean isGroup, String messageType,String message, String groupId,String severIP, int severPort) {
        if(isGroup) {
            ClientsUtils.sendRequest(
                    ClientsUtils.constructRequest(
                            "/chat",
                            JsonPayloadBuilder.buildPeerMessage
                                    (true, messageType, message, Config.IP, Config.PORT, GetUserContent.UserID(), groupId)),
                    severIP,
                    severPort
            );
        } else {
            ClientsUtils.sendRequest(
                    ClientsUtils.constructRequest(
                            "/chat",
                            JsonPayloadBuilder.buildPeerMessage
                                    (false, messageType, message, Config.IP, Config.PORT, GetUserContent.UserID(), null)),
                    severIP,
                    severPort
            );
        }
    }
}
