package clients;

public class DismissGroup {
    public void handle(String groupID,String severIP, int severPort) {
        ClientsUtils.sendRequest(
                ClientsUtils.constructRequest(
                        "/group/dismiss",
                        JsonPayloadBuilder.buildSimpleGroupOp(groupID)
                ),
                severIP,
                severPort
        );
    }
}
