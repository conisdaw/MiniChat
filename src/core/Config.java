package core;

public class Config {
    public static final String DB_PATH = "DATA.db";
    public static int PORT = GetUserContent.getPort();
    public static String IP = GetUserContent.personIP();
    public static String USER_ID = GetUserContent.UserID();
    public static final String USER_NAME = GetUserContent.UserName();
    public static String IMAGE_PATH = "/resources/image";
    public static final String FONT_PATH = "/resources/fonts/SourceHanSansSC.otf";
    public static final String FILE_BASE_DIR = "files";
}
