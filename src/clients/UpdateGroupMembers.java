package clients;

public class UpdateGroupMembers {
    public void handle(String groupId,String memberId,String userIP, int userPort,String severIP, int severPort) {
        ClientsUtils.sendRequest(
                ClientsUtils.constructRequest(
                        "/group/update/network",
                        JsonPayloadBuilder.buildAddGroupMember(
                                groupId,
                                memberId,
                                userIP,
                                userPort
                        )
                ),
                severIP,
                severPort
        );
    }
}
