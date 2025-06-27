package core;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

public class FontUtil {
    public static Font loadCustomFont(float size) {
        try {
            InputStream is = FontUtil.class.getResourceAsStream(Config.FONT_PATH);
            if (is == null) {
                throw new IOException("字体文件未找到: " + Config.FONT_PATH +
                        "\n请确保文件存在于资源目录中");
            }
            Font font = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(size);
            is.close();
            return font;
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
            return new Font("Microsoft YaHei", Font.PLAIN, (int) size);
        }
    }
}