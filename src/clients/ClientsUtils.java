package clients;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientsUtils {
    public static String constructRequest(String path, String jsonPayload) {
        String httpRequest = "POST " + path + " HTTP/1.1\r\n" +
                "Content-Type: application/json\r\n" +
                "Content-Length: " + jsonPayload.length() + "\r\n" +
                "\r\n" +
                jsonPayload;
        return httpRequest;
    }

    public static String sendRequest(String request, String serverHost, int serverPort) {
        try (Socket socket = new Socket(serverHost, serverPort);
             OutputStream out = socket.getOutputStream();
             InputStream in = socket.getInputStream()) {

            // 发送请求
            out.write(request.getBytes(StandardCharsets.UTF_8));
            out.flush();

            // 读取响应状态行
            ByteArrayOutputStream statusBuffer = new ByteArrayOutputStream();
            while (true) {
                int b = in.read();
                if (b == -1) break;
                statusBuffer.write(b);
                if (statusBuffer.size() > 4 &&
                        statusBuffer.toString().endsWith("\r\n")) {
                    break;
                }
            }

            String statusLine = statusBuffer.toString(StandardCharsets.UTF_8.name());
            if (!statusLine.startsWith("HTTP/1.1 200 OK")) {
                return null;
            }

            ByteArrayOutputStream headerBuffer = new ByteArrayOutputStream();
            while (true) {
                int b = in.read();
                if (b == -1) break;
                headerBuffer.write(b);
                String headers = headerBuffer.toString(StandardCharsets.UTF_8.name());
                if (headers.endsWith("\r\n\r\n")) {
                    break;
                }
            }

            ByteArrayOutputStream jsonBuffer = new ByteArrayOutputStream();
            while (true) {
                int b = in.read();
                if (b == -1 || b == '}') {
                    jsonBuffer.write(b);
                    break;
                }
                jsonBuffer.write(b);
            }

            String json = jsonBuffer.toString(StandardCharsets.UTF_8.name());
            int start = json.indexOf("\"status\":\"") + 10;
            int end = json.indexOf("\"", start);
            if (start < 10 || end == -1) {
                return null;
            }
            return json.substring(start, end);

        } catch (IOException e) {
            System.err.println("发送失败：" + e.getMessage());
        }
        return null;
    }
}
