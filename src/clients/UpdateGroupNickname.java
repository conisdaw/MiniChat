package clients;

public class UpdateGroupNickname {
    public void handle(String groupId,String memberId,String nickname,String severIP, int severPort) {
        ClientsUtils.sendRequest(
                ClientsUtils.constructRequest(
                        "/group/update/nickname",
                        JsonPayloadBuilder.buildUpdateMemberNickname(
                                groupId,
                                memberId,
                                nickname
                        )
                ),
                severIP,
                severPort
        );
    }
}
