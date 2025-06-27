package sever;

import data.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RadioMode {
    public void handle(String jsonBody, OutputStream out) throws IOException {
        String senderId = ServiceUtils.extractStringField(jsonBody, "senderId");
        String massage = ServiceUtils.extractStringField(jsonBody, "massage");
        if (UserMute.isUserMuted(senderId)) {
            ServiceUtils.sendSuccessResponse(out,"Ciallo～(∠・ω< )⌒★你被禁言啦!!!");
            return;
        }
        if(Disable.isDisableUser(senderId)) {
            ServiceUtils.sendSuccessResponse(out,"Ciallo～(∠・ω< )⌒★你被禁言啦!!!");
            return;
        }
        if (Disable.isDisableMessage(senderId, massage)) {
            ServiceUtils.sendSuccessResponse(out,"Ciallo～(∠・ω< )⌒★你被禁言啦!!!");
            return;
        }
        List<String> onlineList = new ArrayList<>(MemberList.getOnlineList());
        onlineList.remove(senderId);
        if (onlineList.isEmpty()) return;
        String name = ServiceUtils.extractStringField(jsonBody, "name");
        String messageType = ServiceUtils.extractStringField(jsonBody, "messageType");
        senderId = UserSQL.getUserAccount(senderId);
        for(String user : onlineList) {
            Map<String, String> userNetworkInfo = UserSQL.getUserNetworkInfo(user);
            ClientsUtils.sendRequest(
                    ClientsUtils.constructRequest(
                            "/radioMode",
                            JsonPayloadBuilder.buildRadioMessage(
                                    name,
                                    massage,
                                    senderId,
                                    messageType
                            )
                    ),
                    userNetworkInfo.get("ip"),
                    Integer.parseInt(userNetworkInfo.get("port"))
            );
        }

    }
}
