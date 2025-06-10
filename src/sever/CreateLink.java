package sever;

import java.io.IOException;
import java.io.OutputStream;

public class CreateLink {
    public void handle(String jsonBody, OutputStream out) throws IOException {
        boolean isSolo = ServiceUtils.extractBooleanField(jsonBody, "isSolo");
        String[] userID = ServiceUtils.extractStringField(jsonBody, "userID").split(",");

        System.out.println(isSolo);
        for(String a : userID) System.out.println(a);

        ServiceUtils.sendSuccessResponse(out);
    }
}
