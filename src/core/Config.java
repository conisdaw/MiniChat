package core;

import data.Database;

public class Config {
    public static final String DB_PATH = "DATA.db";
    public static int PORT = 8080;
    static {Database.initializeDatabase(DB_PATH);}
    public static String IP = "";
    public static String USER_ID = "";
    public static String USER_NAME = "";
    public static final String IMAGE_PATH = "/image";
    public static final String FONT_PATH = "/fonts/SourceHanSansSC.otf";
    public static final String FILE_BASE_DIR = "files";
}
