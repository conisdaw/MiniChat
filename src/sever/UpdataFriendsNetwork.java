package sever;

import data.UserSQL;

import java.io.IOException;
import java.io.OutputStream;

public class UpdataFriendsNetwork {
    public void handle(String jsonBody, OutputStream out) {
        String friendId = ServiceUtils.extractStringField(jsonBody, "friendId");
        String IP = ServiceUtils.extractStringField(jsonBody, "ip");
        int port = ServiceUtils.extractIntField(jsonBody, "port");
        UserSQL.updateNetwork(friendId, IP, port);
        try {
            ServiceUtils.sendSuccessResponse(out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
