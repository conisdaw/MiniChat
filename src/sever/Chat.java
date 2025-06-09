package sever;

import java.io.IOException;
import java.io.OutputStream;

public class Chat {
    public void handle(String jsonBody, OutputStream out) throws IOException {
        String nickname = ServiceUtils.extractStringField(jsonBody, "nickname");
        String message = ServiceUtils.extractStringField(jsonBody, "message");
        boolean avatarChanges = ServiceUtils.extractBooleanField(jsonBody, "avatarChanges");

        if (nickname.isEmpty() || message.isEmpty()) {
            ServiceUtils.sendErrorResponse(out, 400, "Missing required fields");
            return;
        }

        System.out.println("New message from " + nickname + ": " + message);
        System.out.println(avatarChanges?"yes":"no");

        ServiceUtils.sendSuccessResponse(out);
    }

}
