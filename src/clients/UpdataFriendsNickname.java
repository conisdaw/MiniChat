package clients;

public class UpdataFriendsNickname {
    public static void handle(String friendID, String nickname, String severIP, int severPort) {
        ClientsUtils.sendRequest(
                ClientsUtils.constructRequest(
                        "/friend/nickname",
                        JsonPayloadBuilder.buildUpdateFriendNickname(
                                friendID,
                                nickname
                        )
                ),
                severIP,
                severPort
        );
    }
}
