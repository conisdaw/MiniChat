package clients;

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

    public static void sendRequest(String request, String serverHost, int serverPort) {
        try (Socket socket = new Socket(serverHost, serverPort);
             OutputStream out = socket.getOutputStream();
             InputStream in = socket.getInputStream()) {

            // 发送请求
            out.write(request.getBytes(StandardCharsets.UTF_8));
            out.flush();

            // 读取响应
            byte[] buffer = new byte[1024];
            int bytesRead = in.read(buffer);
            String response = new String(buffer, 0, bytesRead);
            System.out.println("服务器响应：\n" + response);

        } catch (IOException e) {
            System.err.println("测试失败：" + e.getMessage());
        }
    }
}
