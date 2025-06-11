package sever;

import java.io.IOException;
import java.io.OutputStream;

public class ServiceUtils {
     public static String extractStringField(String json, String key) {
        String pattern = "\"" + key + "\":\"";
        int keyIndex = json.indexOf(pattern);

        if (keyIndex == -1) {
            System.err.println("缺少字段: " + key);
            return "";
        }

        int start = keyIndex + pattern.length();
        int end = json.indexOf("\"", start);

        if (end == -1) {
            System.err.println("字段 " + key + " 缺少结束引号");
            return json.substring(start);
        }

        return json.substring(start, end);
    }

     public static boolean extractBooleanField(String json, String key) {
        String pattern = "\"" + key + "\":";
        int keyIndex = json.indexOf(pattern);

        if (keyIndex == -1) {
            System.err.println("缺少字段: " + key);
            return false;
        }

        int start = keyIndex + pattern.length();
        int end = json.indexOf(",", start);
        if (end == -1) end = json.indexOf("}", start);

        String value = json.substring(start, end).trim();
        return Boolean.parseBoolean(value);
    }

    public static int extractIntField(String json, String key) {
        String pattern = "\"" + key + "\":";
        int keyIndex = json.indexOf(pattern);

        if (keyIndex == -1) {
            System.err.println("缺少字段: " + key);
            return 0;
        }

        int start = keyIndex + pattern.length();
        int end = json.indexOf(",", start);
        if (end == -1) end = json.indexOf("}", start);

        try {
            String value = json.substring(start, end).trim();
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            System.err.println("字段 " + key + " 格式错误: " + e.getMessage());
            return 0;
        }
    }


    public static void sendSuccessResponse(OutputStream out) throws IOException {
        String response = "HTTP/1.1 200 OK\r\n"
                + "Content-Type: application/json\r\n\r\n"
                + "{\"status\":\"success\", \"message\":\"请求成功\"}";
        out.write(response.getBytes());
    }


     public static void sendErrorResponse(OutputStream out, int code, String message) throws IOException {
        String response = "HTTP/1.1 " + code + " Error\r\n"
                + "Content-Type: application/json\r\n\r\n"
                + "{\"status\":\"error\", \"code\":" + code + ", \"message\":\"" + message + "\"}";
        out.write(response.getBytes());
    }


}
