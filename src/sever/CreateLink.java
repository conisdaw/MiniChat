package sever;

import java.io.IOException;
import java.io.OutputStream;

public class CreateLink {
    public void handle(String jsonBody, OutputStream out) throws IOException {
        String nickname = ServiceUtils.extractStringField(jsonBody, "nickname");
        String message = ServiceUtils.extractStringField(jsonBody, "message");

        if (nickname.isEmpty() || message.isEmpty()) {
            ServiceUtils.sendErrorResponse(out, 400, "Missing required fields");
            return;
        }

        System.out.println("New message from " + nickname + ": " + message);
        ServiceUtils.sendSuccessResponse(out);
    }
}
