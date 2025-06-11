import java.io.IOException;

import core.Config;
import sever.user.UserInterfaces;

public class Main {
    public static void main(String[] args) throws IOException {
        Config.PORT = 8080;
        UserInterfaces userInterfaces = new UserInterfaces(Config.PORT);
    }
}