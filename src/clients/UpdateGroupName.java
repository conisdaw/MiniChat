package clients;

public class UpdateGroupName {
    public static void handle(String groupId, String groupName, String severIP, int severPort) {
        ClientsUtils.sendRequest(
                ClientsUtils.constructRequest(
                        "/group/name",
                        JsonPayloadBuilder.buildUpdateGroupName(
                                groupId,
                                groupName
                        )
                ),
                severIP,
                severPort
        );
    }
}
