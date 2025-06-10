import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class test {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8080; // 与服务端端口保持一致

    public static void main(String[] args) {
        testChat();
        testCreateLink();
    }

    /**
     * 测试对话
     */
    private static void testChat() {
        String jsonPayload = "{\"avatarChanges\":false,\"nickname\":\"测试用户\",\"message\":\"你好世界\"}";
        String httpRequest = "POST /chat HTTP/1.1\r\n" +
                "Content-Type: application/json\r\n" +
                "Content-Length: " + jsonPayload.length() + "\r\n" +
                "\r\n" +
                jsonPayload;

        sendTestRequest(httpRequest, "正常请求测试");
    }

    /**
     * 测试连接创建
     */
    private static void testCreateLink() {
        String jsonPayload = "{\"isSolo\":true,\"userID\":\"测试用户1,测试用户2\"}";
        String httpRequest = "POST /createLink HTTP/1.1\r\n" +
                "Content-Type: application/json\r\n" +
                "Content-Length: " + jsonPayload.length() + "\r\n" +
                "\r\n" +
                jsonPayload;

        sendTestRequest(httpRequest, "正常请求测试");
    }

    private static void sendTestRequest(String request, String testName) {
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             OutputStream out = socket.getOutputStream();
             InputStream in = socket.getInputStream()) {

            System.out.println("\n=== " + testName + " ===");

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
