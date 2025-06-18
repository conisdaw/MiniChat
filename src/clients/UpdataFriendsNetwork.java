package clients;

public class UpdataFriendsNetwork {
    public static void handle(String friendID, String ip, int port, String severIP, int severPort) {
        ClientsUtils.sendRequest(
                ClientsUtils.constructRequest(
                        "/friend/network",
                        JsonPayloadBuilder.buildUpdateFriendNetwork(
                                friendID,
                                ip,
                                port
                        )
                ),
                severIP,
                severPort
        );
    }
}
